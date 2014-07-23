//
//  PrintJobHistoryGroupTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PrintJobHistoryGroup.h"
#import "DatabaseManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintJob.h"

@interface PrintJobHistoryGroupTest : GHTestCase

@property (strong, nonatomic) Printer* testPrinter;
@property (strong, nonatomic) PrintJob* testJob1;
@property (strong, nonatomic) PrintJob* testJob2;
@property (strong, nonatomic) PrintJob* testJob3;
@property (strong, nonatomic) PrintJob* testJob4;
@property (strong, nonatomic) PrintJob* testJob5;
@property (strong, nonatomic) NSArray* jobList;
@property (strong, nonatomic) NSArray* sortedJobList;

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
    [MagicalRecord setDefaultModelFromClass:[self class]];
    [MagicalRecord setupCoreDataStackWithInMemoryStore];
    
    self.testPrinter = [Printer MR_createEntity];
    self.testPrinter.name = @"Printer 1";
    self.testPrinter.ip_address = @"192.168.1.1";
    
    self.testJob1 = [PrintJob MR_createEntity];
    self.testJob1.name = @"Job 1";
    self.testJob1.result = [NSNumber numberWithBool:YES];
    self.testJob1.date = [NSDate dateWithTimeIntervalSinceNow:50000];
    self.testJob1.printer = self.testPrinter;
    
    self.testJob2 = [PrintJob MR_createEntity];
    self.testJob2.name = @"Job 2";
    self.testJob2.result = [NSNumber numberWithBool:NO];
    self.testJob2.date = [NSDate dateWithTimeIntervalSinceNow:80000];
    self.testJob2.printer = self.testPrinter;
    
    self.testJob3 = [PrintJob MR_createEntity];
    self.testJob3.name = @"Job 3";
    self.testJob3.result = [NSNumber numberWithBool:YES];
    self.testJob3.date = [NSDate dateWithTimeIntervalSinceNow:10000];
    self.testJob3.printer = self.testPrinter;
    
    self.testJob4 = [PrintJob MR_createEntity];
    self.testJob4.name = @"Job 4";
    self.testJob4.result = [NSNumber numberWithBool:NO];
    self.testJob4.date = self.testJob1.date;
    self.testJob4.printer = self.testPrinter;
    
    self.testJob5 = [PrintJob MR_createEntity];
    self.testJob5.name = @"Job 5";
    self.testJob5.result = [NSNumber numberWithBool:NO];
    self.testJob5.date = [NSDate dateWithTimeIntervalSinceNow:30000];
    self.testJob5.printer = self.testPrinter;
    
    self.jobList = @[self.testJob1, self.testJob2, self.testJob3, self.testJob4, self.testJob5];
    self.sortedJobList = @[self.testJob2, self.testJob1, self.testJob4, self.testJob5, self.testJob3];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [self.testPrinter MR_deleteEntity];
    [self.testJob1 MR_deleteEntity];
    [self.testJob2 MR_deleteEntity];
    [self.testJob3 MR_deleteEntity];
    [self.testJob4 MR_deleteEntity];
    [self.testJob5 MR_deleteEntity];
    self.jobList = nil;
    self.sortedJobList = nil;
    
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

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)testInitialization
{
    GHTestLog(@"# CHECK: Create a PJHGroup. #");
    
    // SUT
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    
    // Verification
    GHAssertNotNil(testGroup, @"could not initialize a PrintJobHistoryGroup");
    GHAssertTrue(testGroup.tag == groupTag, @"group tag was not properly initialized");
    GHAssertEqualStrings(testGroup.groupName, groupName, @"groupName should be %@", groupName);
    GHAssertEqualStrings(testGroup.groupIP, groupIP, @"groupName should be %@", groupIP);
    GHAssertTrue(testGroup.countPrintJobs == 0, @"group initially should have no jobs");
    GHAssertFalse(testGroup.isCollapsed, @"group initially should not be collapsed");
}

- (void)testAddJobs
{
    GHTestLog(@"# CHECK: Add Jobs to a PJHGroup. #");
    
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    
    // SUT + Verification
    PrintJob* job;
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        job = [self.jobList objectAtIndex:i];
        GHTestLog(@"-- add %@", job.name);
        
        [testGroup addPrintJob:job];
        GHAssertTrue(testGroup.countPrintJobs == i+1, @"group count should auto-increment on add");
    }
}

- (void)testGetJobs
{
    GHTestLog(@"# CHECK: Get Jobs from a PJHGroup. #");
    
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        [testGroup addPrintJob:[self.jobList objectAtIndex:i]];
    }
    
    // SUT + Verification
    PrintJob* getJob;
    PrintJob* refJob;
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        refJob = [self.jobList objectAtIndex:i];
        GHTestLog(@"-- get %@", refJob.name);
        
        getJob = [testGroup getPrintJobAtIndex:i];
        GHAssertNotNil(getJob, @"should get %@", refJob.name);
        GHAssertEqualStrings(getJob.name, refJob.name, @"getJob name should be %@", refJob.name);
        GHAssertTrue([getJob.result boolValue] == [refJob.result boolValue],
                       @"getJob result should be same as %@", refJob.name);
        GHAssertTrue([getJob.date compare:refJob.date] == NSOrderedSame,
                       @"getJob date should be same as %@", refJob.name);
    }
    GHTestLog(@"-- getting an invalid index");
    GHAssertNil([testGroup getPrintJobAtIndex:[self.jobList count]+5], @"");
}

- (void)testSortJobs
{
    GHTestLog(@"# CHECK: Sort Jobs in a PJHGroup. #");
    
    // SUT
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        [testGroup addPrintJob:[self.jobList objectAtIndex:i]];
    }
    [testGroup sortPrintJobs];
    
    // Verification
    GHAssertTrue([testGroup countPrintJobs] == [self.jobList count], @"should have no change in count");
    PrintJob* getJob;
    PrintJob* refJob;
    BOOL sorted = YES;
    GHTestLog(@"-- compare to expected sorted list");
    for (NSUInteger i = 0; i < [self.sortedJobList count]; i++)
    {
        getJob = [testGroup getPrintJobAtIndex:i];
        refJob = [self.sortedJobList objectAtIndex:i];
        GHTestLog(@"-- @%lu: get=%@, ref=%@", (unsigned long)i, getJob.name, refJob.name);

        if (![getJob.name isEqualToString:refJob.name])
            sorted = NO;
    }
    if (!sorted)
        GHFail(@"group is not sorted properly");
}

- (void)testRemoveJobs
{
    GHTestLog(@"# CHECK: Remove Jobs in a PJHGroup. #");
    
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        [testGroup addPrintJob:[self.jobList objectAtIndex:i]];
    }
    
    // SUT + Verification
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
    GHTestLog(@"-- removing an invalid index");
    GHAssertFalse([testGroup removePrintJobAtIndex:2], @"");
}

- (void)testRemoveJobFailed
{
    GHTestLog(@"# CHECK: Remove Jobs in a PJHGroup. #");
    
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] deleteObject:OCMOCK_ANY];
    
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        [testGroup addPrintJob:[self.jobList objectAtIndex:i]];
    }
    
    // SUT + Verification
    GHAssertFalse([testGroup removePrintJobAtIndex:0], @"remove print job should be successful");
    GHAssertTrue(testGroup.countPrintJobs == [self.jobList count], @"job count should remain the same");
}

- (void)testCollapseGroup
{
    GHTestLog(@"# CHECK: Toggle Collapse/Expand. #");
    
    NSInteger groupTag = arc4random() % 10; //0-9
    NSString* groupName = self.testPrinter.name;
    NSString* groupIP = self.testPrinter.ip_address;
    PrintJobHistoryGroup* testGroup = [PrintJobHistoryGroup initWithGroupName:groupName
                                                                  withGroupIP:groupIP
                                                                 withGroupTag:groupTag];
    for (NSUInteger i = 0; i < [self.jobList count]; i++)
    {
        [testGroup addPrintJob:[self.jobList objectAtIndex:i]];
    }
    
    // SUT + Verification
    GHAssertFalse(testGroup.isCollapsed, @"group initially should not be collapsed");
    [testGroup collapse:YES];
    GHAssertTrue(testGroup.isCollapsed, @"group should now be collapsed");
    [testGroup collapse:NO];
    GHAssertFalse(testGroup.isCollapsed, @"group should now be expanded");
}

@end
