//
//  PListHelperTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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
    
    GHTestLog(@"-- reading [Max Printer Count]");
    NSUInteger actualMaxPrinterCount = [PListHelper readUint:kPlistUintValMaxPrinters];
    GHAssertEquals(actualMaxPrinterCount, (NSUInteger)20, @"");
}

- (void)test002_ReadBool
{
    GHTestLog(@"# CHECK: The BOOL settings are correct. #");
    
    GHTestLog(@"-- reading [Use SNMP Common Lib]");
    BOOL useSNMPCommonLib = [PListHelper readBool:kPlistBoolValUseSNMP];
    GHAssertTrue(useSNMPCommonLib, @"");
    
    GHTestLog(@"-- reading [Use SNMP Timeout]");
    BOOL useSNMPTimeout = [PListHelper readBool:kPlistBoolValUseSNMPTimeout];
    GHAssertFalse(useSNMPTimeout, @"");
}

- (void)test003_ReadDefaultSettings
{
    GHTestLog(@"# CHECK: The Default Print Settings are correct. #");
    
    GHTestLog(@"-- reading [Default Print Settings]");
    NSDictionary* actualDefaultPrintSettings = [PListHelper readDefaultPrintSettings];
    
    GHTestLog(@"-- reading Bind");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Bind"] intValue], 0, nil);
    
    GHTestLog(@"-- reading BookletBinding");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"BookletBinding"] intValue], 0, nil);
    
    GHTestLog(@"-- reading BookletTray");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"BookletTray"] intValue], 0, nil);
    
    GHTestLog(@"-- reading CatchTray");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"CatchTray"] intValue], 0, nil);
    
    GHTestLog(@"-- reading ColorMode");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"ColorMode"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Copies");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Copies"] intValue], 1, nil);
    
    GHTestLog(@"-- reading Duplex");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Duplex"] intValue], 0, nil);
    
    GHTestLog(@"-- reading ImageQuality");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"ImageQuality"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Pagination");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Pagination"] intValue], 0, nil);
    
    GHTestLog(@"-- reading PaperSize");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"PaperSize"] intValue], 0, nil);
    
    GHTestLog(@"-- reading PaperType");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"PaperType"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Punch");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Punch"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Sort");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Sort"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Staple");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Staple"] intValue], 0, nil);
    
    GHTestLog(@"-- reading Zoom");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"Zoom"] intValue], 0, nil);
    
    GHTestLog(@"-- reading ZoomRate");
    GHAssertEquals([[actualDefaultPrintSettings valueForKey:@"ZoomRate"] intValue], 100, nil);
}

@end
