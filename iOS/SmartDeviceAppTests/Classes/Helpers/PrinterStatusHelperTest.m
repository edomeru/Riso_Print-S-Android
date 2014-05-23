//
//  PrinterStatusHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PrinterStatusHelper.h"
#import "SimplePing.h"

@interface PrinterStatusHelperTest : GHAsyncTestCase <PrinterStatusHelperDelegate>
{
}

@end

@implementation PrinterStatusHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
}

// Run at end of all tests in the class
- (void)tearDownClass
{
}

// Run before each test method
- (void)setUp
{
    [self prepare];
}

// Run after each test method
- (void)tearDown
{
}

#pragma mark - Test Cases

- (void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange:(BOOL)isOnline
{
    if (isOnline)
    {
        [self notify:kGHUnitWaitStatusSuccess];
    }
    else
    {
        [self notify:kGHUnitWaitStatusFailure];
    }
}

- (void)testInitWithPrinterIp
{
    // SUT
    NSString *ip = @"192.168.1.1";
    PrinterStatusHelper *statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:ip];
    
    // Verification
    GHAssertEqualStrings(statusHelper.ipAddress, ip, @"IP must match.");
}

- (void)testPolling_OK
{
    // Mock
    NSData *data = [@"192.168.1.1" dataUsingEncoding:NSUTF8StringEncoding];
    id mockSimplePing = [OCMockObject mockForClass:[SimplePing class]];
    [[[mockSimplePing stub] andReturn:mockSimplePing] simplePingWithHostName:OCMOCK_ANY];
    __block id<SimplePingDelegate> delegate;
    [[[mockSimplePing stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&delegate atIndex:2];
    }] setDelegate:OCMOCK_ANY];
    [(SimplePing *)[[mockSimplePing expect] andDo:^(NSInvocation *invocation){
        NSLog(@"Starting");
        [delegate simplePing:invocation.target didStartWithAddress:data];
        [delegate simplePing:invocation.target didSendPacket:data];
        [delegate simplePing:invocation.target didReceivePingResponsePacket:data];
    }] start];
    [[mockSimplePing stub] sendPingWithData:nil];
    [[mockSimplePing stub] stop];

    // SUT
    NSString *ip = @"192.168.1.1";
    PrinterStatusHelper *statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:ip];
    statusHelper.delegate = self;
    [statusHelper startPrinterStatusPolling];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:6];
    [statusHelper stopPrinterStatusPolling];
    GHAssertNoThrow([mockSimplePing verify], @"");
    [mockSimplePing stopMocking];
}

- (void)testPolling_NG
{
    // Mock
    NSData *data = [@"192.168.1.1" dataUsingEncoding:NSUTF8StringEncoding];
    id mockSimplePing = [OCMockObject mockForClass:[SimplePing class]];
    [[[mockSimplePing stub] andReturn:mockSimplePing] simplePingWithHostName:OCMOCK_ANY];
    __block id<SimplePingDelegate> delegate;
    [[[mockSimplePing stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&delegate atIndex:2];
    }] setDelegate:OCMOCK_ANY];
    [(SimplePing *)[[mockSimplePing expect] andDo:^(NSInvocation *invocation){
        NSLog(@"Starting");
        [delegate simplePing:invocation.target didStartWithAddress:data];
        [delegate simplePing:invocation.target didFailWithError:nil];
        [delegate simplePing:invocation.target didFailToSendPacket:nil error:nil];
    }] start];
    [[mockSimplePing stub] sendPingWithData:nil];
    [[mockSimplePing stub] stop];

    // SUT
    NSString *ip = @"192.168.1.1";
    PrinterStatusHelper *statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:ip];
    statusHelper.delegate = self;
    [statusHelper startPrinterStatusPolling];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusFailure timeout:6];
    [statusHelper stopPrinterStatusPolling];
    GHAssertNoThrow([mockSimplePing verify], @"");
    [mockSimplePing stopMocking];
}

- (void)testPolling_BG
{
    // Mock
    NSData *data = [@"192.168.1.1" dataUsingEncoding:NSUTF8StringEncoding];
    id mockSimplePing = [OCMockObject mockForClass:[SimplePing class]];
    [[[mockSimplePing stub] andReturn:mockSimplePing] simplePingWithHostName:OCMOCK_ANY];
    __block id<SimplePingDelegate> delegate;
    [[[mockSimplePing stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&delegate atIndex:2];
    }] setDelegate:OCMOCK_ANY];
    __block BOOL isFirst = YES;
    [(SimplePing *)[[mockSimplePing stub] andDo:^(NSInvocation *invocation){
        NSLog(@"Starting");
        [delegate simplePing:invocation.target didStartWithAddress:data];
        if (isFirst)
        {
            isFirst = NO;
            [[NSNotificationCenter defaultCenter] postNotificationName:UIApplicationDidEnterBackgroundNotification object:nil];
            [NSThread sleepForTimeInterval:1];
            [[NSNotificationCenter defaultCenter] postNotificationName:UIApplicationWillEnterForegroundNotification object:nil];
        } else
        {
            [delegate simplePing:invocation.target didReceivePingResponsePacket:data];
        }
    }] start];
    [[mockSimplePing stub] sendPingWithData:nil];
    [[mockSimplePing stub] stop];

    // SUT
    NSString *ip = @"192.168.1.1";
    PrinterStatusHelper *statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:ip];
    statusHelper.delegate = self;
    [statusHelper startPrinterStatusPolling];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:10];
    [statusHelper stopPrinterStatusPolling];
    GHAssertNoThrow([mockSimplePing verify], @"");
    [mockSimplePing stopMocking];
}

@end
