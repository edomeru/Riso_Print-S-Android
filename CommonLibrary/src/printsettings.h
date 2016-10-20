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
void create_pjl_fw(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
void create_pjl_gd(char *pjl, char *settings, char *printerName, char *hostName, char *appVersion);
#endif
