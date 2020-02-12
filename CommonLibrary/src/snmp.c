//
//  snmp.c
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include "net-snmp/net-snmp-config.h"
#include "net-snmp/net-snmp-includes.h"
#include <arpa/inet.h>
#include "common.h"

#define SNMP_MANAGER "snmpmanager"
#define BROADCAST_ADDRESS "255.255.255.255"
#define COMMUNITY_NAME_DEFAULT "public"
#define SESSION_TIMEOUT 10000000
#define REQ_ID_DISCOVERY 0x000003
#define IP_ADDRESS_LENGTH 128
#define MIB_STRING_LENGTH 256
#define COMMUNITY_NAME_LENGTH 32
#define TIMEOUT 10
#define IPV6_LINK_LOCAL_PREFIX "fe80"

#define PDL_VALUE 54 // PDF

#define SNMPV3_USER "risosnmp"
#define SNMPV3_PASS "risosnmp"

#define FT_PRINTER_TYPE "FT"
#define GL_PRINTER_TYPE "GL"
#define OIS_PRINTER_TYPE "OIS"

#define DETECT_ALL_DEVICES 0  // 0 for RISO only

typedef struct
{
    snmp_device *first;
    snmp_device *current;
}caps_queue;

struct snmp_context_s
{
    int state;
    snmp_discovery_ended_callback discovery_ended_callback;
    snmp_printer_added_callback printer_added_callback;
    snmp_device *device_list;
    char ip_address[IP_ADDRESS_LENGTH];
    char community_name[COMMUNITY_NAME_LENGTH+1];
    int is_broadcast;
    
    caps_queue device_queue;
    
    pthread_t main_thread;
    pthread_mutex_t mutex;
    pthread_mutex_t queue_mutex;
    
    void *caller_data;
};

enum
{
    MIB_HW_STAT = 0,
    MIB_PDL,
    MIB_DEV_DESCR,
    MIB_HW_CAP_1,
    MIB_HW_CAP_2,
    MIB_HW_CAP_3,
    MIB_HW_CAP_4,
    MIB_HW_CAP_5,
    MIB_HW_CAP_6,
    MIB_HW_CAP_7,
    MIB_HW_CAP_8,
    MIB_HW_CAP_9,
    MIB_INFO_COUNT
};

struct snmp_device_s
{
    char ip_address[IP_ADDRESS_LENGTH];
    char device_info[MIB_INFO_COUNT][MIB_STRING_LENGTH];
    
    snmp_device *next;
};

static const char *MIB_REQUESTS[] = {
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.1", // Finisher 2/4 holes
    "1.3.6.1.2.1.43.15.1.1.2.1.5",
    "1.3.6.1.2.1.25.3.2.1.3.1",
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.3", // Booklet-finishing unit
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.20", // Offset Stapler
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.2", // Finisher 2/3 holes
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.1", // Finisher 2/4 holes
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.1", // Tray face-down
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.3", // Tray top
    "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.4", // Tray stack
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.25", // External feeder
    "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.26", // Finisher 0 holes
};

#define AZA_DEVICE_NAME_COUNT 3

static const char *AZA_DEVICE_NAMES[] = {
    "RISO IS1000C-J",
    "RISO IS1000C-G",
    "RISO IS950C-G",
};

#define FW_DEVICE_NAME_COUNT 21

static const char *FW_DEVICE_NAMES[] = {
    // Japan, Overseas or Korea:
    "ORPHIS FW5230",
    "ORPHIS FW5230A",
    "ORPHIS FW5231",
    "ORPHIS FW2230",
    "ORPHIS FW1230",
    "ComColor FW5230",
    "ComColor FW5230R",
    "ComColor FW5231",
    "ComColor FW5231R",
    "ComColor FW5000",
    "ComColor FW5000R",
    "ComColor FW2230",
    "ComColor black FW1230",
    "ComColor black FW1230R",
    // China:
    "Shan Cai Yin Wang FW5230",
    "Shan Cai Yin Wang FW5230R",
    "Shan Cai Yin Wang FW5231",
    "Shan Cai Yin Wang FW2230 Wenjianhong",
    "Shan Cai Yin Wang FW2230 Lan",
    "Shan Cai Yin Wang black FW1230",
    "Shan Cai Yin Wang black FW1230R",
};

// Main functions
void snmp_device_discovery(snmp_context *context);
void snmp_manual_discovery(snmp_context *context, const char *ip_address);
void snmp_cancel(snmp_context *context);

// Thread
void *do_discovery(void *parameter);
void *do_capability_check(void *parameter);

// Callback
int snmp_discovery_callback(int operation, struct snmp_session *host, int req_id, struct snmp_pdu *pdu, void *magic);

// SNMP context accessors
snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback, const char *community_name);
void snmp_context_free(snmp_context *context);
int snmp_context_get_state(snmp_context *context);
void snmp_context_set_state(snmp_context *context, int state);
void snmp_context_device_add(snmp_context *context, snmp_device *device);
int snmp_context_device_find_with_ip(snmp_context *context, const char *ip_address);
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
int snmp_get_capabilities(snmp_context *context, snmp_device *device);
void snmp_call_add_callback(snmp_context *context, snmp_device *device);
void snmp_call_end_callback(snmp_context *context, int count);

void snmp_device_discovery(snmp_context *context)
{
    strncpy(context->ip_address, BROADCAST_ADDRESS, IP_ADDRESS_LENGTH - 1);
    context->is_broadcast = 1;

    pthread_create(&context->main_thread, 0, do_discovery, (void *)context);
}

void snmp_manual_discovery(snmp_context *context, const char *ip_address)
{
    // Check if ipv6
    struct in6_addr ip_v6;
    int result = inet_pton(AF_INET6, ip_address, &ip_v6);
    if (result == 1)
    {
        if (strncmp(ip_address, IPV6_LINK_LOCAL_PREFIX, strlen(IPV6_LINK_LOCAL_PREFIX)) == 0)
        {
            snprintf(context->ip_address, IP_ADDRESS_LENGTH - 1, "udp6:[%s%%en0]", ip_address);
        }
        else
        {
            snprintf(context->ip_address, IP_ADDRESS_LENGTH - 1, "udp6:[%s]", ip_address);
        }
    }
    else
    {
        strncpy(context->ip_address, ip_address, IP_ADDRESS_LENGTH - 1);
    }
    context->is_broadcast = 0;
    
    pthread_create(&context->main_thread, 0, do_discovery, (void *)context);
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
    
    // Setup community name
    if(strlen(context->community_name) > 0 )
    {
        // set community name from Application
        session.community = (u_char *) strdup(context->community_name);
        session.community_len = strlen(context->community_name);
    }
    else
    {
        // Note: Handling of blank community name should be done in the Application side
        // This handling is just to ensure that the default behavior (use public community name)
        // is handled also in the common module in case Application fails to enforce it
        session.community = (u_char *) strdup(COMMUNITY_NAME_DEFAULT);
        session.community_len = strlen(COMMUNITY_NAME_DEFAULT);
    }
    session.timeout = SESSION_TIMEOUT;
    session.callback = snmp_discovery_callback;
    session.callback_magic = context;
    session.retries = 0;
    session.securityName = 0;
    
    if (context->is_broadcast == 0)
    {
        // Use V3 for Unicast
        session.version = SNMP_VERSION_3;
        session.securityLevel = SNMP_SEC_LEVEL_AUTHNOPRIV;
        session.securityName = strdup(SNMPV3_USER);
        session.securityNameLen = strlen(SNMPV3_USER);
        session.securityAuthProto = usmHMACMD5AuthProtocol;
        session.securityAuthProtoLen = USM_AUTH_PROTO_MD5_LEN;
        session.securityAuthKeyLen = USM_AUTH_KU_LEN;
        char *password = strdup(SNMPV3_PASS);
        if (generate_Ku(session.securityAuthProto, (u_int)session.securityAuthProtoLen, (u_char *)password, strlen(SNMPV3_PASS), session.securityAuthKey, &session.securityAuthKeyLen) != SNMPERR_SUCCESS)
        {
            free(session.peername);
            free(session.community);
            free(session.securityName);
            free(password);
            snmp_call_end_callback(context, -1);
            
            return 0;
        }
        free(password);
    }
    else
    {
        // Use V1 for Broadcast
        session.flags |= SNMP_FLAGS_UDP_BROADCAST;
        session.version = SNMP_VERSION_1;
    }
    
    snmp_context_set_state(context, kSnmpStateStarted);
    
    // Open session
    ss = snmp_open(&session);
    if (!ss)
    {
        snmp_sess_perror("ack", &session);
        //snmp_log(LOG_ERR, "Error opening snmp session.\n");
        
        free(session.peername);
        free(session.community);
        
        snmp_call_end_callback(context, -1);
        
        return 0;
    }
    
    // Create PDU request
    pdu_request = snmp_pdu_create(SNMP_MSG_GET);
    pdu_request->reqid = REQ_ID_DISCOVERY;
    
    oid oid[MAX_OID_LEN];
    size_t oid_len = MAX_OID_LEN;
    for (int i = MIB_HW_STAT; i <= MIB_PDL; i++)
    {
        read_objid(MIB_REQUESTS[i], oid, &oid_len);
        snmp_add_null_var(pdu_request, oid, oid_len);
    }
    
    // Send the request
    if (!snmp_send(ss, pdu_request))
    {
        snmp_sess_perror("ack", ss);
        //snmp_log(LOG_ERR, "Error in sending request.\n");
        
        snmp_close(ss);
        free(session.peername);
        free(session.community);
        snmp_call_end_callback(context, -1);
        
        return 0;
    }
    
    pthread_t caps_thread;
    pthread_create(&caps_thread, 0, do_capability_check, context);

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
        timeout.tv_sec = 0;
        timeout.tv_usec = 0;
        FD_ZERO(&fdset);
        snmp_select_info(&fds, &fdset, &timeout, &block);
        fds = select(fds, &fdset, NULL, NULL, &timeout);
        if (fds)
        {
            snmp_read(&fdset);
            if (strcmp(context->ip_address, BROADCAST_ADDRESS) != 0)
            {
                snmp_context_set_state(context, kSnmpStateEnded);
                break;
            }
        }
        else
        {
            time_t current_time;
            time(&current_time);
            if (difftime(current_time, start_time) >= TIMEOUT)
            {
                snmp_timeout();
                if (snmp_context_get_state(context) != kSnmpStateCancelled)
                {
                    snmp_context_set_state(context, kSnmpStateEnded);
                }
                break;
            }
        }
    }
    
    // Cleanup
    snmp_close(ss);
    free(session.peername);
    free(session.community);
    if (session.securityName != 0)
    {
        free(session.securityName);
    }
    
    pthread_join(caps_thread, 0);
    
    if (snmp_context_get_state(context) != kSnmpStateCancelled)
    {
        int count = snmp_context_device_count(context);
        snmp_call_end_callback(context, count);
    }
    
    return 0;
}

void *do_capability_check(void *parameter)
{
    snmp_context *context = parameter;
    
    while (1)
    {
        int state = snmp_context_get_state(context);
        if (state == kSnmpStateCancelled)
        {
            break;
        }
        
        snmp_device *device = 0;
        pthread_mutex_lock(&context->queue_mutex);
        if (context->device_queue.first != 0)
        {
            device = context->device_queue.first;
            context->device_queue.first = device->next;
            if (context->device_queue.first == 0)
            {
                context->device_queue.current = 0;
            }
        }
        pthread_mutex_unlock(&context->queue_mutex);
        
        if (device == 0)
        {
            if (state == kSnmpStateEnded)
            {
                break;
            }
            else
            {
                continue;
            }
        }
        
        // For non-broadcast search (single IP), SNMP V3 is being used to initially search device.
        // SNMP V3 does not use the community name string during search. (https://www.paessler.com/manuals/ipcheck_server_monitor/whatisansnmpcommunitystring)
        // However process of querying device name uses SNMP V2 which uses the community name string
        // For single IP search, if community name used for search did not match community name of device,
        // the result is the device is found but was not able to get the device name
        // to resolve this, do not add printer if the device name is not obtained
        if (strlen(snmp_device_get_name(device)) > 0)
        {
            snmp_context_device_add(context, device);
            snmp_call_add_callback(context, device);
        }
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

snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback, const char* community_name)
{
    snmp_context *context = (snmp_context *)malloc(sizeof(snmp_context));
    memset(context->ip_address, 0, IP_ADDRESS_LENGTH);
    memset(context->community_name, 0, COMMUNITY_NAME_LENGTH + 1);
    
    if(community_name != NULL)
    {
        strncpy(context->community_name, community_name, COMMUNITY_NAME_LENGTH);
    }
    
    context->state = kSnmpStateInitialized;
    context->discovery_ended_callback = discovery_ended_callback;
    context->printer_added_callback = printer_added_callback;
    context->device_list = 0;
    context->device_queue.first = 0;
    context->device_queue.current = 0;
    context->is_broadcast = 0;
    pthread_mutex_init(&context->mutex, 0);
    pthread_mutex_init(&context->queue_mutex, 0);
    
    return context;
}

void snmp_context_free(snmp_context *context)
{
    pthread_mutex_destroy(&context->mutex);
    pthread_mutex_destroy(&context->queue_mutex);
    
    while (context->device_list != 0)
    {
        snmp_device *device = context->device_list;
        context->device_list = device->next;
        snmp_device_free(device);
    }
    
    snmp_device *queue_device = context->device_queue.first;
    while (queue_device != 0)
    {
        snmp_device *next_device = queue_device->next;
        snmp_device_free(queue_device);
        queue_device = next_device;
    }
    
    free(context);
}

void snmp_cancel(snmp_context *context)
{
    snmp_context_set_state(context, kSnmpStateCancelled);
    pthread_join(context->main_thread, 0);
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

int snmp_context_device_find_with_ip(snmp_context *context, const char *ip_address)
{
    snmp_device *device = context->device_list;
    while (device != 0)
    {
        if (strcmp(ip_address, device->ip_address) == 0)
        {
            return 1;
        }
        device = device->next;
    }
    
    return 0;
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
    return device->device_info[MIB_DEV_DESCR];
}

int snmp_device_get_series(snmp_device *device)
{
    for (int i = 0; i < AZA_DEVICE_NAME_COUNT; i++)
    {
        if (strcmp(AZA_DEVICE_NAMES[i], device->device_info[MIB_DEV_DESCR]) == 0)
        {
            // IS Series
            return kPrinterSeriesIS;
        }
    }
    
    for (int i = 0; i < FW_DEVICE_NAME_COUNT; i++)
    {
        if (strcmp(FW_DEVICE_NAMES[i], device->device_info[MIB_DEV_DESCR]) == 0)
        {
            // FW Series
            return kPrinterSeriesFW;
        }
    }

    if (strstr(device->device_info[MIB_DEV_DESCR], FT_PRINTER_TYPE) != NULL ||
        strstr(device->device_info[MIB_DEV_DESCR], OIS_PRINTER_TYPE) != NULL)
    {
        // FT Series / OIS Series
        return kPrinterSeriesFT;
    }

    if (strstr(device->device_info[MIB_DEV_DESCR], GL_PRINTER_TYPE) != NULL)
    {
        // GL Series
        return kPrinterSeriesGL;
    }

    return kPrinterSeriesGD;
}

int snmp_device_get_capability_status(snmp_device *device, int capability)
{
    int supported = 0;
    switch (capability)
    {
        case kSnmpCapabilityLPR:
            supported = 1;
            break;
        case kSnmpCapabilityRaw:
            supported = 1;
            
            // METHOD OF DETECTION IS TO BE UPDATED:
            for (int i = 0; i < AZA_DEVICE_NAME_COUNT; i++)
            {
                if (strcmp(AZA_DEVICE_NAMES[i], device->device_info[MIB_DEV_DESCR]) == 0)
                {
                    supported = 0;
                    break;
                }
            }
            break;
        case kSnmpCapabilityStapler:
            if ((strlen(device->device_info[MIB_HW_CAP_1 + kSnmpCapabilityStapler]) > 0) ||
                        (strlen(device->device_info[MIB_HW_CAP_1 + kSnmpCapabilityFin23Holes]) > 0) ||
                        (strlen(device->device_info[MIB_HW_CAP_1 + kSnmpCapabilityFin24Holes]) > 0) ||
                        (strlen(device->device_info[MIB_HW_CAP_9]) > 0))//Mantis82960
            {
                supported = 1;
            }
            break;
        case kSnmpCapabilityExternalFeeder:
            if ((snmp_device_get_series(device) == kPrinterSeriesFT || snmp_device_get_series(device) == kPrinterSeriesGL) && 
                strlen(device->device_info[MIB_HW_CAP_8]) > 0) {
                supported = 1;
            } else {
                supported = 0;
            }
            break;
        case kSnmpCapabilityFin0Holes:
            if ((snmp_device_get_series(device) == kPrinterSeriesFT || snmp_device_get_series(device) == kPrinterSeriesGL) && 
                strlen(device->device_info[MIB_HW_CAP_9]) > 0) {
                supported = 1;
            } else {
                supported = 0;
            }
            break;
        default:
            if (capability > kSnmpCapabilityFin0Holes) {
                capability -= 1;
            }
            if (strlen(device->device_info[MIB_HW_CAP_1 + capability]) > 0)
            {
                supported = 1;
            }
            break;
    }
    
    return supported;
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
        sprintf(ip_address, "%s", ipv6Addr);
    }
    
    return 1;
}

int snmp_handle_pdu_response(char *ip_address, netsnmp_variable_list *var_list, snmp_context *context)
{
    snmp_device *device = snmp_device_new(ip_address);

    // Parse information
    int valid = 0;
    size_t hw_stat_oid_len = MAX_OID_LEN;
    oid hw_stat_oid[MAX_OID_LEN];
    read_objid(MIB_REQUESTS[MIB_HW_STAT], hw_stat_oid, &hw_stat_oid_len);
    size_t pdl_oid_len = MAX_OID_LEN;
    oid pdl_oid[MAX_OID_LEN];
    read_objid(MIB_REQUESTS[MIB_PDL], pdl_oid, &pdl_oid_len);
    
    for (netsnmp_variable_list *vars = var_list; vars != 0; vars = vars->next_variable)
    {
        if (snmp_oid_compare(vars->name, vars->name_length, hw_stat_oid, hw_stat_oid_len) == 0)
        {
            if (vars->type == ASN_INTEGER)
            {
                valid++;
                continue;
            }
        }
        
        if (snmp_oid_compare(vars->name, vars->name_length, pdl_oid, pdl_oid_len) == 0)
        {
            if (vars->type == ASN_INTEGER)
            {
                long value = *vars->val.integer;
                if (value == PDL_VALUE)
                {
                    valid++;
                    continue;
                }
            }
        }
    }
    
#if DETECT_ALL_DEVICES
    if (context != 0)
#else
    if (valid == 2 && context != 0)
#endif
    {
        snmp_get_capabilities(context, device);
        pthread_mutex_lock(&context->queue_mutex);
        if (context->device_queue.first == 0 && context->device_queue.current == 0)
        {
            context->device_queue.first = device;
            context->device_queue.current = device;
        }
        else
        {
            context->device_queue.current->next = device;
            context->device_queue.current = device;
        }
        pthread_mutex_unlock(&context->queue_mutex);
        return 1;
    }
    
    snmp_device_free(device);
    return 0;
}

int snmp_get_capabilities(snmp_context *context, snmp_device *device)
{
    // Information on who we're going to talk to
    netsnmp_session session;
    netsnmp_session *ss;
    
    // Initialize session
    snmp_sess_init(&session);
    
    // Prepare IP Address
    char ip_address[IP_ADDRESS_LENGTH];
    struct in6_addr ip_v6;
    int result = inet_pton(AF_INET6, device->ip_address, &ip_v6);
    if (result == 1)
    {
        if (strncmp(ip_address, IPV6_LINK_LOCAL_PREFIX, strlen(IPV6_LINK_LOCAL_PREFIX)) == 0)
        {
            sprintf(ip_address, "udp6:[%s%%en0]", device->ip_address);
        }
        else
        {
            sprintf(ip_address, "udp6:[%s]", device->ip_address);
        }
    }
    else
    {
        sprintf(ip_address, "%s", device->ip_address);
    }
        
    // Setup session information
    session.peername = strdup(ip_address);
    
    // Initialize SNMPv1
    session.version = SNMP_VERSION_1;
    
    //Setup community name
    if(strlen(context->community_name) > 0 )
    {
        session.community = (u_char *) strdup(context->community_name);
        session.community_len = strlen(context->community_name);
    }
    else
    {
        // Note: Handling of blank community name should be done in the Application side
        // This handling is just to ensure that the default behavior (use public community name)
        // is handled also in the common module in case Application fails to enforce it
        session.community = (u_char *) strdup(COMMUNITY_NAME_DEFAULT);
        session.community_len = strlen(COMMUNITY_NAME_DEFAULT);
    }
    session.timeout = SESSION_TIMEOUT / MIB_INFO_COUNT;
    session.callback = 0;
    session.retries = 0;
    
    // Open session
    ss = snmp_open(&session);
    if (!ss)
    {
        free(session.peername);
        free(session.community);
        return 0;
    }
    
    for (int i = MIB_DEV_DESCR; i < MIB_INFO_COUNT; i++)
    {
        if (snmp_context_get_state(context) == kSnmpStateCancelled)
        {
            free(session.peername);
            free(session.community);
            return 0;
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
        
        if(pdu_response != 0)
        {
            snmp_free_pdu(pdu_response);
        }
        
        if (status != STAT_SUCCESS || pdu_response->errstat != SNMP_ERR_NOERROR)
        {
            break;
        }
    }
    
    snmp_close(ss);
    free(session.peername);
    free(session.community);
    return 1;
}

void snmp_call_add_callback(snmp_context *context, snmp_device *device)
{
    if (snmp_context_get_state(context) == kSnmpStateCancelled)
    {
        return;
    }
    
    context->printer_added_callback(context, device);
}

void snmp_call_end_callback(snmp_context *context, int count)
{
    if (snmp_context_get_state(context) == kSnmpStateCancelled)
    {
        return;
    }
    
    snmp_context_set_state(context, count);
    context->discovery_ended_callback(context, count);
}
