//
//  printsettings.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#ifndef SmartDeviceApp_printsettings_h
#define SmartDeviceApp_printsettings_h

void create_pjl(char *pjl, char *settings);
// Ver.2.0.0.3 start
//void create_pjl_fw(char *pjl, char *settings, char *appName, char *appVersion);
//void create_pjl_gd(char *pjl, char *settings, char *appName, char *appVersion);
// Ver.2.0.4.2 Start
//void create_pjl_fw(char *pjl, char *settings, char *printerName, char *appVersion);
//void create_pjl_gd(char *pjl, char *settings, char *printerName, char *appVersion);
// Ver.2.0.4.2 End
// Ver.3.0.0.2 Start
void create_pjl_fw(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
void create_pjl_gd(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
void create_pjl_rag(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
// Ver.3.0.0.2 End
// Ver.4.0.0.0 Start
void create_pjl_lio(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
// Ver.4.0.0.0 End
// Ver.2.0.0.3 Start
//void create_pjl_fw(char *pjl, char *settings, char *printerName);
//void create_pjl_gd(char *pjl, char *settings, char *printerName);
// Ver.2.0.0.3 end
#endif
