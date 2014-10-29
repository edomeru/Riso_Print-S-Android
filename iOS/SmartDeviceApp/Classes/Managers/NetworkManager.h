//
//  NetworkManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * Handler for all network connectivity operations.
 * This class provides the interface to the Reachability open-source library.\n
 * This class is not required to be instantiated to be used, since its methods are declared as static.
 */
@interface NetworkManager : NSObject

/**
 * Checks if the device is currently connected to the local Wi-Fi.
 *
 * @return YES if connected, NO otherwise
 */
+ (BOOL)isConnectedToLocalWifi;

@end
