//
//  snmp.c
//  SmartDeviceApp
//
//  Created by Seph on 4/9/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/net-snmp-includes.h>
#include <arpa/inet.h>
#include "common.h"

#define SNMP_MANAGER "snmpmanager"
#define BROADCAST_ADDRESS "255.255.255.255"
//#define BROADCAST_ADDRESS "192.168.1.206"  // For debugging
#define COMMUNITY_NAME "public"
#define SESSION_TIMEOUT 10000000
#define REQ_ID_DISCOVERY 0x000003
#define IP_ADDRESS_LENGTH 128
#define MIB_STRING_LENGTH 256
#define TIMEOUT 10

#define SYS_OBJ_ID_VALUE "1.3.6.1.4.1.8072.3.2.10"

struct snmp_context_s
{
    int state;
    snmp_discovery_ended_callback discovery_ended_callback;
    snmp_printer_added_callback printer_added_callback;
    snmp_device *device_list;
    char ip_address[IP_ADDRESS_LENGTH];
    
    pthread_mutex_t mutex;
    
    void *caller_data;
};

enum
{
    MIB_SYS_OBJ_ID = 0,
    MIB_GENERAL_NAME,
    MIB_HW_CAP_1,
    MIB_HW_CAP_2,
    MIB_HW_CAP_3,
    MIB_HW_CAP_4,
    MIB_HW_CAP_5,
    MIB_HW_CAP_6,
    MIB_HW_CAP_7,
    MIB_HW_CAP_8,
    MIB_INFO_COUNT
};

struct snmp_device_s
{
    char ip_address[IP_ADDRESS_LENGTH];
    char device_info[MIB_INFO_COUNT][MIB_STRING_LENGTH];
    
    struct snmp_device_s *next;
};

typedef struct snmp_capabilty_data_s
{
    snmp_context *context;
    snmp_device *device;
} snmp_capability_data;

static const char *MIB_REQUESTS[] = {
    "1.3.6.1.2.1.1.2.0", // sysObjectId
    //"1.3.6.1.4.1.24807.1.2.1.1.1.0", // ijGeneralName
    "1.3.6.1.2.1.1.1.0", // sysDescr // TODO: Replace with above
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.3", // Booklet unit
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.20", // Stapler
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.1", // Finisher 2/4 holes
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.2", // Finisher 2/3 holes
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.1", // Tray face-down
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.2", // Tray auto-stacking
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.3", // Tray top
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.4", // Tray stack
};

// Main functions
void snmp_device_discovery(snmp_context *context);
void snmp_manual_discovery(snmp_context *context, const char *ip_address);
void snmp_cancel(snmp_context *context);

// Thread
void *do_discovery(void *parameter);

// Callback
int snmp_discovery_callback(int operation, struct snmp_session *host, int req_id, struct snmp_pdu *pdu, void *magic);

// SNMP context accessors
snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback);
void snmp_context_free(snmp_context *context);
int snmp_context_get_state(snmp_context *context);
void snmp_context_set_state(snmp_context *context, int state);
void snmp_context_device_add(snmp_context *context, snmp_device *device);
snmp_device *snmp_context_device_find_with_ip(snmp_context *context, const char *ip_address);
int snmp_context_device_count(snmp_context *context);
void *snmp_context_get_caller_data(snmp_context *context);
void snmp_context_set_caller_data(snmp_context *context, void *caller_data);

// SNMP device accessors
snmp_device *snmp_device_new(const char *ip_address);
void snmp_device_free(snmp_device *device);
const char *snmp_device_get_ip_address(snmp_device *device);
const char *snmp_device_get_name(snmp_device *device);
int snmp_device_get_capability_status(snmp_device *device, int capability);

// Utility functions
int snmp_extract_ip_address(netsnmp_pdu *pdu, char *ip_address);
int snmp_handle_pdu_response(char *ip_address, netsnmp_variable_list *var_list, snmp_context *context);
void snmp_get_capabilities(snmp_context *context, snmp_device *device);

void snmp_device_discovery(snmp_context *context)
{
    strncpy(context->ip_address, BROADCAST_ADDRESS, IP_ADDRESS_LENGTH - 1);
    
    pthread_t thread;
    pthread_create(&thread, 0, do_discovery, (void *)context);
    pthread_detach(thread);
}

void snmp_manual_discovery(snmp_context *context, const char *ip_address)
{
    strncpy(context->ip_address, ip_address, IP_ADDRESS_LENGTH - 1);
    
    pthread_t thread;
    pthread_create(&thread, 0, do_discovery, (void *)context);
    pthread_detach(thread);
}

/**
 MARK: Thread functions
 */

void *do_discovery(void *parameter)
{
    snmp_context *context = (snmp_context *)parameter;
    
    // Information on who we're going to talk to
    netsnmp_session session;
    netsnmp_session *ss;
    
    // Holds all info to send to remote host
    netsnmp_pdu *pdu_request;
    
    // Initialize net-snmp
    //setenv("MIBS", "", 1);
    init_snmp(SNMP_MANAGER);
    
    // Initialize session
    snmp_sess_init(&session);
    
    // Setup session information
    session.peername = strdup(context->ip_address);
    session.flags |= SNMP_FLAGS_UDP_BROADCAST;
    
    
    // Initialize SNMPv1
    session.version = SNMP_VERSION_1;
    session.community = (u_char *) strdup(COMMUNITY_NAME);
    session.community_len = strlen(COMMUNITY_NAME);
    
    session.timeout = SESSION_TIMEOUT;
    
    session.callback = snmp_discovery_callback;
    session.callback_magic = context;
    
    snmp_context_set_state(context, kSnmpStateStarted);
    
    // Open session
    ss = snmp_open(&session);
    if (!ss)
    {
        snmp_sess_perror("ack", &session);
        snmp_log(LOG_ERR, "Error opening snmp session.\n");
        
        free(session.peername);
        free(session.community);
        snmp_context_set_state(context, kSnmpStateEnded);
        context->discovery_ended_callback(context, -1);
        
        return 0;
    }
    
    // Create PDU request
    pdu_request = snmp_pdu_create(SNMP_MSG_GET);
    pdu_request->reqid = REQ_ID_DISCOVERY;
    
    oid oid[MAX_OID_LEN];
    size_t oid_len = MAX_OID_LEN;
    read_objid(MIB_REQUESTS[MIB_SYS_OBJ_ID], oid, &oid_len);
    snmp_add_null_var(pdu_request, oid, oid_len);
    
    // Send the request
    if (!snmp_send(ss, pdu_request))
    {
        snmp_sess_perror("ack", ss);
        snmp_log(LOG_ERR, "Error in sending request.\n");
        
        snmp_close(ss);
        free(session.peername);
        free(session.community);
        snmp_context_set_state(context, kSnmpStateEnded);
        context->discovery_ended_callback(context, -1);
        
        return 0;
    }
    
    time_t start_time;
    time(&start_time);
    while (1)
    {
        if (snmp_context_get_state(context) == kSnmpStateCancelled)
        {
            break;
        }
        
        int fds = 0;
        int block = 0;
        fd_set fdset;
        struct timeval timeout;
        timeout.tv_sec = 10;
        timeout.tv_usec = 0;
        FD_ZERO(&fdset);
        snmp_select_info(&fds, &fdset, &timeout, &block);
        fds = select(fds, &fdset, NULL, NULL, &timeout);
        if (fds)
        {
            snmp_read(&fdset);
        }
        else
        {
            time_t current_time;
            time(&current_time);
            if (difftime(current_time, start_time) >= TIMEOUT)
            {
                snmp_timeout();
                break;
            }
        }
    }
    
    // Cleanup
    snmp_close(ss);
    if (snmp_context_get_state(context) == kSnmpStateStarted)
    {
        free(session.peername);
        free(session.community);
        int count = snmp_context_device_count(context);
        snmp_context_set_state(context, kSnmpStateEnded);
        context->discovery_ended_callback(context, count);
    }
    
    return 0;
}




/**
 MARK: Net-snmp callback
 */

int snmp_discovery_callback(int operation, struct snmp_session *host, int req_id, netsnmp_pdu *pdu, void *magic)
{
    snmp_context *context = (snmp_context *)magic;
    if (snmp_context_get_state(context) == kSnmpStateCancelled)
    {
        return -1;
    }
    
    if (req_id == REQ_ID_DISCOVERY)
    {
        if (operation == NETSNMP_CALLBACK_OP_RECEIVED_MESSAGE)
        {
            char ip_address[IP_ADDRESS_LENGTH];
            if (snmp_extract_ip_address(pdu, ip_address))
            {
                snmp_handle_pdu_response(ip_address, pdu->variables, context);
            }
        }
    }
    
    return 0;
}

/**
 MARK: SNMP context accessors
 */

snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback)
{
    snmp_context *context = (snmp_context *)malloc(sizeof(snmp_context));
    context->discovery_ended_callback = discovery_ended_callback;
    context->printer_added_callback = printer_added_callback;
    context->device_list = 0;
    pthread_mutex_init(&context->mutex, 0);
    
    return context;
}

void snmp_context_free(snmp_context *context)
{
    pthread_mutex_destroy(&context->mutex);
    
    while (context->device_list != 0)
    {
        snmp_device *device = context->device_list;
        context->device_list = device->next;
        snmp_device_free(device);
    }
    
    free(context);
    context = 0;
}

void snmp_cancel(snmp_context *context)
{
    snmp_context_set_state(context, kSnmpStateCancelled);
}

int snmp_context_get_state(snmp_context *context)
{
    int state;
    pthread_mutex_lock(&context->mutex);
    state = context->state;
    pthread_mutex_unlock(&context->mutex);
    return state;
}

void snmp_context_set_state(snmp_context *context, int state)
{
    pthread_mutex_lock(&context->mutex);
    context->state = state;
    pthread_mutex_unlock(&context->mutex);
}

void snmp_context_device_add(snmp_context *context, snmp_device *device)
{
    if (context->device_list == 0)
    {
        context->device_list = device;
        return;
    }
    
    if (snmp_context_device_find_with_ip(context, device->ip_address) != 0)
    {
        return;
    }
    
    snmp_device *current_device = context->device_list;
    while (current_device->next != 0)
    {
        current_device = current_device->next;
    }
    current_device->next = device;
}

snmp_device *snmp_context_device_find_with_ip(snmp_context *context, const char *ip_address)
{
    snmp_device *device = context->device_list;
    while (device != 0)
    {
        if (strcmp(ip_address, device->ip_address) == 0)
        {
            break;
        }
        device = device->next;
    }
    
    return device;
}

int snmp_context_device_count(snmp_context *context)
{
    int count = 0;
    snmp_device *device = context->device_list;
    while (device != 0)
    {
        count++;
        device = device->next;
    }
    
    return count;
}

void *snmp_context_get_caller_data(snmp_context *context)
{
    return context->caller_data;
}

void snmp_context_set_caller_data(snmp_context *context, void *caller_data)
{
    context->caller_data = caller_data;
}

/**
 MARK: SNMP device accessors
 */

snmp_device *snmp_device_new(const char *ip_address)
{
    snmp_device *device = (snmp_device *)malloc(sizeof(snmp_device));
    strncpy(device->ip_address, ip_address, IP_ADDRESS_LENGTH);
    for (int i = 0; i < MIB_INFO_COUNT; i++)
    {
        device->device_info[i][0] = 0;
    }
    device->next = 0;
    
    return device;
}

void snmp_device_free(snmp_device *device)
{
    free(device);
    device = 0;
}

const char *snmp_device_get_ip_address(snmp_device *device)
{
    return device->ip_address;
}

const char *snmp_device_get_name(snmp_device *device)
{
    return device->device_info[MIB_GENERAL_NAME];
}

int snmp_device_get_capability_status(snmp_device *device, int capability)
{
    if (strlen(device->device_info[MIB_HW_CAP_1 + capability]) > 0)
    {
        return 1;
    }
    
    return 0;
}

/**
 MARK: Utility functions
 */

int snmp_extract_ip_address(netsnmp_pdu *pdu, char *ip_address)
{
    netsnmp_indexed_addr_pair *responder = (netsnmp_indexed_addr_pair *)pdu->transport_data;
    struct sockaddr_in6 *to = (struct sockaddr_in6 *)pdu->transport_data;
    if (responder == 0 || to == 0 || (pdu->transport_data_length != sizeof(netsnmp_indexed_addr_pair) && pdu->transport_data_length != sizeof(struct sockaddr_in6)))
    {
        return 0;
    }
    
    if (pdu->transport_data_length == sizeof(netsnmp_indexed_addr_pair))
    {
        struct sockaddr_in *remote = (struct sockaddr_in *)&(responder->remote_addr);
        if (remote == 0)
        {
            return 0;
        }
        sprintf(ip_address, "%s", inet_ntoa(remote->sin_addr));
    }
    else
    {
        char ipv6Addr[IP_ADDRESS_LENGTH];
        inet_ntop(AF_INET6, &(to->sin6_addr), ipv6Addr, IP_ADDRESS_LENGTH);
        sprintf(ip_address, "udp6:[%s%%en0]", ipv6Addr);
    }
    
    return 1;
}

int snmp_handle_pdu_response(char *ip_address, netsnmp_variable_list *var_list, snmp_context *context)
{
    snmp_device *device = snmp_device_new(ip_address);

    // Parse information
    int valid = 0;
    oid oid_val[MAX_OID_LEN];
    size_t oid_len = MAX_OID_LEN;
    read_objid(MIB_REQUESTS[MIB_SYS_OBJ_ID], oid_val, &oid_len);
    oid sys_obj_id_oid[MAX_OID_LEN];
    size_t sys_obj_id_oid_len = MAX_OID_LEN;
    read_objid(SYS_OBJ_ID_VALUE, sys_obj_id_oid, &sys_obj_id_oid_len);
    for (netsnmp_variable_list *vars = var_list; vars != 0; vars = vars->next_variable)
    {
        // Check for MIB_SYS_OBJ_ID value
        if (snmp_oid_compare(vars->name, vars->name_length, oid_val, oid_len) == 0)
        {
            if (vars->type == ASN_OBJECT_ID)
            {
                size_t len = (vars->val_len < sys_obj_id_oid_len ? vars->val_len : sys_obj_id_oid_len);
                if (snmp_oid_compare(vars->val.objid, len, sys_obj_id_oid, len) == 0)
                {
                    valid = 1;
                    continue;
                }
                valid = 1; // TODO: Uncomment code to restrict found printers to Riso printers only
                continue;
            }
        }
    }
    
    if (valid == 1 && context != 0)
    {
        snmp_get_capabilities(context, device);
        snmp_context_device_add(context, device);
        context->printer_added_callback(context, device);
        return 1;
    }
    
    snmp_device_free(device);
    return 0;
}

void snmp_get_capabilities(snmp_context *context, snmp_device *device)
{
    // Information on who we're going to talk to
    netsnmp_session session;
    netsnmp_session *ss;
    
    // Initialize session
    snmp_sess_init(&session);
    
    // Setup session information
    session.peername = strdup(device->ip_address);
    
    // Initialize SNMPv1
    session.version = SNMP_VERSION_1;
    session.community = (u_char *) strdup(COMMUNITY_NAME);
    session.community_len = strlen(COMMUNITY_NAME);
    session.timeout = SESSION_TIMEOUT;
    
    // Open session
    ss = snmp_open(&session);
    if (!ss)
    {
        free(session.peername);
        free(session.community);
        return;
    }
    
    for (int i = MIB_GENERAL_NAME; i < MIB_INFO_COUNT; i++)
    {
        if (snmp_context_get_state(context) == kSnmpStateCancelled)
        {
            break;
        }
        
        oid oid_value[MAX_OID_LEN];
        size_t oid_len = MAX_OID_LEN;
        read_objid(MIB_REQUESTS[i], oid_value, &oid_len);
        
        netsnmp_pdu *pdu_request;
        netsnmp_pdu *pdu_response;
        
        pdu_request = snmp_pdu_create(SNMP_MSG_GET);
        snmp_add_null_var(pdu_request, oid_value, oid_len);
        int status = snmp_synch_response(ss, pdu_request, &pdu_response);
        if (status == STAT_SUCCESS && pdu_response->errstat == SNMP_ERR_NOERROR)
        {
            netsnmp_variable_list *vars = pdu_response->variables;
            if (snmp_oid_compare(vars->name, vars->name_length, oid_value, oid_len) == 0)
            {
                if (vars->type == ASN_OCTET_STR)
                {
                    char *sp = (char *)malloc(vars->val_len + 1);
                    memcpy(sp, vars->val.string, vars->val_len);
                    sp[vars->val_len] = 0;
                    
                    sprintf(device->device_info[i], "%s", sp);
                    free(sp);
                }
                else if (vars->type == ASN_INTEGER)
                {
                    if (*vars->val.integer == 1)
                    {
                        device->device_info[i][0] = '1';
                        device->device_info[i][1] = 0;
                    }
                }
            }
        }
        
    }
}

