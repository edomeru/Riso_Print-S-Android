//
//  DeviceLockObserverTest.m
//  RISOSmartPrint
//
//  Created by Mobile OS on 26/10/2016.
//  Copyright © 2016 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DeviceLockObserver.h"
#import "NotificationNames.h"
#import "OCMock.h"
#include "fff.h"
DEFINE_FFF_GLOBALS;

extern void lockStateChangedNotification(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo);

extern char* const lockObserver;

FAKE_VOID_FUNC2(CFNotificationCenterRemoveEveryObserver, CFNotificationCenterRef, const void *);
FAKE_VOID_FUNC6(CFNotificationCenterAddObserver, CFNotificationCenterRef, const void *, CFNotificationCallback, CFStringRef, const void *, CFNotificationSuspensionBehavior);


@interface DeviceLockObserver(Test)

@property BOOL isObserving;

- (void)notifyLockEvent;

@end

@interface DeviceLockObserverTest : GHTestCase

@property (assign) BOOL receivedNotification;

@end

@implementation DeviceLockObserverTest

- (void)setUp
{
    self.receivedNotification = YES;
    
    RESET_FAKE(CFNotificationCenterRemoveEveryObserver);
    RESET_FAKE(CFNotificationCenterAddObserver);
    
    FFF_RESET_HISTORY();
}

- (void)testSharedObserverInstance
{
    DeviceLockObserver *instance = [DeviceLockObserver sharedObserver];
    
    GHAssertNotNil(instance, @"");
    GHAssertEqualObjects(instance, [DeviceLockObserver sharedObserver], @"");
}


- (void)testStartStopObserver
{
    DeviceLockObserver *instance = [DeviceLockObserver sharedObserver];
    [instance startObserver];
    
    GHAssertTrue(instance.isObserving, @"");
    GHAssertTrue(CFNotificationCenterAddObserver_fake.call_count == 1, @"");
    
    [instance startObserver];
    
    GHAssertTrue(instance.isObserving, @"");
    GHAssertTrue(CFNotificationCenterAddObserver_fake.call_count == 1, @"");
    
    [instance stopObserver];
    
    GHAssertFalse(instance.isObserving, @"");
    GHAssertTrue(CFNotificationCenterRemoveEveryObserver_fake.call_count == 1, @"");
}


- (void)testNotifyObserver
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector (deviceLockEventDidNotify)
                                                 name: NOTIF_DEVICE_LOCK object:nil];
    
    [[DeviceLockObserver sharedObserver] notifyLockEvent];
    
    [NSThread sleepForTimeInterval:1];
    
    GHAssertTrue(self.receivedNotification, @"");
    
}

- (void)testLockNotify
{
    id mockDeviceLockObserver = OCMClassMock([DeviceLockObserver class]);
    [[[[mockDeviceLockObserver stub] andReturn:mockDeviceLockObserver] classMethod] sharedObserver];
    [[mockDeviceLockObserver expect] notifyLockEvent];
    
    lockStateChangedNotification(CFNotificationCenterGetDarwinNotifyCenter(), lockObserver, CFSTR("com.apple.springboard.lockstate"), NULL, NULL);
    
    [mockDeviceLockObserver verify];
    
    [mockDeviceLockObserver stopMocking];
}

- (void)deviceLockEventDidNotify
{
    self.receivedNotification = YES;
}

@end
