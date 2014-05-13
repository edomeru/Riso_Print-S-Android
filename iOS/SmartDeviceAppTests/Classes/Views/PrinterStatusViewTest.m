//
//  PrinterStatusViewTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrinterStatusView.h"

@interface PrinterStatusViewTest : GHTestCase
{
}

@end

@implementation PrinterStatusViewTest

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

- (void)test001_SetOnlineStatus
{
    PrinterStatusView* statusView = [[PrinterStatusView alloc] init];
    GHAssertNotNil(statusView, @"");
    GHAssertFalse(statusView.onlineStatus, @"");
    
    [statusView setStatus:YES];
    GHAssertTrue(statusView.onlineStatus, @"");
    
    [statusView setStatus:YES]; //repeat for coverage
    GHAssertTrue(statusView.onlineStatus, @"");
    
    [statusView setStatus:NO];
    GHAssertFalse(statusView.onlineStatus, @"");
}

- (void)test002_StatusHelperDelegate
{
    PrinterStatusView* statusView = [[PrinterStatusView alloc] init];
    GHAssertNotNil(statusView, @"");
    NSString* printerIP = @"192.168.0.197";

    statusView.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printerIP];
    statusView.statusHelper.delegate = statusView;
    
    [statusView.statusHelper startPrinterStatusPolling];
    [self waitForCompletion:5+1];
    [statusView.statusHelper stopPrinterStatusPolling];
    GHAssertNotNil(statusView.statusHelper, @"");
    GHAssertFalse([statusView.statusHelper isPolling], @"");
    
    [statusView.statusHelper startPrinterStatusPolling];
    [self waitForCompletion:5+1];
    [statusView.statusHelper stopPrinterStatusPolling];
    GHAssertNotNil(statusView.statusHelper, @"");
    GHAssertFalse([statusView.statusHelper isPolling], @"");
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    return done;
}

@end
