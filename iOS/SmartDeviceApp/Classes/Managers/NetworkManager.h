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
 Checks if the device is currently connected to the network.
 
 @return YES if connected, NO otherwise.
 */
+ (BOOL)isConnectedToNetwork;

@end
