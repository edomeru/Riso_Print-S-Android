//
//  SNMP_Manager.c
//  SNMPDiscoveryApp
//
//  Created by Paulus on 10/3/13.
//  Copyright (c) 2013 a-LINK Group. All rights reserved.
//

#include "SNMP_Manager.h"
#include <stdio.h>
#include <net-snmp/net-snmp-config.h>
#include <net-snmp/net-snmp-includes.h>

static char *MIB_REQUESTS[] = {
    "1.3.6.1.2.1.1.1", // sysDesc
    "1.3.6.1.2.1.1.5", // sysName
    "1.3.6.1.2.1.1.6", // sysLocation
    "1.3.6.1.2.1.2.2.1.6", // ifPhysAddress
    "1.3.6.1.2.1.43" // Printer-MIB
};

#define MIB_GETNEXTOID_PRINTERINTERPRETERLANG       "1.3.6.1.2.1.43.15.1.1.2"

#define ASYNC_CAPABILITY_RETRIEVAL 0
#define REQ_ID_DISCOVERY 0x000003
#define REQ_ID_CAPABILITY 0x000005

#pragma mark -
#pragma mark Forward Declaration

// allocators
snmp_device *malloc_snmp_device(char *ip_addr);
void free_snmp_device(snmp_device *device);
snmp_discovery_data *malloc_snmp_discovery_data();
void free_snmp_discovery_data(snmp_discovery_data *data);

// utility functions
int insert_snmp_device(snmp_discovery_data *data, snmp_device *newDevice);
snmp_device *find_snmp_device(snmp_discovery_data *data, const char* ipAddress);
int count_snmp_devices(snmp_discovery_data *data);
int snmp_extract_ip_address(struct snmp_pdu *pdu, char *ip_addr);
int snmp_handle_pdu_response(char *ip_addr, netsnmp_variable_list *var_list, snmp_discovery_data *magic);
int perform_get_capabilities_sync(netsnmp_session *host, snmp_device *device);

// net-snmp callback
int snmp_discovery_callback(int operation, struct snmp_session *host, int reqid,
                            struct snmp_pdu *pdu, void *magic);
int snmp_discovery_callback2(int operation, struct snmp_session *host, int reqid,
                            struct snmp_pdu *pdu, void *magic);

#pragma mark -
#pragma mark Public

void snmp_device_discovery(discovery_ended_callback onEndCallback, printer_added_callback onAddCallback)
{
    // Information on who we're fgoing to talk to
    netsnmp_session session;
    netsnmp_session *ss;
    
    // Hold all info to send to remote host
    netsnmp_pdu *pduRequest;
    
    snmp_discovery_data *magicData = malloc_snmp_discovery_data();
    magicData->onEndCallback = onEndCallback;
    magicData->onAddCallback = onAddCallback;
    
    // Initialize the SNMP library
    init_snmp("snmpmanager");
    
    // Initialize Session
    snmp_sess_init(&session);
    
    // Setup session information
    session.peername = strdup("255.255.255.255");   // Do a network discovery via a broadcast
    session.flags |= SNMP_FLAGS_UDP_BROADCAST;        // set the SNMP flag
    
    // Initialize SNMPv1
    session.version = SNMP_VERSION_1;               // set the SNMP version number
    session.community = strdup("public");           // set the SNMPv1 community name used for authentication
    session.community_len = strlen(session.community);
    
    session.timeout = 10000000;
    
    session.callback = snmp_discovery_callback;        // set the SNMP callback
    session.callback_magic = magicData;
    
    // Open session
    ss = snmp_open(&session);                       // Establish the session
    
    if (!ss)
    {
        snmp_sess_perror("ack", &session);
        snmp_log(LOG_ERR, "Error opening!!\n");
        
        magicData->onEndCallback(-1);
        free_snmp_discovery_data(magicData);
        
        return;
    }
    
    // Create the PDU for the data for our request.
    pduRequest = snmp_pdu_create(SNMP_MSG_GETNEXT);
    pduRequest->reqid = REQ_ID_DISCOVERY;
    
    // Add all oid requests
    for (int i = 0; i < MIB_INFO_COUNT; i++)
    {
        oid oid[MAX_OID_LEN];
        size_t oid_len = MAX_OID_LEN;
        
        read_objid(MIB_REQUESTS[i], oid, &oid_len);
        
        snmp_add_null_var(pduRequest, oid, oid_len);
        
    }
    
    // Send the Request out.
    if (!snmp_send(ss, pduRequest))
    {
        snmp_sess_perror("ack", &session);
        snmp_log(LOG_ERR, "Error in sending!!\n");
        
        snmp_close(ss);
        
        magicData->onEndCallback(-1);
        free_snmp_discovery_data(magicData);
        
        return;
    }
    
    while (1)
    {
        int fds = 0, block = 0;
        fd_set fdset;
        struct timeval timeout; // timeout for select()
        timeout.tv_sec = 10;
        timeout.tv_usec = 0;
        FD_ZERO (&fdset);
        snmp_select_info (&fds, &fdset, &timeout, &block);
        fds = select (fds, &fdset, NULL, NULL, /*block?NULL:*/&timeout);
        if (fds)
        {
            snmp_read(&fdset);
        }
        else
        {
            snmp_timeout();
            break;
        }
    }
    
    // Do clean up
    snmp_close(ss);
    snmp_log(LOG_DEBUG, "Finished!!\n");
    
    magicData->onEndCallback(count_snmp_devices(magicData));
    
    free_snmp_discovery_data(magicData);
}

int snmp_get_print_capabilities(snmp_device *device, printer_added_callback onAddCallback)
{
    // Information on who we're fgoing to talk to
    netsnmp_session session;
    netsnmp_session *ss;
        
    // Initialize the SNMP library
    init_snmp("snmpmanager");
    
    // Initialize Session
    snmp_sess_init(&session);
    
    session.peername = (char *)device->ip_addr;
    
    // Initialize SNMPv1
    session.version = SNMP_VERSION_1;               // set the SNMP version number
    session.community = strdup("public");           // set the SNMPv1 community name used for authentication
    session.community_len = strlen(session.community);
    
    // Open session
    ss = snmp_open(&session);                       // Establish the session
    
    if (!ss)
    {
        snmp_sess_perror("ack", &session);
        snmp_log(LOG_ERR, "Error opening!!\n");
        
        // Do clean up
        snmp_close(ss);
        return 0;
    }
    
    
    int result = perform_get_capabilities_sync(ss, device);
    
    snmp_close(ss);
    
    // Any error, we will break it
    if (result)
    {
        onAddCallback(device);
    }
    
    return 1;
}

#pragma mark -
#pragma mark Allocators

snmp_device *malloc_snmp_device(char *ip_addr)
{
    snmp_device *newDevice = malloc(sizeof(snmp_device));
    strcpy(&newDevice->ip_addr[0], ip_addr);
    for (int i = 0; i < MIB_INFO_COUNT; i++)
    {
        strcpy(&newDevice->device_info[i][0], ip_addr);
    }
    newDevice->print_capabilities_count = 0;
    newDevice->next = 0;
    
    return newDevice;
}

void free_snmp_device(snmp_device *device)
{
    free(device);
}

snmp_discovery_data *malloc_snmp_discovery_data()
{
    snmp_discovery_data *newDiscoveryData = malloc(sizeof(snmp_discovery_data));
    newDiscoveryData->deviceList = 0;
    return newDiscoveryData;
}

void free_snmp_discovery_data(snmp_discovery_data *data)
{
    //free magic
    while (data->deviceList != 0)
    {
        snmp_device *device = data->deviceList;
        data->deviceList = data->deviceList->next;
        
        free_snmp_device(device);
    }
    
    free(data);
}

#pragma mark -
#pragma mark Utility Functions

int insert_snmp_device(snmp_discovery_data *data, snmp_device *newDevice)
{
    if (data->deviceList == 0)
    {
        data->deviceList = newDevice;
        return 1;
    }
    
    if (find_snmp_device(data, newDevice->ip_addr))
    {
        //already added do not add
        return 0;
    }
    
    snmp_device *device = data->deviceList;
    while (1)
    {
        if (device->next != 0)
        {
            device = device->next;
        }
        else
        {
            device->next = newDevice;
            break;
        }
    }
    
    return 1;
}

snmp_device *find_snmp_device(snmp_discovery_data *data, const char* ipAddress)
{
    snmp_device *device = data->deviceList;
    while (device)
    {
        if (strcmp(ipAddress, device->ip_addr) == 0)
        {
            break;
        }
        device = device->next;
    }
    return device;
}

int count_snmp_devices(snmp_discovery_data *data)
{
    int count = 0;
    snmp_device *device = data->deviceList;
    while (device)
    {
        count++;
        device = device->next;
    }
    return count;
}

int snmp_extract_ip_address(struct snmp_pdu *pdu, char *ip_addr)
{
	// Device information variables
    snmp_log(LOG_DEBUG, "Handling SNMP response\n");
    
	// Remote IP detection variables
	netsnmp_indexed_addr_pair *responder = (netsnmp_indexed_addr_pair *) pdu->transport_data;
	if (responder == NULL || pdu->transport_data_length != sizeof(netsnmp_indexed_addr_pair))
	{
        snmp_log(LOG_DEBUG, "Unable to extract IP address from SNMP response.\n");
		return 0;
	}
    
	struct sockaddr_in *remote = (struct sockaddr_in *) &(responder->remote_addr);
	if (remote == NULL)
	{
        snmp_log(LOG_DEBUG, "Unable to extract IP address from SNMP response.\n");
		return 0;
	}
    
	sprintf(ip_addr, "%s", inet_ntoa(remote->sin_addr));
    snmp_log(LOG_DEBUG, "IP Address of responder is %s.\n", ip_addr);
    
    return 1;
}

int snmp_handle_pdu_response(char *ip_addr, netsnmp_variable_list *var_list, snmp_discovery_data *magic)
{
    snmp_log(LOG_ERR, "snmp_handle_pdu_response!!\n");
    
    snmp_device *newDevice = malloc_snmp_device(ip_addr);
    
    // Parse info here
    int valid = 0;
    
    for ( netsnmp_variable_list *vars = var_list; vars; vars = vars->next_variable )
    {
        // Add all oid requests
        for (int i = 0; i < MIB_INFO_COUNT; i++)
        {
            oid oid[MAX_OID_LEN];
            size_t oid_len = MAX_OID_LEN;
            
            read_objid(MIB_REQUESTS[i], oid, &oid_len);
            
            if (snmp_oidtree_compare(vars->name, vars->name_length, oid, oid_len) == 0)
            {
                if (i == MIB_PRINTER_MIB)
                {
                    valid = 1;
                    continue;
                }
                
                //print_variable(oid, oid_len, vars);
                
                sprintf(newDevice->device_info[i], "");
                if (vars->type == ASN_OCTET_STR)
                {
                    char *sp = malloc(1 + vars->val_len);
                    memcpy(sp, vars->val.string, vars->val_len);
                    sp[vars->val_len] = '\0';
                    
                    //check here if hex string
                    
                    sprintf(newDevice->device_info[i], "%s", sp);
                    free(sp);
                }
            }
        }
    }
    
	if (valid && magic)
	{
        if (insert_snmp_device(magic, newDevice))
        {
#if !ASYNC_CAPABILITY_RETRIEVAL
            //magic->onAddCallback(newDevice);
            snmp_get_print_capabilities(newDevice, magic->onAddCallback);
#endif
            return 1;
        }
	}
    
    
	return 0;
}

int perform_get_capabilities_sync(netsnmp_session *host, snmp_device *device)
{
    char *strOid = MIB_GETNEXTOID_PRINTERINTERPRETERLANG;
    
    oid printerOid[MAX_OID_LEN];
    size_t printerOid_len = MAX_OID_LEN;
    
    read_objid(strOid, printerOid, &printerOid_len);
    
    oid oid[MAX_OID_LEN];
    size_t oid_len = MAX_OID_LEN;
    
    read_objid(strOid, oid, &oid_len);
    
    int result = 1;
    int count = 0;
    while (1)
    {
        result = 1;
        
        netsnmp_pdu *pduRequest;
        netsnmp_pdu *pduResponse;
        
        // Create the PDU for the data for our request.
        pduRequest = snmp_pdu_create(SNMP_MSG_GETNEXT);
        
        snmp_add_null_var(pduRequest, oid, oid_len);
        
        int status = snmp_synch_response(host, pduRequest, &pduResponse);
        
        if (status == STAT_SUCCESS && pduResponse->errstat == SNMP_ERR_NOERROR)
        {
            // Success
            netsnmp_variable_list *vars = pduResponse->variables;
            
            if (snmp_oidtree_compare(vars->name, vars->name_length, printerOid, printerOid_len) == 0)
            {
                memcpy(oid, vars->name, MAX_OID_LEN);
                oid_len = vars->name_length;
                
                int value = *vars->val.integer;
                device->print_capabilities[count++] = value;
                device->print_capabilities_count = count;
            }
            else
            {
                // Finished
                break;
            }
        }
        else
        {
            // FAILED
            result = 0;
            
            if (status == STAT_SUCCESS)
            {
                fprintf(stderr, "Error in packet\nReason: %s\n",
                        snmp_errstring(pduResponse->errstat));
            }
            else
            {
                snmp_sess_perror("snmpgetnext", host);
            }
            
            break;
        }
        if (pduResponse)
        {
            snmp_free_pdu(pduResponse);
        }
        
    }
    
    return result;
}

#pragma mark -
#pragma mark Callbacks

int snmp_discovery_callback(int operation, struct snmp_session *host, int reqid,
                                       struct snmp_pdu *pdu, void *magic)
{
    snmp_log(LOG_ERR, "snmp_discovery_callback!!\n");
    
    if (reqid == REQ_ID_DISCOVERY)
    {
        if (operation == NETSNMP_CALLBACK_OP_RECEIVED_MESSAGE)
        {
            char ip_addr[IP_ADDRESS_LEN];
            if (snmp_extract_ip_address(pdu, ip_addr))
            {
                if (snmp_handle_pdu_response(ip_addr, pdu->variables, (snmp_discovery_data *)magic))
                {
#if ASYNC_CAPABILITY_RETRIEVAL
                    snmp_device *device = find_snmp_device(magic, ip_addr);
                    int result = perform_get_capabilities_sync(host, device);
                    
                    if (result)
                    {
                        ((snmp_discovery_data *)magic)->onAddCallback(device);
                    }
#endif
                }
            }
        }
    }
    
    // Return 0 to process further callbacks
	return 0;
}
