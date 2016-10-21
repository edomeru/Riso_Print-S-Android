//
//  DeviceLockObserver.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DeviceLockObserver.h"
#import "NotificationNames.h"

static char* const lockObserver = "SDALockObserver";

@interface DeviceLockObserver ()

/**
 * Flag to indicated if the device lock observer is already observing
 */
@property (nonatomic, assign) BOOL isObserving;

@end

@implementation DeviceLockObserver

+(DeviceLockObserver *)sharedObserver
{
    static DeviceLockObserver *sharedObserver = nil;
    
    if(sharedObserver == nil)
    {
        sharedObserver= [[DeviceLockObserver alloc] init];
    }
    return sharedObserver;
}

- (void)startObserver
{
    if(self.isObserving)
    {
        return;
    }
    
    CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), (void *)lockObserver, lockStateChangedNotification,
                                    CFSTR("com.apple.springboard.lockstate"), NULL, CFNotificationSuspensionBehaviorDeliverImmediately);
    
    self.isObserving = YES;
}

- (void)stopObserver
{
    CFNotificationCenterRemoveEveryObserver(CFNotificationCenterGetDarwinNotifyCenter(), (void *)lockObserver);
    self.isObserving = NO;
}

- (void)notifyLockEvent
{
    [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_DEVICE_LOCK object:nil];
}

static void lockStateChangedNotification(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo)
{
    [[DeviceLockObserver sharedObserver] notifyLockEvent];
}
@end
