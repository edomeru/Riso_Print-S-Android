//
//  PrintJobHistoryGroupTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintJobHistoryGroup.h"
#import "DatabaseManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintJob.h"

@interface PrintJobHistoryGroupTest : GHTestCase
{
    PrintJobHistoryGroup* testGroup;
    NSArray* jobList;
    NSArray* sortedJobList;
}

@end

@implementation PrintJobHistoryGroupTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.name = @"Printer 1";
    pd.ip = @"192.168.1.1";
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    GHAssertTrue([pm registerPrinter:pd], @"check functionality of PrinterManager");
    Printer* printer = [pm getPrinterAtIndex:0];
    GHAssertNotNil(printer, @"check functionality of PrinterManager");
    
    PrintJob* job1 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    GHAssertNotNil(job1, @"check functionality of DatabaseManager");
    job1.name = @"Job 1";
    job1.result = [NSNumber numberWithBool:YES];
    job1.date = [NSDate dateWithTimeIntervalSinceNow:50000];
    job1.printer = printer;
    
    PrintJob* job2 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    GHAssertNotNil(job2, @"check functionality of DatabaseManager");
    job2.name = @"Job 2";
    job2.result = [NSNumber numberWithBool:NO];
    job2.date = [NSDate dateWithTimeIntervalSinceNow:80000];
    job2.printer = printer;
    
    PrintJob* job3 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    GHAssertNotNil(job3, @"check functionality of DatabaseManager");
    job3.name = @"Job 3";
    job3.result = [NSNumber numberWithBool:YES];
    job3.date = [NSDate dateWithTimeIntervalSinceNow:10000];
    job3.printer = printer;
    
    PrintJob* job4 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    GHAssertNotNil(job4, @"check functionality of DatabaseManager");
    job4.name = @"Job 4";
    job4.result = [NSNumber numberWithBool:NO];
    job4.date = [NSDate dateWithTimeIntervalSinceNow:50000];
    job4.printer = printer;
    
    PrintJob* job5 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    GHAssertNotNil(job5, @"check functionality of DatabaseManager");
    job5.name = @"Job 5";
    job5.result = [NSNumber numberWithBool:NO];
    job5.date = [NSDate dateWithTimeIntervalSinceNow:30000];
    job5.printer = printer;
    
    GHAssertTrue(pm.countSavedPrinters == 1, @"");
    GHAssertTrue([DatabaseManager saveChanges], @"check functionality of DatabaseManager");
    
    jobList = [NSArray arrayWithObjects:job1, job2, job3, job4, job5, nil];
    sortedJobList = [NSArray arrayWithObjects:job2, job4, job1, job5, job3, nil];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [DatabaseManager discardChanges];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    GHAssertTrue([pm deletePrinterAtIndex:0], @"check functionality of PrinterManager");
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
    GHTestLog(@"# CHECK: Create a PJHGroup. #");
    
    NSString* groupName = @"Group X";
    
    testGroup = [PrintJobHistoryGroup initWithGroupName:groupName withGroupTag:0];
    GHAssertNotNil(testGroup, @"could not initialize a PrintJobHistoryGroup");
    GHAssertEqualStrings(testGroup.groupName, groupName, @"groupName should be %@", groupName);
    GHAssertTrue(testGroup.countPrintJobs == 0, @"group initially should have no jobs");
    GHAssertFalse(testGroup.isCollapsed, @"group initially should not be collapsed");
}

- (void)test002_AddJobs
{
    GHTestLog(@"# CHECK: Add Jobs to a PJHGroup. #");
    
    PrintJob* job;
    
    for (NSUInteger i = 0; i < [jobList count]; i++)
    {
        job = [jobList objectAtIndex:i];
        GHTestLog(@"-- add %@", job.name);
        
        [testGroup addPrintJob:job];
        GHAssertTrue(testGroup.countPrintJobs == i+1, @"group count should auto-increment on add");
    }
}

- (void)test003_GetJobs
{
    GHTestLog(@"# CHECK: Get Jobs from a PJHGroup. #");
    
    PrintJob* getJob;
    PrintJob* job;

    for (NSUInteger i = 0; i < [jobList count]; i++)
    {
        job = [jobList objectAtIndex:i];
        GHTestLog(@"-- get %@", job.name);
        
        getJob = [testGroup getPrintJobAtIndex:i];
        GHAssertNotNil(getJob, @"should get %@", job.name);
        GHAssertEqualStrings(getJob.name, job.name, @"getJob name should be %@", job.name);
        GHAssertEquals([getJob.result boolValue], [job.result boolValue],
                       @"getJob result should be same as %@", job.name);
        GHAssertEquals([getJob.date compare:job.date], NSOrderedSame,
                       @"getJob date should be same as %@", job.name);
    }
}

- (void)test004_SortJobs
{
    GHTestLog(@"# CHECK: Sort Jobs in a PJHGroup. #");
    
    [testGroup sortPrintJobs];
    GHAssertTrue([testGroup countPrintJobs] == [jobList count], @"should have no change in count");
    
    PrintJob* getJob;
    PrintJob* refJob;
    BOOL sorted = YES;
    
    GHTestLog(@"-- compare to expected sorted list");
    for (NSUInteger i = 0; i < [sortedJobList count]; i++)
    {
        getJob = [testGroup getPrintJobAtIndex:i];
        refJob = [sortedJobList objectAtIndex:i];
        GHTestLog(@"-- @%lu: get=%@, ref=%@", (unsigned long)i, getJob.name, refJob.name);

        if (![getJob.name isEqualToString:refJob.name])
            sorted = NO;
    }
    
    if (!sorted)
        GHFail(@"group is not sorted properly");
}

- (void)test005_RemoveJobs
{
    GHTestLog(@"# CHECK: Remove Jobs in a PJHGroup. #");
    
    PrintJob* job;
    NSUInteger totalJobs = testGroup.countPrintJobs;
    
    while (testGroup.countPrintJobs > 0)
    {
        job = [testGroup getPrintJobAtIndex:0];
        GHTestLog(@"-- remove %@", job.name);
        
        GHAssertTrue([testGroup removePrintJobAtIndex:0], @"remove print job should be successful");
        GHAssertTrue(testGroup.countPrintJobs == --totalJobs, @"group count should auto-decrement on remove");
    }
    
    GHAssertTrue(testGroup.countPrintJobs == 0, @"there should be no more print jobs");
}

- (void)test006_CollapseGroup
{
    GHTestLog(@"# CHECK: Toggle Collapse/Expand. #");
    
    GHAssertFalse(testGroup.isCollapsed, @"");
    [testGroup collapse:YES];
    GHAssertTrue(testGroup.isCollapsed, @"");
    [testGroup collapse:NO];
    GHAssertFalse(testGroup.isCollapsed, @"");
}

@end
