//
//  SNMPManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "SNMPManager.h"
#import "NotificationNames.h"
#import "PrinterDetails.h"

const float SNMPM_SEARCH_TIMEOUT = 10;

@interface SNMPManager (UnitTest)

// expose private properties
- (BOOL)useSNMPCommonLib;
- (BOOL)useSNMPUnicastTimeout;

// expose private methods
- (void)setUseSNMPCommonLib:(BOOL)setting;
- (void)setUseSNMPUnicastTimeout:(BOOL)setting;

@end

@interface SNMPManagerTest : GHTestCase
{
    SNMPManager* snmpManager;
    BOOL correctUseSNMPCommonLib;
    BOOL correctUseSNMPUnicastTimeout;
    BOOL notificationEndReceived;
    BOOL notificationAddReceived;
    BOOL printerFound;
}

@end

@implementation SNMPManagerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    snmpManager = [SNMPManager sharedSNMPManager];
    GHAssertNotNil(snmpManager, @"check initialization of SNMPManager");
    correctUseSNMPCommonLib = [snmpManager useSNMPCommonLib];
    correctUseSNMPUnicastTimeout = [snmpManager useSNMPUnicastTimeout];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(snmpManagerDidNotifyEnd:)
                                                 name:NOTIF_SNMP_END
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(snmpManagerDidNotifyAdd:)
                                                 name:NOTIF_SNMP_ADD
                                               object:nil];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    //revert override
    [snmpManager setUseSNMPCommonLib:correctUseSNMPCommonLib];
    [snmpManager setUseSNMPUnicastTimeout:correctUseSNMPUnicastTimeout];
}

// Run before each test method
- (void)setUp
{
}

// Run after each test method
- (void)tearDown
{
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_SearchForPrinter
{
    GHTestLog(@"# CHECK: SNMPM can initiate Manual Search. #");
    NSString* msg = [NSString stringWithFormat:
                     @"wait for %.2f seconds after initiating manual search", SNMPM_SEARCH_TIMEOUT];
    
    notificationEndReceived = NO;
    notificationAddReceived = NO;
    printerFound = NO;
    [snmpManager searchForPrinter:@"192.168.0.197"];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT+1 withMessage:msg];
    GHAssertTrue(notificationEndReceived, @"");
}

- (void)test002_SearchForAvailablePrinters
{
    GHTestLog(@"# CHECK: SNMPM can initiate Device Discovery. #");
    NSString* msg = [NSString stringWithFormat:
                     @"wait for %.2f seconds after initiating device discovery", SNMPM_SEARCH_TIMEOUT];
    
    notificationEndReceived = NO;
    notificationAddReceived = NO;
    printerFound = NO;
    [snmpManager searchForAvailablePrinters];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT+1 withMessage:msg];
    GHAssertTrue(notificationEndReceived, @"");
}

- (void)test003_CancelSearch
{
    GHTestLog(@"# CHECK: SNMPM can cancel search. #");
    NSString* msg = [NSString stringWithFormat:
                     @"wait for %.2f seconds after stopping search", SNMPM_SEARCH_TIMEOUT];
    
    notificationEndReceived = NO;
    [snmpManager searchForPrinter:@"192.168.0.1"];
    [self waitForCompletion:2 withMessage:msg];
    [snmpManager cancelSearch];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT withMessage:msg];
    GHAssertFalse(notificationEndReceived, @"");
}

- (void)test004_Singleton
{
    GHTestLog(@"# CHECK: SNPM is indeed a singleton. #");
    
    SNMPManager* snmpmNew = [SNMPManager sharedSNMPManager];
    GHAssertEqualObjects(snmpManager, snmpmNew, @"should return the same object");
}

- (void)test005_FakeSearchForPrinter
{
    GHTestLog(@"# CHECK: SNPM's Fake Manual Search. #");
    NSString* msg = [NSString stringWithFormat:
                     @"wait for %.2f seconds while making fake manual search", SNMPM_SEARCH_TIMEOUT];
    
    //override setting
    [snmpManager setUseSNMPCommonLib:NO]; //use the fake search
    [snmpManager setUseSNMPUnicastTimeout:NO];
    
    notificationEndReceived = NO;
    notificationAddReceived = NO;
    printerFound = NO;
    [snmpManager searchForPrinter:@"192.168.0.1"];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT+1 withMessage:msg];
    GHAssertTrue(notificationEndReceived, @"");
    GHAssertTrue(notificationAddReceived, @"");
    GHAssertTrue(printerFound, @"");
    
    //override setting
    [snmpManager setUseSNMPCommonLib:NO]; //use the fake search
    [snmpManager setUseSNMPUnicastTimeout:YES];
    
    notificationEndReceived = NO;
    notificationAddReceived = NO;
    printerFound = NO;
    [snmpManager searchForPrinter:@"192.168.0.1"];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT+1 withMessage:msg];
    GHAssertTrue(notificationEndReceived, @"");
    GHAssertFalse(notificationAddReceived, @"");
    GHAssertFalse(printerFound, @"");
}

- (void)test006_FakeSearchForAvailablePrinters
{
    GHTestLog(@"# CHECK: SNPM's Fake Device Discovery. #");
    NSString* msg = [NSString stringWithFormat:
                     @"wait for %.2f seconds while making fake device discovery", SNMPM_SEARCH_TIMEOUT];
    
    //override setting
    [snmpManager setUseSNMPCommonLib:NO]; //use the fake search
    
    notificationEndReceived = NO;
    notificationAddReceived = NO;
    printerFound = NO;
    [snmpManager searchForAvailablePrinters];
    [self waitForCompletion:SNMPM_SEARCH_TIMEOUT+1 withMessage:msg];
    GHAssertTrue(notificationEndReceived, @"");
    GHAssertTrue(notificationAddReceived, @"");
    GHAssertTrue(printerFound, @"");
}

#pragma mark - Notification Callbacks

- (void)snmpManagerDidNotifyEnd:(NSNotification*)notif
{
    notificationEndReceived = YES;
    
    printerFound = [(NSNumber*)[notif object] boolValue];
}

- (void)snmpManagerDidNotifyAdd:(NSNotification*)notif
{
    notificationAddReceived = YES;
    
    PrinterDetails* pd = (PrinterDetails*)[notif object];
    GHAssertNotNil(pd, @"");
    GHAssertNotNil(pd.ip, @"");
    GHAssertFalse(pd.enFinisher23Holes && pd.enFinisher24Holes, @"");
    GHAssertTrue([pd.port intValue] == 0 || [pd.port intValue] == 1, @"");
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"SNMP Manager Test"
                                                    message:msg
                                                   delegate:self
                                          cancelButtonTitle:@"HIDE"
                                          otherButtonTitles:nil];
    [alert show];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    [alert dismissWithClickedButtonIndex:0 animated:YES];
    
    return done;
}

@end
