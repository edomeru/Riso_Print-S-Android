//
//  common.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2023 RISO KAGAKU CORPORATION. All rights reserved.
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
    kJobStatusWaking,
    kJobStatusConnected,
    kJobStatusSending,
    kJobStatusSent,
    kJobStatusJobNumUpdate
};

// ホスト名出力処理の追加 Start
// ・重複するdirectprint.c側の宣言はメンテ効率化のため削除した。
//directprint_job *directprint_job_new(const char *printer_name, const char *app_name, const char *app_version,
 //                                    const char *user_name, int job_num, const char *job_name, const char *filename,
 //                                    const char *print_settings, const char *ip_address, directprint_callback callback);
directprint_job *directprint_job_new(const char *printer_name, const char *host_name, const char *app_name, const char *app_version,
                                     const char *user_name, int job_num, const char *job_name, const char *filename,
                                     const char *print_settings, const char *ip_address, const char *mac_address, directprint_callback callback);
// ホスト名出力処理の追加 End

void directprint_job_free(directprint_job *print_job);
void *directprint_job_get_caller_data(directprint_job *print_job);
void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data);
void directprint_job_cancel(directprint_job *print_job);
int directprint_job_lpr_print(directprint_job *print_job);
int directprint_job_raw_print(directprint_job *print_job);

/*
 * UTF8 16進数変換
 * [in] 変換前文字列
 * [out] 変換後文字列 or NULL失敗
 */
char* ConvertUTF8String(char* str);

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
    kSnmpStateCancelled = -1,
    kSnmpStateInitialized = 0,
    kSnmpStateStarted = 1,
    kSnmpStateEnded = 2
} kSnmpState;

// SNMP Capabilities

typedef enum
{
    kSnmpCapabilityBookletFinishing,
    kSnmpCapabilityStapler,
    kSnmpCapabilityFin23Holes,
    kSnmpCapabilityFin24Holes,
    kSnmpCapabilityFin0Holes, //Mantis82960
    kSnmpCapabilityTrayFaceDown,
    kSnmpCapabilityTrayTop,
    kSnmpCapabilityTrayStack,
    kSnmpCapabilityLPR,
    kSnmpCapabilityRaw,
    kSnmpCapabilityExternalFeeder,
    kSnmpCapabilityCount
} kSnmpCapability;


// Target Printer Series
typedef enum
{
    kPrinterSeriesIS,
    kPrinterSeriesFW,
    kPrinterSeriesGD,
    kPrinterSeriesFT,
    kPrinterSeriesGL,
    kDeviceSeriesCount
} kPrinterSeries;

snmp_context *snmp_context_new(snmp_discovery_ended_callback discovery_ended_callback, snmp_printer_added_callback printer_added_callback, const char* community_name);
void snmp_context_free(snmp_context *context);
void snmp_device_discovery(snmp_context *context);
void snmp_manual_discovery(snmp_context *context, const char *ip_address);
void snmp_cancel(snmp_context *context);
void *snmp_context_get_caller_data(snmp_context *context);
void snmp_context_set_caller_data(snmp_context *context, void *caller_data);

snmp_device *snmp_device_new(const char *ip_address);
void snmp_device_free(snmp_device *device);
const char *snmp_device_get_ip_address(snmp_device *device);
const char *snmp_device_get_mac_address(snmp_device *device);
const char *snmp_device_get_name(snmp_device *device);
int snmp_device_get_series(snmp_device *device);
int snmp_device_get_capability_status(snmp_device *device, int capability);

/**
 Util
 */

int util_validate_ip(const char *input_ip, char *formatted_ip, size_t max_len);

#endif
