//
//  PrinterStatusHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrinterStatusHelper.h"

static NSString* TEST_PRINTER_IP = @"192.168.0.199";

@interface PrinterStatusHelperTest : GHTestCase
{
}

@end

@implementation PrinterStatusHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return NO;
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
    
    PrinterStatusHelper* psh = [[PrinterStatusHelper alloc] initWithPrinterIP:TEST_PRINTER_IP];
    GHAssertNotNil(psh, @"check initialization of PrinterStatusHelper");
    GHAssertFalse([psh isPolling], @"should not be polling");
    GHAssertEqualStrings(psh.ipAddress, TEST_PRINTER_IP, @"");
}

- (void)test002_StartStop
{
    GHTestLog(@"# CHECK: PSHelper can be started/stopped. #");
    
    PrinterStatusHelper* psh = [[PrinterStatusHelper alloc] initWithPrinterIP:TEST_PRINTER_IP];
    GHAssertNotNil(psh, @"check initialization of PrinterStatusHelper");

    GHAssertFalse([psh isPolling], @"should not be polling");
    [psh startPrinterStatusPolling];
    GHAssertTrue([psh isPolling], @"should now be polling");
    [psh stopPrinterStatusPolling];
    GHAssertFalse([psh isPolling], @"should not be polling");
}

@end
