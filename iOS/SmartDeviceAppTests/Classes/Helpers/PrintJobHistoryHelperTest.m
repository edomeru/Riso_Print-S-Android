//
//  PrintJobHistoryHelperTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintJobHistoryHelper.h"
#import "PrintJobHistoryGroup.h"
#import "PrinterManager.h"
#import "PListHelper.h"

@interface PrintJobHistoryHelperTest : GHTestCase
{
    BOOL usingTestData;
}

@end

@implementation PrintJobHistoryHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    // check setting
    usingTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
    GHAssertTrue(usingTestData, @"enable print job history test data");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    // remove all test data
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
    {
        if (![pm deletePrinterAtIndex:0])
            break;
    }
    if (pm.countSavedPrinters != 0)
    {
        GHFail(@"could not delete all test printers from DB");
        return;
    }
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

- (void)test001_PreparePrintJobHistoryGroups
{
    GHTestLog(@"# CHECK: PJHHelper can get PJHGroups. #");
    GHTestLog(@"-- usingTestData = %@", usingTestData ? @"YES" : @"NO");
    
    // TEST DATA CONSTANTS
    // (copy from PrintJobHistoryHelper)
    const NSUInteger TEST_NUM_PRINTERS = 8;
    
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");

    GHTestLog(@"-- checking number of groups");
    NSUInteger countPrintJobHistoryGroups = [listPrintJobHistoryGroups count];
    GHTestLog(@"-- #groups = %lu", (unsigned long)countPrintJobHistoryGroups);

    GHAssertTrue(countPrintJobHistoryGroups == TEST_NUM_PRINTERS, @"should be equal to # of test printers");
    
    for (NSUInteger i = 0; i < TEST_NUM_PRINTERS; i++)
    {
        NSObject* obj = [listPrintJobHistoryGroups objectAtIndex:i];
        GHAssertTrue([obj isKindOfClass:[PrintJobHistoryGroup class]], @"");
        
        PrintJobHistoryGroup* group = (PrintJobHistoryGroup*)obj;
        GHTestLog(@"-- group=[%@], jobs=%lu", group.groupName, (unsigned long)group.countPrintJobs);
        GHAssertNotNil(group, @"");
        GHAssertTrue(group.countPrintJobs > 0, @"");
        GHAssertFalse(group.isCollapsed, @"");
    }
}

@end
