//
//  PrintJobHistoryHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintJobHistoryHelper.h"
#import "PrintJobHistoryGroup.h"
#import "PrinterManager.h"
#import "PListHelper.h"
#import "PrintJob.h"
#import "Printer.h"
#import "DatabaseManager.h"
#import "PrinterDetails.h"
#import "PrintDocument.h"

@interface PrintJobHistoryHelperTest : GHTestCase
{
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
    // remove all test data
    [DatabaseManager discardChanges];
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"check functionality of PrinterManager");
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_PreparePrintJobHistoryGroups
{
    GHTestLog(@"# CHECK: PJHHelper can get PJHGroups. #");
    
    // check setting
    BOOL usingTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
    GHTestLog(@"-- usingTestData = %@", usingTestData ? @"YES" : @"NO");
    GHAssertTrue(usingTestData, @"enable print job history test data");
    
    // TEST DATA CONSTANTS
    // (copy from PrintJobHistoryHelper)
    NSString* TEST_PRINTER_NAME = @"PrintJob Test Printer";
    NSString* TEST_PRINTER_IP = @"999.99.9";
    NSString* TEST_JOB_NAME = @"Test Job";
    const NSUInteger TEST_NUM_PRINTERS = 8;
    const NSUInteger TEST_NUM_JOBS[TEST_NUM_PRINTERS] = {5, 8, 10, 1, 4, 10, 3, 7};
    
    // add a non-test printer with no jobs (for coverage)
    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
    pd1.name = @"Not A Test Printer";
    pd1.ip = @"127.0.0.1";
    GHAssertTrue([[PrinterManager sharedPrinterManager] registerPrinter:pd1], @"");
    // add a test printer (for coverage)
    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
    pd2.name = [NSString stringWithFormat:@"%@ 3", TEST_PRINTER_NAME];
    pd2.ip = [NSString stringWithFormat:@"%@.3", TEST_PRINTER_IP];
    GHAssertTrue([[PrinterManager sharedPrinterManager] registerPrinter:pd2], @"");
    
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
        GHAssertNotNil(group, @"");
        
        // check identifier
        GHTestLog(@"-- group=[%@],[%@]", group.groupName, group.groupIP);
        GHAssertTrue([group.groupName hasPrefix:TEST_PRINTER_NAME], @"");
        GHAssertTrue([group.groupIP hasPrefix:TEST_PRINTER_IP], @"");
        
        // check if number of jobs are correct
        GHAssertTrue(group.countPrintJobs > 0, @"");
        int printerIdx = [[group.groupName substringFromIndex:[TEST_PRINTER_NAME length]+1] intValue] - 1;
        GHTestLog(@"--- expected #jobs=%d", TEST_NUM_JOBS[printerIdx]);
        GHTestLog(@"--- actual #jobs=%lu",  (unsigned long)group.countPrintJobs);
        GHAssertTrue(group.countPrintJobs == TEST_NUM_JOBS[printerIdx], @"");
        
        // check group attributes
        GHAssertFalse(group.isCollapsed, @"");
        
        // check jobs
        for (NSUInteger i = 0; i < group.countPrintJobs; i++)
        {
            PrintJob* job = [group getPrintJobAtIndex:i];
            GHTestLog(@"--- %@", job.name);
            
            GHAssertTrue([job.name hasPrefix:TEST_JOB_NAME], @"");
            NSString* findString = [NSString stringWithFormat:@"%d-", printerIdx+1];
            GHAssertTrue([job.name rangeOfString:findString].location != NSNotFound, @"");
        }
    }
}

- (void)test002_CreatePrintJobFromDocument
{
    GHTestLog(@"# CHECK: PJHHelper can create Print Jobs. #");
    NSString* PDF_NAME = @"test1234.pdf";
    NSString* PDF_URL = @"/temp";
    NSString* VALID_PRINTER_NAME = @"RISO Printer";
    NSString* VALID_PRINTER_IP = @"192.168.1.199";
    NSString* INVALID_PRINTER1_IP = @"192.168.0.1";
    NSString* INVALID_PRINTER2_IP = @"192.168.0.2";
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(pm, @"check functionality of PrinterManager");
    GHAssertTrue(pm.countSavedPrinters == 0, @"");
    
    // create test printers
    //  1 valid (can actually print the document, has a name)
    //  1 invalid (can send but does not receive/process the print job, name is nil)
    //  1 invalid (can send but does not receive/process the print job, name is "")
    
    // --valid
    GHTestLog(@"-- creating printer 1 (valid, has a name)");
    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
    pd1.name = VALID_PRINTER_NAME;
    pd1.ip = VALID_PRINTER_IP;
    GHAssertTrue([pm registerPrinter:pd1], @"check functionality of PrinterManager");
    Printer* printer1 = [pm getPrinterAtIndex:0];
    GHAssertNotNil(printer1, @"check functionality of PrinterManager");
    
    // --invalid
    GHTestLog(@"-- creating printer 2 (invalid, no name)");
    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
    pd2.name = nil;
    pd2.ip = INVALID_PRINTER1_IP;
    GHAssertTrue([pm registerPrinter:pd2], @"check functionality of PrinterManager");
    Printer* printer2 = [pm getPrinterAtIndex:1];
    GHAssertNotNil(printer2, @"check functionality of PrinterManager");
    
    // --invalid
    GHTestLog(@"-- creating printer 3 (invalid, no name)");
    PrinterDetails* pd3 = [[PrinterDetails alloc] init];
    pd3.name = @"";
    pd3.ip = INVALID_PRINTER2_IP;
    GHAssertTrue([pm registerPrinter:pd3], @"check functionality of PrinterManager");
    Printer* printer3 = [pm getPrinterAtIndex:2];
    GHAssertNotNil(printer3, @"check functionality of PrinterManager");

    // create the test document
    GHTestLog(@"-- creating the document [%@]", PDF_NAME);
    PrintDocument* doc = [[PrintDocument alloc] initWithURL:[NSURL URLWithString:PDF_URL]
                                                       name:PDF_NAME];
    GHAssertNotNil(doc, @"check functionality of PrintDocument");
    
    // create the test print jobs
    GHTestLog(@"-- creating the print jobs");
    BOOL isCreated = NO;
    NSInteger result = 0;
    
    // --job1
    GHTestLog(@"--- job1: printer1, result=failed");
    doc.printer = printer1;
    result = 0;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // --job2
    GHTestLog(@"--- job2: printer1, result=success");
    doc.printer = printer1;
    result = 100;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // --job3
    GHTestLog(@"--- job3: printer2, result=failed");
    doc.printer = printer2;
    result = 0;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // --job4
    GHTestLog(@"--- job4: printer2, result=success");
    doc.printer = printer2;
    result = 100;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // --job5
    GHTestLog(@"--- job5: printer3, result=failed");
    doc.printer = printer3;
    result = 0;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // --job6
    GHTestLog(@"--- job6: printer3, result=success");
    doc.printer = printer3;
    result = 100;
    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
    GHAssertTrue(isCreated, @"");
    
    // retrieve the list of print jobs
    GHTestLog(@"-- retrieving the list of print jobs");
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    
    // check list of print jobs
    GHTestLog(@"-- checking number of groups");
    NSUInteger countPrintJobHistoryGroups = [listPrintJobHistoryGroups count];
    GHTestLog(@"-- #groups = %lu", (unsigned long)countPrintJobHistoryGroups);
    GHAssertTrue(countPrintJobHistoryGroups == 3, @"there should be three groups");
    
    // check groups
    for (NSUInteger i = 0; i < 3; i++)
    {
        PrintJobHistoryGroup* group = [listPrintJobHistoryGroups objectAtIndex:i];
        GHTestLog(@"-- group=[%@]: #jobs=[%lu]", group.groupName, (unsigned long)group.countPrintJobs);
        GHAssertTrue(group.countPrintJobs == 2, @"there should be two jobs per group");
        
        // check handling for nil/empty names
        if (![group.groupName isEqualToString:VALID_PRINTER_NAME])
        {
            GHAssertTrue(group.groupName == nil || [group.groupName isEqualToString:@""],
                         @"invalid printer should be either nil or @\"\" name to be properly handled");
        }
    }
}

@end
