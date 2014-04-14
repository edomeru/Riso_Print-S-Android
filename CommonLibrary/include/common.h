//
//  common.h
//  SmartDeviceApp
//
//  Created by Seph on 4/9/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#ifndef SmartDeviceApp_common_h
#define SmartDeviceApp_common_h

/**
 Direct Print
 */

// Direct print context
struct directprint_job_s;
typedef struct directprint_job_s directprint_job;

// Direct print notifiation callback
typedef void(*directprint_callback)(directprint_job *print_job, int status, float progress);

// Direct print status
enum kJobStatus
{
    kJobStatusErrorConnecting = -4,
    kJobStatusErrorSending = -3,
    kJobStatusErrorFile = -2,
    kJobStatusError = -1,
    kJobStatusStarted = 0,
    kJobStatusConnecting,
    kJobStatusConnected,
    kJobStatusSending,
    kJobStatusSent
};

directprint_job *directprint_job_new(const char *job_name, const char *filename, const char *print_settings, const char *ip_address, directprint_callback callback);
void directprint_job_free(directprint_job *print_job);
void *directprint_job_get_caller_data(directprint_job *print_job);
void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data);
int lpr_print(directprint_job *print_job);
int raw_print(directprint_job *print_job);


/**
 SNMP
 */

// SNMP Context
struct snmp_context_s;
typedef struct snmp_context_s snmp_context;

struct snmp_device_s;
typedef struct snmp_device_s snmp_device;

typedef void (*snmp_discovery_ended_callback)(snmp_context *context, int);
typedef void (*snmp_printer_added_callback)(snmp_context *context, snmp_device *);

// SNMP state
typedef enum
{
    kSnmpStateCancelled = 0,
    kSnmpStateStarted = 1,
    kSnmpStateEnded = 2
} kSnmpState;

// SNMP Capabilities

typedef enum
{
    kSnmpCapabilityBooklet,
    kSnmpCapabilityStapler,
    kSnmpCapabilityFin24Holes,
    kSnmpCapabilityFin23Holes,
    kSnmpCapabilityTrayFaceDown,
    kSnmpCapabilityTrayAutoStack,
    kSnmpCapabilityTrayTop,
    kSnmpCapabilityTrayStack,
    kSnmpCapabilityCount
} kSnmpCapability;

snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback);
void snmp_context_free(snmp_context *context);
void snmp_device_discovery(snmp_context *context);
void snmp_cancel(snmp_context *context);
void *snmp_context_get_caller_data(snmp_context *context);
void snmp_context_set_caller_data(snmp_context *context, void *caller_data);

const char *snmp_device_get_ip_address(snmp_device *device);
const char *snmp_device_get_name(snmp_device *device);
int snmp_device_get_capability_status(snmp_device *device, int capability);

#endif
