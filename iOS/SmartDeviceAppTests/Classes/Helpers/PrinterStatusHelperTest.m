//
//  PrinterStatusHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrinterStatusHelper.h"

//use an online printer IP
//static NSString* TEST_PRINTER_IP = @"192.168.0.198";
static NSString* TEST_PRINTER_IP = @"192.168.0.199";

@interface PrinterStatusHelperTest : GHTestCase <PrinterStatusHelperDelegate>
{
    BOOL statusDidChangeCallbackReceived;
    BOOL onlineStatus;
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
}

// Run after each test method
- (void)tearDown
{
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_Initialization
{
    GHTestLog(@"# CHECK: PSHelper can be initialized. #");
    
    GHTestLog(@"-- creating the helper");
    PrinterStatusHelper* psh = [[PrinterStatusHelper alloc] initWithPrinterIP:TEST_PRINTER_IP];
    GHAssertNotNil(psh, @"check initialization of PrinterStatusHelper");
    GHAssertFalse([psh isPolling], @"should not be polling");
    GHAssertEqualStrings(psh.ipAddress, TEST_PRINTER_IP, @"");
}

- (void)test002_StartStop
{
    GHTestLog(@"# CHECK: PSHelper can be started/stopped. #");
    float POLL_TIMEOUT = 5;
    NSString* msg;
    
    GHTestLog(@"-- creating the helper");
    PrinterStatusHelper* psh = [[PrinterStatusHelper alloc] initWithPrinterIP:TEST_PRINTER_IP];
    GHAssertNotNil(psh, @"check initialization of PrinterStatusHelper");
    GHAssertFalse([psh isPolling], @"should not be polling");
    psh.delegate = self;
    
    GHTestLog(@"-- starting status poller");
    statusDidChangeCallbackReceived = NO;
    [psh startPrinterStatusPolling];
    
    msg = [NSString stringWithFormat: @"wait for %.2f seconds for printer status polling to start", POLL_TIMEOUT];
    [self waitForCompletion:POLL_TIMEOUT withMessage:msg];
    GHAssertTrue([psh isPolling], @"should now be polling");
    
    GHTestLog(@"-- waiting for status change callback");
    msg = [NSString stringWithFormat: @"wait for %.2f seconds while waiting for the polling callback", POLL_TIMEOUT];
    [self waitForCompletion:POLL_TIMEOUT withMessage:msg];
    GHAssertTrue(statusDidChangeCallbackReceived, @"");
    GHAssertTrue(onlineStatus, [NSString stringWithFormat:@"check if printer=[%@] is online", TEST_PRINTER_IP]);
    
    GHTestLog(@"-- stopping status poller");
    [psh stopPrinterStatusPolling];
    GHAssertFalse([psh isPolling], @"should not be polling");
}

#pragma mark - PrinterStatusHelperDelegate Methods

- (void)statusDidChange:(BOOL)isOnline
{
    statusDidChangeCallbackReceived = YES;
    onlineStatus = isOnline;
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Printer Status Helper Test"
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
