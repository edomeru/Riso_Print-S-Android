//
//  SNMP_Manager.h
//  SNMPDiscoveryApp
//
//  Created by Paulus on 10/3/13.
//  Copyright (c) 2013 a-LINK Group. All rights reserved.
//

#ifndef SNMPDiscoveryApp_SNMP_Manager_h
#define SNMPDiscoveryApp_SNMP_Manager_h

enum {
    MIB_SYS_DESC = 0,
    MIB_SYS_NAME,
    MIB_SYS_LOCATION,
    MIB_IF_PHYS_ADDRESS,
    MIB_PRINTER_MIB,
    MIB_INFO_COUNT
};

#define IP_ADDRESS_LEN 128
#define MIB_STR_LEN 256
#define MAX_PRINT_CAPABILITIES 128

typedef struct snmp_device
{
 	char ip_addr[IP_ADDRESS_LEN];
    char device_info[MIB_INFO_COUNT][MIB_STR_LEN];
    
    int print_capabilities[MAX_PRINT_CAPABILITIES];
    int print_capabilities_count;
    
 	struct snmp_device *next;
} snmp_device;

// Callback declarations
typedef void (*discovery_ended_callback)(int);
typedef void (*printer_added_callback)(snmp_device *);

typedef struct {
 	snmp_device *deviceList;
    discovery_ended_callback onEndCallback;
    printer_added_callback onAddCallback;
} snmp_discovery_data;

// main methods
void snmp_device_discovery(discovery_ended_callback onEndCallback, printer_added_callback onAddCallback);
int snmp_get_print_capabilities(snmp_device *device, printer_added_callback onAddCallback);
void snmp_device_manualdiscovery(discovery_ended_callback onEndCallback, printer_added_callback onAddCallback, char** ipAddress);
void snmp_device_discovery_cancel();
int snmp_device_checkstatus(const char* ipAddress);
#endif
