//
//  NetworkManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NetworkManager : NSObject

/**
 Checks if the device is currently connected to the local Wi-Fi.
 
 @return YES if connected, NO otherwise.
 */
+ (BOOL)isConnectedToLocalWifi;

@end
