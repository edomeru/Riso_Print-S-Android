//
//  DeviceLockObserver.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//
#import <Foundation/Foundation.h>


@interface DeviceLockObserver : NSObject

/**
 * Return the shared instance of the device lock observer
 */
+ (DeviceLockObserver *)sharedObserver;

/**
 * Start device lock observer
 */
- (void)startObserver;

/**
 * Stop device lock observer
 */
- (void)stopObserver;

@end
