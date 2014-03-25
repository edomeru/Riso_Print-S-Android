//
//  NetworkManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NetworkManager : NSObject

/**
 Checks if the device is currently connected to the local Wi-Fi.
 
 @return YES if connected, NO otherwise.
 */
+ (BOOL)isConnectedToLocalWifi;

@end
