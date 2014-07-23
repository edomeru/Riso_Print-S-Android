//
//  PrintJobHistoryHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "MagicalRecord.h"
#import "PrintJobHistoryHelper.h"
#import "PrinterDetails.h"
#import "PrintSetting.h"
#import "DatabaseManager.h"
#import "PrinterManager.h"
#import "PListHelper.h"
#import "Printer.h"
#import "PrintJob.h"
#import "PrintJobHistoryGroup.h"
#import "PrintDocument.h"

@interface PrintJobHistoryHelperTest : GHTestCase

@property (strong, nonatomic) Printer* printer1;
@property (strong, nonatomic) Printer* printer2;
@property (strong, nonatomic) Printer* printer3;

@property (strong, nonatomic) NSString* filter1;
@property (strong, nonatomic) NSString* filter2;
@property (strong, nonatomic) NSString* filter3;

@property (strong, nonatomic) PrintJob* job11;
@property (strong, nonatomic) PrintJob* job21;
@property (strong, nonatomic) PrintJob* job22;
@property (strong, nonatomic) PrintJob* job31;
@property (strong, nonatomic) PrintJob* job32;
@property (strong, nonatomic) PrintJob* job33;

@property (strong, nonatomic) NSMutableArray* jobs1;
@property (strong, nonatomic) NSMutableArray* jobs2;
@property (strong, nonatomic) NSMutableArray* jobs3;

@property (strong, nonatomic) PrintDocument* doc;

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
    [MagicalRecord setDefaultModelFromClass:[self class]];
    [MagicalRecord setupCoreDataStackWithInMemoryStore];
    
    self.printer1 = [Printer MR_createEntity];
    self.printer2 = [Printer MR_createEntity];
    self.printer3 = [Printer MR_createEntity];
    self.printer1.ip_address = @"192.168.0.1";
    self.printer2.ip_address = @"192.168.0.2";
    self.printer3.ip_address = @"192.168.0.3";
    self.filter1 = [NSString stringWithFormat:@"printer.ip_address = '%@'", self.printer1.ip_address];
    self.filter2 = [NSString stringWithFormat:@"printer.ip_address = '%@'", self.printer2.ip_address];
    self.filter3 = [NSString stringWithFormat:@"printer.ip_address = '%@'", self.printer3.ip_address];
    
    self.job11 = [PrintJob MR_createEntity];
    self.job21 = [PrintJob MR_createEntity];
    self.job21.date = [NSDate dateWithTimeIntervalSinceNow:20000];
    self.job22 = [PrintJob MR_createEntity];
    self.job22.date = [NSDate dateWithTimeIntervalSinceNow:40000];
    self.job31 = [PrintJob MR_createEntity];
    self.job31.date = [NSDate dateWithTimeIntervalSinceNow:60000];
    self.job32 = [PrintJob MR_createEntity];
    self.job31.date = [NSDate dateWithTimeIntervalSinceNow:30000];
    self.job33 = [PrintJob MR_createEntity];
    self.job31.date = [NSDate dateWithTimeIntervalSinceNow:90000];
    self.job11.printer = self.printer1;
    self.job21.printer = self.printer2;
    self.job22.printer = self.printer2;
    self.job31.printer = self.printer3;
    self.job32.printer = self.printer3;
    self.job33.printer = self.printer3;
    
    self.jobs1 = [NSMutableArray arrayWithArray:@[self.job11]];
    self.jobs2 = [NSMutableArray arrayWithArray:@[self.job21, self.job22]];
    self.jobs3 = [NSMutableArray arrayWithArray:@[self.job31, self.job32, self.job33]];
    
    NSURL* url = [NSURL URLWithString:@"/temp"];
    NSString* name = @"sample.pdf";
    self.doc = [[PrintDocument alloc] initWithURL:url name:name];
    self.doc.printer = self.printer1;
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [self.printer1 MR_deleteEntity];
    [self.printer2 MR_deleteEntity];
    [self.printer3 MR_deleteEntity];
    self.filter1 = nil;
    self.filter2 = nil;
    self.filter3 = nil;
    
    [self.job11 MR_deleteEntity];
    [self.job21 MR_deleteEntity];
    [self.job22 MR_deleteEntity];
    [self.job31 MR_deleteEntity];
    [self.job32 MR_deleteEntity];
    [self.job33 MR_deleteEntity];
    
    [self.jobs1 removeAllObjects];
    [self.jobs2 removeAllObjects];
    [self.jobs3 removeAllObjects];
    
    self.doc = nil;
    
    [MagicalRecord cleanUp];
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

- (void)testPrepareGroups_NoPrinters
{
    // Mock PrinterManager
    id mockPrinterManager = [OCMockObject partialMockForObject:[PrinterManager sharedPrinterManager]];
    [[[mockPrinterManager stub] andReturnValue:OCMOCK_VALUE((NSUInteger)0)] countSavedPrinters];
    
    // SUT
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    
    // Verification
    GHAssertNotNil(listPrintJobHistoryGroups, @"print job history groups list should not be nil");
    GHAssertTrue([listPrintJobHistoryGroups count] == 0, @"print job history groups should be empty");
}

- (void)testPrepareGroups_NoPrintersWithJobs
{
    // Mock PrinterManager
    id mockPrinterManager = [OCMockObject partialMockForObject:[PrinterManager sharedPrinterManager]];
    [[[mockPrinterManager stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] countSavedPrinters];
    [[[mockPrinterManager stub] andReturn:self.printer1] getPrinterAtIndex:0];
    [[[mockPrinterManager stub] andReturn:self.printer2] getPrinterAtIndex:1];
    [[[mockPrinterManager stub] andReturn:self.printer3] getPrinterAtIndex:2];
    
    // Mock DatabaseManager
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:@[]] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturn:@[]] getObjects:E_PRINTJOB usingFilter:self.filter2];
    [[[mockDatabaseManager stub] andReturn:@[]] getObjects:E_PRINTJOB usingFilter:self.filter3];
    
    // SUT
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    
    // Verification
    GHAssertNotNil(listPrintJobHistoryGroups, @"print job history groups list should not be nil");
    GHAssertTrue([listPrintJobHistoryGroups count] == 0, @"print job history groups should be empty");
}

- (void)testPrepareGroups_SomePrintersWithJobs
{
    // Mock PrinterManager
    id mockPrinterManager = [OCMockObject partialMockForObject:[PrinterManager sharedPrinterManager]];
    [[[mockPrinterManager stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] countSavedPrinters];
    [[[mockPrinterManager stub] andReturn:self.printer1] getPrinterAtIndex:0];
    [[[mockPrinterManager stub] andReturn:self.printer2] getPrinterAtIndex:1];
    [[[mockPrinterManager stub] andReturn:self.printer3] getPrinterAtIndex:2];
    
    // Mock DatabaseManager
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.jobs1] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturn:@[]] getObjects:E_PRINTJOB usingFilter:self.filter2];
    [[[mockDatabaseManager stub] andReturn:self.jobs3] getObjects:E_PRINTJOB usingFilter:self.filter3];
    
    // SUT
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    
    // Verification
    GHAssertNotNil(listPrintJobHistoryGroups, @"print job history groups list should not be nil");
    GHAssertTrue([listPrintJobHistoryGroups count] == 2, @"print job history groups should be 2");
    PrintJobHistoryGroup* group1 = [listPrintJobHistoryGroups objectAtIndex:0];
    PrintJobHistoryGroup* group3 = [listPrintJobHistoryGroups objectAtIndex:1];
    GHAssertTrue([group1 countPrintJobs] == 1, @"");
    GHAssertTrue([group3 countPrintJobs] == 3, @"");
}

- (void)testPrepareGroups_AllPrintersWithJobs
{
    // Mock PrinterManager
    id mockPrinterManager = [OCMockObject partialMockForObject:[PrinterManager sharedPrinterManager]];
    [[[mockPrinterManager stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] countSavedPrinters];
    [[[mockPrinterManager stub] andReturn:self.printer1] getPrinterAtIndex:0];
    [[[mockPrinterManager stub] andReturn:self.printer2] getPrinterAtIndex:1];
    [[[mockPrinterManager stub] andReturn:self.printer3] getPrinterAtIndex:2];
    
    // Mock DatabaseManager
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.jobs1] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturn:self.jobs2] getObjects:E_PRINTJOB usingFilter:self.filter2];
    [[[mockDatabaseManager stub] andReturn:self.jobs3] getObjects:E_PRINTJOB usingFilter:self.filter3];
    
    // SUT
    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    
    // Verification
    GHAssertNotNil(listPrintJobHistoryGroups, @"print job history groups list should not be nil");
    GHAssertTrue([listPrintJobHistoryGroups count] == 3, @"print job history groups should be 3");
    PrintJobHistoryGroup* group1 = [listPrintJobHistoryGroups objectAtIndex:0];
    PrintJobHistoryGroup* group2 = [listPrintJobHistoryGroups objectAtIndex:1];
    PrintJobHistoryGroup* group3 = [listPrintJobHistoryGroups objectAtIndex:2];
    GHAssertTrue([group1 countPrintJobs] == 1, @"");
    GHAssertTrue([group2 countPrintJobs] == 2, @"");
    GHAssertTrue([group3 countPrintJobs] == 3, @"");
}

- (void)testCreatePrintJob_OK_FirstJob
{
    // Mock DatabaseManager
    PrintJob* newJob = [PrintJob MR_createEntity];
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:newJob] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:@[]] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertTrue(created, @"print job must be successfully created");
    GHAssertEqualStrings(newJob.name, self.doc.name, @"");
    GHAssertEqualStrings(newJob.printer.name, self.doc.printer.name, @"");
    GHAssertEquals([newJob.result intValue], 100, @"");
}

- (void)testCreatePrintJob_OK_NextJob
{
    // Mock DatabaseManager
    PrintJob* newJob = [PrintJob MR_createEntity];
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:newJob] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:self.jobs1] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertTrue(created, @"print job must be successfully created");
    GHAssertEqualStrings(newJob.name, self.doc.name, @"");
    GHAssertEqualStrings(newJob.printer.name, self.doc.printer.name, @"");
    GHAssertEquals([newJob.result intValue], 100, @"");
}

- (void)testCreatePrintJob_OK_MaximumJobs
{
    // Mock DatabaseManager
    PrintJob* newJob = [PrintJob MR_createEntity];
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:newJob] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:self.jobs3] getObjects:E_PRINTJOB usingFilter:self.filter3];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    self.doc.printer = self.printer3;
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertTrue(created, @"print job must be successfully created");
    GHAssertEqualStrings(newJob.name, self.doc.name, @"");
    GHAssertEqualStrings(newJob.printer.name, self.doc.printer.name, @"");
    GHAssertEquals([newJob.result intValue], 100, @"");
}

- (void)testCreatePrintJob_NG_CannotAddJob
{
    // Mock DatabaseManager
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:nil] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:self.jobs1] getObjects:E_PRINTJOB usingFilter:self.filter1];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertFalse(created, @"print job creation must fail");
}

- (void)testCreatePrintJob_NG_CannotSaveJob
{
    // Mock DatabaseManager
    PrintJob* newJob = [PrintJob MR_createEntity];
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:newJob] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:self.jobs1] getObjects:E_PRINTJOB usingFilter:self.filter1];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] saveChanges];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertFalse(created, @"print job creation must fail");
}

- (void)testCreatePrintJob_NG_CannotDeleteOldestJob
{
    // Mock DatabaseManager
    PrintJob* newJob = [PrintJob MR_createEntity];
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:newJob] addObject:E_PRINTJOB];
    [[[mockDatabaseManager stub] andReturn:self.jobs3] getObjects:E_PRINTJOB usingFilter:self.filter3];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] deleteObject:OCMOCK_ANY];
    
    // Mock PListHelper
    id mockPListHelper = [OCMockObject mockForClass:[PListHelper class]];
    [[[mockPListHelper stub] andReturnValue:OCMOCK_VALUE((NSUInteger)3)] readUint:kPlistUintValMaxPrintJobsPerPrinter];
    
    // SUT
    self.doc.printer = self.printer3;
    BOOL created = [PrintJobHistoryHelper createPrintJobFromDocument:self.doc withResult:100];
    
    // Verification
    GHAssertFalse(created, @"print job creation must fail");
}

///* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
///* use a naming scheme for defining the execution order of your test cases */
//
//- (void)test001_PreparePrintJobHistoryGroups
//{
//    GHTestLog(@"# CHECK: PJHHelper can get PJHGroups. #");
//    
//    // check setting
//    BOOL usingTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
//    GHTestLog(@"-- usingTestData = %@", usingTestData ? @"YES" : @"NO");
//    GHAssertTrue(usingTestData, @"enable print job history test data");
//    
//    // TEST DATA CONSTANTS
//    // (copy from PrintJobHistoryHelper)
//    NSString* TEST_PRINTER_NAME = @"PrintJob Test Printer";
//    NSString* TEST_PRINTER_IP = @"999.99.9";
//    NSString* TEST_JOB_NAME = @"Test Job";
//    const NSUInteger TEST_NUM_PRINTERS = 8;
//    const NSUInteger TEST_NUM_JOBS[TEST_NUM_PRINTERS] = {5, 8, 10, 1, 4, 10, 3, 7};
//    
//    // add a non-test printer with no jobs (for coverage)
//    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
//    pd1.name = @"Not A Test Printer";
//    pd1.ip = @"127.0.0.1";
//    GHAssertTrue([[PrinterManager sharedPrinterManager] registerPrinter:pd1], @"");
//    // add a test printer with no jobs (for coverage)
//    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
//    pd2.name = [NSString stringWithFormat:@"%@ 3", TEST_PRINTER_NAME];
//    pd2.ip = [NSString stringWithFormat:@"%@.3", TEST_PRINTER_IP];
//    GHAssertTrue([[PrinterManager sharedPrinterManager] registerPrinter:pd2], @"");
//    
//    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
//    GHAssertNotNil(listPrintJobHistoryGroups, @"");
//
//    GHTestLog(@"-- checking number of groups");
//    NSUInteger countPrintJobHistoryGroups = [listPrintJobHistoryGroups count];
//    GHTestLog(@"-- #groups = %lu", (unsigned long)countPrintJobHistoryGroups);
//    GHAssertTrue(countPrintJobHistoryGroups == TEST_NUM_PRINTERS, @"should be equal to # of test printers");
//    
//    for (NSUInteger i = 0; i < TEST_NUM_PRINTERS; i++)
//    {
//        NSObject* obj = [listPrintJobHistoryGroups objectAtIndex:i];
//        GHAssertTrue([obj isKindOfClass:[PrintJobHistoryGroup class]], @"");
//        
//        PrintJobHistoryGroup* group = (PrintJobHistoryGroup*)obj;
//        GHAssertNotNil(group, @"");
//        
//        // check identifier
//        GHTestLog(@"-- group=[%@],[%@]", group.groupName, group.groupIP);
//        GHAssertTrue([group.groupName hasPrefix:TEST_PRINTER_NAME], @"");
//        GHAssertTrue([group.groupIP hasPrefix:TEST_PRINTER_IP], @"");
//        
//        // check if number of jobs are correct
//        GHAssertTrue(group.countPrintJobs > 0, @"");
//        int printerIdx = [[group.groupName substringFromIndex:[TEST_PRINTER_NAME length]+1] intValue] - 1;
//        GHTestLog(@"--- expected #jobs=%d", TEST_NUM_JOBS[printerIdx]);
//        GHTestLog(@"--- actual #jobs=%lu",  (unsigned long)group.countPrintJobs);
//        GHAssertTrue(group.countPrintJobs == TEST_NUM_JOBS[printerIdx], @"");
//        
//        // check group attributes
//        GHAssertFalse(group.isCollapsed, @"");
//        
//        // check jobs
//        for (NSUInteger i = 0; i < group.countPrintJobs; i++)
//        {
//            PrintJob* job = [group getPrintJobAtIndex:i];
//            GHTestLog(@"--- %@", job.name);
//            
//            GHAssertTrue([job.name hasPrefix:TEST_JOB_NAME], @"");
//            NSString* findString = [NSString stringWithFormat:@"%d-", printerIdx+1];
//            GHAssertTrue([job.name rangeOfString:findString].location != NSNotFound, @"");
//        }
//    }
//}
//
//- (void)test002_CreatePrintJobFromDocument
//{
//    GHTestLog(@"# CHECK: PJHHelper can create Print Jobs. #");
//    NSString* PDF_NAME = @"test1234.pdf";
//    NSString* PDF_URL = @"/temp";
//    NSString* VALID_PRINTER_NAME = @"RISO Printer";
//    NSString* VALID_PRINTER_IP = @"192.168.1.199";
//    NSString* INVALID_PRINTER1_IP = @"192.168.0.1";
//    NSString* INVALID_PRINTER2_IP = @"192.168.0.2";
//    
//    PrinterManager* pm = [PrinterManager sharedPrinterManager];
//    GHAssertNotNil(pm, @"check functionality of PrinterManager");
//    GHAssertTrue(pm.countSavedPrinters == 0, @"");
//    
//    // create test printers
//    //  1 valid (can actually print the document, has a name)
//    //  1 invalid (can send but does not receive/process the print job, name is nil)
//    //  1 invalid (can send but does not receive/process the print job, name is "")
//    
//    // --valid
//    GHTestLog(@"-- creating printer 1 (valid, has a name)");
//    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
//    pd1.name = VALID_PRINTER_NAME;
//    pd1.ip = VALID_PRINTER_IP;
//    GHAssertTrue([pm registerPrinter:pd1], @"check functionality of PrinterManager");
//    Printer* printer1 = [pm getPrinterAtIndex:0];
//    GHAssertNotNil(printer1, @"check functionality of PrinterManager");
//    
//    // --invalid
//    GHTestLog(@"-- creating printer 2 (invalid, no name)");
//    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
//    pd2.name = nil;
//    pd2.ip = INVALID_PRINTER1_IP;
//    GHAssertTrue([pm registerPrinter:pd2], @"check functionality of PrinterManager");
//    Printer* printer2 = [pm getPrinterAtIndex:1];
//    GHAssertNotNil(printer2, @"check functionality of PrinterManager");
//    
//    // --invalid
//    GHTestLog(@"-- creating printer 3 (invalid, no name)");
//    PrinterDetails* pd3 = [[PrinterDetails alloc] init];
//    pd3.name = @"";
//    pd3.ip = INVALID_PRINTER2_IP;
//    GHAssertTrue([pm registerPrinter:pd3], @"check functionality of PrinterManager");
//    Printer* printer3 = [pm getPrinterAtIndex:2];
//    GHAssertNotNil(printer3, @"check functionality of PrinterManager");
//
//    // create the test document
//    GHTestLog(@"-- creating the document [%@]", PDF_NAME);
//    PrintDocument* doc = [[PrintDocument alloc] initWithURL:[NSURL URLWithString:PDF_URL]
//                                                       name:PDF_NAME];
//    GHAssertNotNil(doc, @"check functionality of PrintDocument");
//    
//    // create the test print jobs
//    GHTestLog(@"-- creating the print jobs");
//    BOOL isCreated = NO;
//    NSInteger result = 0;
//    
//    // --job1
//    GHTestLog(@"--- job1: printer1, result=failed");
//    doc.printer = printer1;
//    result = 0;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // --job2
//    GHTestLog(@"--- job2: printer1, result=success");
//    doc.printer = printer1;
//    result = 100;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // --job3
//    GHTestLog(@"--- job3: printer2, result=failed");
//    doc.printer = printer2;
//    result = 0;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // --job4
//    GHTestLog(@"--- job4: printer2, result=success");
//    doc.printer = printer2;
//    result = 100;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // --job5
//    GHTestLog(@"--- job5: printer3, result=failed");
//    doc.printer = printer3;
//    result = 0;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // --job6
//    GHTestLog(@"--- job6: printer3, result=success");
//    doc.printer = printer3;
//    result = 100;
//    isCreated = [PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:result];
//    GHAssertTrue(isCreated, @"");
//    
//    // retrieve the list of print jobs
//    GHTestLog(@"-- retrieving the list of print jobs");
//    NSMutableArray* listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
//    GHAssertNotNil(listPrintJobHistoryGroups, @"");
//    
//    // check groups
//    for (NSUInteger i = 0; i < 3; i++)
//    {
//        PrintJobHistoryGroup* group = [listPrintJobHistoryGroups objectAtIndex:i];
//        GHTestLog(@"-- group=[%@]: #jobs=[%lu]", group.groupName, (unsigned long)group.countPrintJobs);
//        GHAssertTrue(group.countPrintJobs == 2, @"there should be two jobs per group");
//        
//        // check handling for nil/empty names
//        if (![group.groupName isEqualToString:VALID_PRINTER_NAME])
//        {
//            GHAssertTrue(group.groupName == nil || [group.groupName isEqualToString:@""],
//                         @"invalid printer should be either nil or @\"\" name to be properly handled");
//        }
//    }
//}
//
//- (void)test003_MaxPrintJobs
//{
//    GHTestLog(@"# CHECK: PJHHelper can handle max jobs. #");
//    
//    PrinterManager* pm = [PrinterManager sharedPrinterManager];
//    GHAssertNotNil(pm, @"check functionality of PrinterManager");
//    GHAssertTrue(pm.countSavedPrinters == 0, @"");
//    
//    GHTestLog(@"-- creating the printer");
//    PrinterDetails* pd = [[PrinterDetails alloc] init];
//    pd.name = @"RISO Printer";
//    pd.ip = @"192.168.1.199";
//    GHAssertTrue([pm registerPrinter:pd], @"check functionality of PrinterManager");
//    Printer* printer = [pm getPrinterAtIndex:0];
//    GHAssertNotNil(printer, @"check functionality of PrinterManager");
//    
//    GHTestLog(@"-- maxing-out the jobs");
//    NSUInteger maxJobCount = [PListHelper readUint:kPlistUintValMaxPrintJobsPerPrinter];
//    for (NSUInteger count = 0; count < maxJobCount; count++)
//    {
//        PrintJob* job = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
//        job.name = @"test";
//        job.printer = printer;
//    }
//
//    GHTestLog(@"-- creating the document [%@]", @"test1234.pdf");
//    PrintDocument* doc = [[PrintDocument alloc] initWithURL:[NSURL URLWithString:@"/temp"]
//                                                       name:@"test1234.pdf"];
//    GHAssertNotNil(doc, @"check functionality of PrintDocument");
//    doc.printer = printer;
//    GHAssertTrue([PrintJobHistoryHelper createPrintJobFromDocument:doc withResult:100], @"");
//    
//    NSArray* jobs = [DatabaseManager getObjects:E_PRINTJOB];
//    GHAssertTrue([jobs count] == maxJobCount, @"there should still be max jobs");
//}

@end
