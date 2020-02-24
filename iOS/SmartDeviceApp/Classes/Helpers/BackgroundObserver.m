//
//  DeviceLockObserver.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//
#import "AddPrinterViewController.h"
#import "BackgroundObserver.h"
#import "NotificationNames.h"

#ifdef SDA_UNIT_TEST
#define STATIC
#else
#define STATIC static
#endif

STATIC char* const lockObserver = "SDALockObserver";

@interface BackgroundObserver ()

/**
 * Flag to indicated if the device lock observer is already observing
 */
@property (nonatomic, assign) BOOL isObserving;

@end

@implementation BackgroundObserver

+(BackgroundObserver *)sharedObserver
{
    static BackgroundObserver *sharedObserver = nil;
    
    if(sharedObserver == nil)
    {
        sharedObserver= [[BackgroundObserver alloc] init];
    }
    return sharedObserver;
}

- (void)startObserver
{
    if(self.isObserving)
    {
        return;
    }
    
    // Mantis 72278
    NSNotification *notification = [NSNotification notificationWithName:UIApplicationDidEnterBackgroundNotification object:self];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
    
    self.isObserving = YES;
}

- (void)stopObserver
{
    CFNotificationCenterRemoveEveryObserver(CFNotificationCenterGetDarwinNotifyCenter(), (void *)lockObserver);
    self.isObserving = NO;
}

/* Mantis 72278 未使用のためコメントアウト
- (void)notifyLockEvent
{
    [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_DEVICE_LOCK object:nil];
}

STATIC void lockStateChangedNotification(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo)
{
    [[DeviceLockObserver sharedObserver] notifyLockEvent];
}
*/
@end
