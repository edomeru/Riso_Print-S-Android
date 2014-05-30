//
//  PListHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PListHelper.h"

@interface PListHelperTest : GHTestCase
{
}

@end

@implementation PListHelperTest

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

- (void)test001_ReadUInt
{
    GHTestLog(@"# CHECK: The UINT settings are correct. #");
    // reads the UINT settings from SmartDeviceAppTests/PropertyList/SmartDeviceApp-Settings.plist
    
    GHTestLog(@"-- reading invalid");
    NSUInteger defaultValue = [PListHelper readUint:99];
    GHAssertTrue(defaultValue == 0, @"");
    
    GHTestLog(@"-- reading [Max Printer Count]");
    NSUInteger actualMaxPrinterCount = [PListHelper readUint:kPlistUintValMaxPrinters];
    GHAssertTrue(actualMaxPrinterCount == 10, @"");
    
    GHTestLog(@"-- reading [Max Print Job Per Printer Count]");
    NSUInteger actualMaxPrintJobCount = [PListHelper readUint:kPlistUintValMaxPrintJobsPerPrinter];
    GHAssertTrue(actualMaxPrintJobCount == 10, @"");
}

- (void)test002_ReadBool
{
    GHTestLog(@"# CHECK: The BOOL settings are correct. #");
    // reads the BOOL settings from SmartDeviceAppTests/PropertyList/SmartDeviceApp-Settings.plist
    
    GHTestLog(@"-- reading invalid");
    NSUInteger defaultValue = [PListHelper readBool:99];
    GHAssertFalse(defaultValue, @"");
    
    GHTestLog(@"-- reading [Use Print Job History Test Data]");
    GHAssertTrue([PListHelper readBool:kPlistBoolValUsePrintJobTestData], @"");
}

@end
