//
//  PListHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PListHelper.h"
#import "OCMock.h"

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

- (void)testReadUint
{
    GHTestLog(@"# CHECK: The UINT settings are correct. #");
    
    GHTestLog(@"-- reading invalid");
    NSUInteger defaultValue = [PListHelper readUint:99];
    GHAssertTrue(defaultValue == 0, @"");
    
    GHTestLog(@"-- reading [Max Printer Count]");
    NSUInteger actualMaxPrinterCount = [PListHelper readUint:kPlistUintValMaxPrinters];
    GHAssertTrue(actualMaxPrinterCount == 10, @"");
    
    GHTestLog(@"-- reading [Max Print Job Per Printer Count]");
    NSUInteger actualMaxPrintJobCount = [PListHelper readUint:kPlistUintValMaxPrintJobsPerPrinter];
    GHAssertTrue(actualMaxPrintJobCount == 100, @"");

    GHTestLog(@"-- reading [Max Print Job Number]");
    NSUInteger actualMaxPrintJobNumber = [PListHelper readUint:kPlistUintValMaxJobNum];
    GHAssertTrue(actualMaxPrintJobNumber == 999, @"");

    GHTestLog(@"-- reading [Next Print Job Number]");
    NSUInteger actualNextPrintJobNumber = [PListHelper readUint:kPlistUintValNextJobNum];
    GHAssertTrue(actualNextPrintJobNumber >= 0 && actualNextPrintJobNumber <= 999, @"");
}

- (void)testSetUint
{
    GHTestLog(@"# CHECK: The UINT settings are correct. #");
    
    GHTestLog(@"-- setting invalid");
    [PListHelper setUint:0 forType:99];
    NSUInteger defaultValue = [PListHelper readUint:99];
    GHAssertTrue(defaultValue == 0, @"");
    
    GHTestLog(@"-- setting [Max Printer Count]");
    [PListHelper setUint:0 forType:kPlistUintValMaxPrinters];
    NSUInteger actualMaxPrinterCount = [PListHelper readUint:kPlistUintValMaxPrinters];
    GHAssertTrue(actualMaxPrinterCount == 10, @"");
    
    GHTestLog(@"-- setting [Max Print Job Per Printer Count]");
    [PListHelper setUint:0 forType:kPlistUintValMaxPrintJobsPerPrinter];
    NSUInteger actualMaxPrintJobCount = [PListHelper readUint:kPlistUintValMaxPrintJobsPerPrinter];
    GHAssertTrue(actualMaxPrintJobCount == 100, @"");

    GHTestLog(@"-- setting [Max Print Job Number]");
    [PListHelper setUint:0 forType:kPlistUintValMaxJobNum];
    NSUInteger actualMaxPrintJobNumber = [PListHelper readUint:kPlistUintValMaxJobNum];
    GHAssertTrue(actualMaxPrintJobNumber == 999, @"");

    GHTestLog(@"-- setting [Next Print Job Number]");
    [PListHelper setUint:2 forType:kPlistUintValNextJobNum];
    NSUInteger actualNextPrintJobNumber = [PListHelper readUint:kPlistUintValNextJobNum];
    GHAssertTrue(actualNextPrintJobNumber == 2, @"");
    [PListHelper setUint:1 forType:kPlistUintValNextJobNum];
    actualNextPrintJobNumber = [PListHelper readUint:kPlistUintValNextJobNum];
    GHAssertTrue(actualNextPrintJobNumber == 1, @"");
}

- (void)testSettingsPath
{
    NSUInteger actualNextPrintJobNumber = [PListHelper readUint:kPlistUintValNextJobNum];

    NSBundle* mainBundle = [NSBundle mainBundle];
    id mockBundle = [OCMockObject partialMockForObject:mainBundle];
    [[[mockBundle stub] andReturn:nil] pathForResource:@"SmartDeviceApp-Settings" ofType:@"plist"];
    
    GHAssertNoThrow({
        [PListHelper setUint:1 forType:kPlistUintValNextJobNum];
    }, @"");

    [mockBundle stopMocking];
    [PListHelper setUint:actualNextPrintJobNumber forType:kPlistUintValNextJobNum];
}

@end
