//
//  SNMPManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "SNMPManager.h"
#import "NotificationNames.h"
#import "PrinterDetails.h"
#include "common.h"

#include "fff.h"
DEFINE_FFF_GLOBALS;

FAKE_VOID_FUNC(snmp_manual_search);
FAKE_VALUE_FUNC(snmp_context *, snmp_context_new, snmp_discovery_ended_callback, snmp_printer_added_callback);
FAKE_VOID_FUNC(snmp_context_free);
FAKE_VOID_FUNC(snmp_manual_discovery);
FAKE_VOID_FUNC(snmp_device_discovery);
FAKE_VOID_FUNC(snmp_cancel);

FAKE_VALUE_FUNC(snmp_device *, snmp_device_new);
FAKE_VOID_FUNC(snmp_device_free);
FAKE_VALUE_FUNC(int, snmp_device_get_capability_status);
FAKE_VALUE_FUNC(const char *, snmp_device_get_ip_address);
FAKE_VALUE_FUNC(const char *, snmp_device_get_name);

snmp_discovery_ended_callback mockEndCallback;
snmp_printer_added_callback mockAddedCallback;

snmp_context *mock_snmp_context_new(snmp_discovery_ended_callback endCallback, snmp_printer_added_callback addedCallback)
{
    mockEndCallback = endCallback;
    mockAddedCallback = addedCallback;
    return snmp_context_new_fake.return_val;
}

void mock_fail_snmp_manual_discovery()
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        mockEndCallback((snmp_context *)1, 0);
    });
}

void mock_success_snmp_manual_discovery()
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        mockAddedCallback((snmp_context *)1, (snmp_device *)1);
        mockEndCallback((snmp_context *)1, 1);
    });
}

void mock_fail_snmp_device_discovery()
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        mockEndCallback((snmp_context *)1, 0);
    });
}

void mock_success_snmp_device_discovery()
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        mockAddedCallback((snmp_context *)1, (snmp_device *)1);
        mockEndCallback((snmp_context *)1, 1);
    });
}

@interface SNMPManagerTest : GHAsyncTestCase
{
    SNMPManager* snmpManager;
    BOOL correctUseSNMPCommonLib;
    BOOL correctUseSNMPUnicastTimeout;
    BOOL notificationEndReceived;
    BOOL notificationAddReceived;
    BOOL printerFound;
}

@property (nonatomic, strong) PrinterDetails *testPrinterDetails;
@property (nonatomic, strong) id mockObserver;

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
    self.testPrinterDetails = [[PrinterDetails alloc] init];
    self.testPrinterDetails.ip = @"192.168.1.1";
    self.testPrinterDetails.name = @"RISO Printer 1";
}

// Run at end of all tests in the class
- (void)tearDownClass
{
}

// Run before each test method
- (void)setUp
{
    RESET_FAKE(snmp_manual_search);
    RESET_FAKE(snmp_context_new);
    RESET_FAKE(snmp_context_free);
    RESET_FAKE(snmp_manual_discovery);
    RESET_FAKE(snmp_device_discovery);
    RESET_FAKE(snmp_cancel);

    RESET_FAKE(snmp_device_new);
    RESET_FAKE(snmp_device_free);
    RESET_FAKE(snmp_device_get_capability_status);
    RESET_FAKE(snmp_device_get_ip_address);
    RESET_FAKE(snmp_device_get_name);
    
    FFF_RESET_HISTORY();
    
    snmp_context_new_fake.custom_fake = mock_snmp_context_new;
    snmp_context_new_fake.return_val = (snmp_context *)1;
}

// Run after each test method
- (void)tearDown
{
    if (self.mockObserver != nil)
    {
        [[NSNotificationCenter defaultCenter] removeObserver:self.self.mockObserver];
        self.mockObserver = nil;
    }
}

#pragma mark - Test Cases

- (void)testSharedManager
{
    // SUT
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Verification
    GHAssertNotNil(sharedSNMPManager, @"sharedSNMPManager should not be nil");
}

- (void)testSearchForPrinter_NotFound
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:snmpManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == NO);
    }]];
    
    snmp_manual_discovery_fake.custom_fake = mock_fail_snmp_manual_discovery;
    
    // SUT
    [sharedSNMPManager searchForPrinter:@"192.168.1.1"];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_manual_discovery_fake.call_count, 1, @"snmp_manual_discovery must be called.");
    GHAssertEquals((int)snmp_device_get_ip_address_fake.call_count, 0, @"snmp_device_get_ip_address must not be called.");
    GHAssertEquals((int)snmp_device_get_name_fake.call_count, 0, @"snmp_device_get_name must not be called.");
    GHAssertNoThrow([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testSearchForPrinter_Found
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_ADD object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_ADD object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        PrinterDetails *pd = [userInfo objectForKey:@"printerDetails"];
        if ([pd.ip compare:self.testPrinterDetails.ip] != NSOrderedSame)
        {
            return NO;
        }
        if ([pd.name compare:self.testPrinterDetails.name] != NSOrderedSame)
        {
            return NO;
        }
        return YES;
    }]];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:[SNMPManager sharedSNMPManager] userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == YES);
    }]];
    
    snmp_manual_discovery_fake.custom_fake = mock_success_snmp_manual_discovery;
    snmp_device_get_ip_address_fake.return_val = [self.testPrinterDetails.ip UTF8String];
    snmp_device_get_name_fake.return_val = [self.testPrinterDetails.name UTF8String];
    
    // SUT
    [sharedSNMPManager searchForPrinter:@"192.168.1.1"];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_manual_discovery_fake.call_count, 1, @"snmp_manual_discovery must be called.");
    GHAssertEquals((int)snmp_device_get_ip_address_fake.call_count, 2, @"snmp_device_get_ip_address must be called.");
    GHAssertEquals((int)snmp_device_get_name_fake.call_count, 1, @"snmp_device_get_name must be called.");
    GHAssertNoThrow([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testSearchForPrinter_Broadcast
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == YES);
    }]];
    
    snmp_manual_discovery_fake.custom_fake = mock_success_snmp_manual_discovery;
    snmp_device_get_ip_address_fake.return_val = "255.255.255.255";
    snmp_device_get_name_fake.return_val = [self.testPrinterDetails.name UTF8String];
    
    // SUT
    [sharedSNMPManager searchForPrinter:@"255.255.255.255"];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_manual_discovery_fake.call_count, 1, @"snmp_manual_discovery must be called.");
    GHAssertEquals((int)snmp_device_get_ip_address_fake.call_count, 1, @"snmp_device_get_ip_address must be called.");
    GHAssertEquals((int)snmp_device_get_name_fake.call_count, 0, @"snmp_device_get_name must not be called.");
    GHAssertNoThrow([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testSearchForAvailablePrinters_NotFound
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == NO);
    }]];
    
    snmp_device_discovery_fake.custom_fake = mock_fail_snmp_device_discovery;
    
    // SUT
    [sharedSNMPManager searchForAvailablePrinters];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_device_discovery_fake.call_count, 1, @"snmp_device_discovery must be called.");
    GHAssertEquals((int)snmp_device_get_ip_address_fake.call_count, 0, @"snmp_device_get_ip_address must not be called.");
    GHAssertEquals((int)snmp_device_get_name_fake.call_count, 0, @"snmp_device_get_name must not be called.");
    GHAssertNoThrow([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testSearchForAvailablePrinters_Found
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_ADD object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_ADD object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        PrinterDetails *pd = [userInfo objectForKey:@"printerDetails"];
        if ([pd.ip compare:self.testPrinterDetails.ip] != NSOrderedSame)
        {
            return NO;
        }
        if ([pd.name compare:self.testPrinterDetails.name] != NSOrderedSame)
        {
            return NO;
        }
        return YES;
    }]];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:sharedSNMPManager userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == YES);
    }]];
    
    snmp_device_discovery_fake.custom_fake = mock_success_snmp_device_discovery;
    snmp_device_get_ip_address_fake.return_val = [self.testPrinterDetails.ip UTF8String];
    snmp_device_get_name_fake.return_val = [self.testPrinterDetails.name UTF8String];
    
    // SUT
    [sharedSNMPManager searchForAvailablePrinters];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_device_discovery_fake.call_count, 1, @"snmp_device_discovery must be called.");
    GHAssertEquals((int)snmp_device_get_ip_address_fake.call_count, 2, @"snmp_device_get_ip_address must be called.");
    GHAssertEquals((int)snmp_device_get_name_fake.call_count, 1, @"snmp_device_get_name must be called.");
    GHAssertNoThrow([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testCancelSearch_Searching
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:[SNMPManager sharedSNMPManager] userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == YES);
    }]];
    
    // SUT
    [sharedSNMPManager searchForAvailablePrinters];
    [sharedSNMPManager cancelSearch];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_cancel_fake.call_count, 1, @"snmp_cancel must be called.");
    GHAssertThrows([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

- (void)testCancelSearch_NotSearching
{
    SNMPManager *sharedSNMPManager = [SNMPManager sharedSNMPManager];
    
    // Mock
    self.mockObserver = [OCMockObject observerMock];
    [[NSNotificationCenter defaultCenter] addMockObserver:self.mockObserver name:NOTIF_SNMP_END object:sharedSNMPManager];
    [[self.mockObserver expect] notificationWithName:NOTIF_SNMP_END object:[SNMPManager sharedSNMPManager] userInfo:[OCMArg checkWithBlock:^BOOL(NSDictionary *userInfo){
        NSNumber *result = [userInfo objectForKey:@"result"];
        return ([result boolValue] == YES);
    }]];
    
    // SUT
    [sharedSNMPManager cancelSearch];
    
    // Verification
    [NSThread sleepForTimeInterval:1];
    GHAssertEquals((int)snmp_cancel_fake.call_count, 0, @"snmp_cancel must not be called.");
    GHAssertThrows([self.mockObserver verify], @"");
    [[NSNotificationCenter defaultCenter] removeObserver:self.mockObserver];
}

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

/*- (void)test001_SearchForPrinter
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
}*/

@end
