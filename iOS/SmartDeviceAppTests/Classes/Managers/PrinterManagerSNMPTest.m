//
//  PrinterManagerSNMPTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/8/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"

@interface PrinterManagerSNMPTest : GHTestCase
{
    PrinterManager* printerManager;
}

@end

@implementation PrinterManagerSNMPTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return NO;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    printerManager = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(printerManager, nil);
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

- (void)test001_SearchForOnePrinter
{
    GHTestLog(@"# CHECK: PM can initiate SNMP Manual Search. #");
    
    GHFail(@"<<UNIT TEST NOT IMPLEMENTED>>");
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test002_SearchForAllPrinters
{
    GHTestLog(@"# CHECK: PM can initiate SNMP Device Discovery. #");
    
    GHFail(@"<<UNIT TEST NOT IMPLEMENTED>>");
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

@end
