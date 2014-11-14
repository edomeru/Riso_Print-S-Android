//
//  AppDebug.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#ifndef _APPDEBUG_H_
#define _APPDEBUG_H_

#pragma mark - Debug Log Switches

#pragma mark Models
#define DEBUG_LOG_PRINTER_MODEL             0
#define DEBUG_LOG_PRINTSETTING_MODEL        0
#define DEBUG_LOG_PRINT_JOB_MODEL           0
#define DEBUG_LOG_PRINT_JOB_GROUP_MODEL     0

#pragma mark Views
#define DEBUG_LOG_PRINTER_STATUS_VIEW       0
#define DEBUG_LOG_PRINT_JOB_GROUP_VIEW      0
#define DEBUG_LOG_PRINT_JOB_LAYOUT          0

#pragma mark ViewControllers
#define DEBUG_LOG_PRINTERS_SCREEN           0
#define DEBUG_LOG_ADD_PRINTER_SCREEN        0
#define DEBUG_LOG_PRINTER_SEARCH_SCREEN     0
#define DEBUG_LOG_PRINTER_INFO_SCREEN       0
#define DEBUG_LOG_PRINT_PREVIEW_SCREEN      0
#define DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN  0

#pragma mark Managers
#define DEBUG_LOG_PDF_MANAGER               0
#define DEBUG_LOG_DATABASE_MANAGER          0
#define DEBUG_LOG_PRINTER_MANAGER           0
#define DEBUG_LOG_SNMP_MANAGER              0
#define DEBUG_LOG_DIRECTPRINT_MANAGER       0

#pragma mark Helpers
#define DEBUG_LOG_PLIST_HELPER              0
#define DEBUG_LOG_PRINT_JOB_HISTORY_HELPER  0

#pragma mark OpenSource
#define DEBUG_LOG_REACHABILITY              0

#pragma mark - Debug Behavior Switches
#define DEBUG_SNMP_USE_FAKE_PRINTERS        0
#define DEBUG_SNMP_USE_TIMEOUT              0
#define DEBUG_PRINT_JOB_USE_TEST_DATA       0


#endif
