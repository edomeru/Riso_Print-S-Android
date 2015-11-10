//
//  PrintJobHistoryViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#include "OCMock.h"
#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryLayout.h"
#import "PrinterManager.h"
#import "DatabaseManager.h"
#import "PrintJobHistoryGroup.h"
#import "PrinterDetails.h"
#import "PrintJob.h"
#import "PrintJobHistoryGroupCell.h"
#import "NSDate+Format.h"
#import "DeleteButton.h"
#import "PrintJobItemCell.h"
#import "PrintJobHistoryHelper.h"
#import "Printer.h"

const NSUInteger TEST_NUM_PRINTERS = 8;
const NSUInteger TEST_NUM_JOBS[TEST_NUM_PRINTERS] = {8, 5, 10, 1, 4, 6, 3, 7};

@interface PrintJobHistoryViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UICollectionView*)groupsView;
- (PrintJobHistoryLayout*)groupsViewLayout;
- (NSMutableArray*)listPrintJobHistoryGroups;

// expose private methods
- (void)findGroupWithTag:(NSInteger)tag outIndex:(NSInteger*)index outGroup:(PrintJobHistoryGroup**)group;
- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath;

@end

@interface PrintJobHistoryGroupCell (UnitTest)

// expose private properties
- (UILabel*)groupName;
- (UILabel*)groupIP;
- (UILabel*)groupIndicator;
- (DeleteButton*)deleteAllButton;
- (UITableView*)printJobsView;
- (NSIndexPath*)jobWithDelete;
- (NSMutableArray*)listPrintJobs;

@end

@interface PrintJobHistoryViewControllerTest : GHTestCase <PrintJobHistoryGroupCellDelegate>
{

    UIStoryboard* storyboard;
    PrintJobHistoryViewController* controller;
    id mockPrintHistoryHelper;
    NSMutableArray *mockListPrintJobHistoryGroup;
}

@end

@implementation PrintJobHistoryViewControllerTest

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
    
    [self prepareTestData];
    
    mockPrintHistoryHelper = OCMClassMock([PrintJobHistoryHelper class]);
    [[[[mockPrintHistoryHelper stub] classMethod] andReturn:mockListPrintJobHistoryGroup] preparePrintJobHistoryGroups];

    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerName = @"PrintJobHistoryViewController";
    controller = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controller, @"unable to instantiate controller (%@)", controllerName);
    GHAssertNotNil(controller.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controller = nil;
    
    [mockPrintHistoryHelper stopMocking];
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

- (void)test001_IBOutletsBinding
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controller mainMenuButton], @"");
    GHAssertNotNil([controller groupsView], @"");
    GHAssertNotNil([controller groupsViewLayout], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButtonIphone = [controller mainMenuButton];
    ibActions = [mainMenuButtonIphone actionsForTarget:controller
                                       forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

- (void)test003_UICollectionView
{
    GHTestLog(@"# CHECK: UICollectionView. #");
  //--defined in PrintJobHistoryHelper
    
    NSMutableArray* listPrintJobHistoryGroups;
    NSUInteger countGroups;
    UICollectionView* groupsView;

    GHTestLog(@"-- List of Print Jobs (iPhone)");
    listPrintJobHistoryGroups = [controller listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    
    GHTestLog(@"-- UICollectionView (iPhone-Portrait)");
    [[controller groupsViewLayout] setupForOrientation:UIInterfaceOrientationPortrait
                                                   forDevice:UIUserInterfaceIdiomPhone];
    GHTestLog(@"-- checking sections");
    groupsView = [controller groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- UICollectionView (iPhone-Landscape)");
    [[controller groupsViewLayout] setupForOrientation:UIInterfaceOrientationLandscapeLeft
                                                   forDevice:UIUserInterfaceIdiomPhone];
    GHTestLog(@"-- checking sections");
    groupsView = [controller groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
}

- (void)test004_FindGroupWithTag
{
    GHTestLog(@"# CHECK: Find Group With Tag. #");
    
    NSMutableArray* listPrintJobHistoryGroups = [controller listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    
    NSUInteger countGroups = [listPrintJobHistoryGroups count]; //--this will be equal to TEST_NUM_PRINTERS
    GHTestLog(@"-- #groups=[%lu]", (unsigned long)countGroups); //--defined in PrintJobHistoryHelper
    
    // get the group (tag,name) as reference later
    GHTestLog(@"-- saving (tag,name) for reference");
    NSMutableDictionary* groupNames = [NSMutableDictionary dictionaryWithCapacity:countGroups];
    NSMutableDictionary* groupIndices = [NSMutableDictionary dictionaryWithCapacity:countGroups];
    for (NSUInteger i = 0; i < countGroups; i++)
    {
        // get the group
        PrintJobHistoryGroup* group = [listPrintJobHistoryGroups objectAtIndex:i];
        
        NSString* tagString = [NSString stringWithFormat:@"%ld", (long)group.tag];
        
        // save the (tag,name)
        [groupNames setValue:group.groupName forKey:tagString];
        
        // save the (tag,index)
        [groupIndices setValue:[NSNumber numberWithUnsignedInteger:i] forKey:tagString];
    }
    
    // find group with tags
    GHTestLog(@"-- finding groups using tag, checking group");
    [groupNames enumerateKeysAndObjectsUsingBlock:^(NSString* tag, NSString* expectedName, BOOL* stop)
    {
        NSInteger groupFoundIdx;
        PrintJobHistoryGroup* groupFound;
        [controller findGroupWithTag:[tag integerValue]   //same method for iPad, no need to test twice
                                  outIndex:&groupFoundIdx
                                  outGroup:&groupFound];
        
        GHTestLog(@"--- actual=[%@]", groupFound.groupName);
        GHTestLog(@"--- expected=[%@]", expectedName);
        GHAssertEqualStrings(groupFound.groupName, expectedName, @"");
    }];
    GHTestLog(@"-- finding groups using tag, checking index");
    [groupIndices enumerateKeysAndObjectsUsingBlock:^(NSString* tag, NSNumber* expectedIdx, BOOL* stop)
    {
         NSInteger groupFoundIdx;
         PrintJobHistoryGroup* groupFound;
         [controller findGroupWithTag:[tag integerValue]   //same method for iPad, no need to test twice
                                   outIndex:&groupFoundIdx
                                   outGroup:&groupFound];
         
         GHTestLog(@"--- actual=[%ld]", (long)groupFoundIdx);
         GHTestLog(@"--- expected=[%ld]", (long)[expectedIdx integerValue]);
         GHAssertTrue(groupFoundIdx == [expectedIdx integerValue], @"");
    }];
    
    // list is unchanged
    GHAssertTrue([listPrintJobHistoryGroups count] == countGroups, @"list of groups should be unchanged");
}

- (void)test005_PrintJobHistoryGroupCellBindings
{
    GHTestLog(@"# CHECK: PrintJobHistoryGroupCell. #");
    UICollectionView* groupsView;
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;
    
    GHTestLog(@"-- UICollectionView (iPhone)");
    groupsView = [controller groupsView];
    GHTestLog(@"-- checking cell bindings");
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];
    GHAssertNotNil(groupCell, @"");
    GHAssertNotNil([groupCell groupName], @"");
    GHAssertNotNil([groupCell groupIP], @"");
    GHAssertNotNil([groupCell groupIndicator], @"");
    GHAssertNotNil([groupCell deleteAllButton], @"");
    GHAssertNotNil([groupCell printJobsView], @"");
}

- (void)test006_PrintJobHistoryGroupCellIPhone
{
    GHTestLog(@"# CHECK: PrintJobHistoryGroupCell (Iphone). #");
    UICollectionView* groupsView;
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;
    NSUInteger tag = 5;
    NSString* printerName = @"RISO Printer";
    NSString* printerIP = @"192.168.0.1";
    NSString* printJobName = @"Print Job 1";
    BOOL printJobResult = YES;
    NSDate* printJobDate = [NSDate date];
    
    GHTestLog(@"-- UICollectionView (iPhone)");
    
    groupsView = [controller groupsView];
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];

    GHAssertNil([groupCell jobWithDelete], @"");
    
    [groupCell initWithTag:tag];
    GHAssertTrue([groupCell groupName].tag == tag, @"");
    GHAssertTrue([groupCell groupIP].tag == tag, @"");
    GHAssertTrue([groupCell groupIndicator].tag == tag, @"");
    GHAssertTrue([groupCell deleteAllButton].tag == tag, @"");
    GHAssertTrue([groupCell printJobsView].tag == tag, @"");
    GHAssertNotNil([groupCell listPrintJobs], @"");
    
    [groupCell putGroupName:printerName];
    GHAssertEqualStrings([groupCell groupName].text, printerName, @"");
    [groupCell putGroupName:nil];
    GHAssertEqualStrings([groupCell groupName].text, NSLocalizedString(@"IDS_LBL_NO_NAME", ""),  @"");
    [groupCell putGroupName:@""];
    GHAssertEqualStrings([groupCell groupName].text, NSLocalizedString(@"IDS_LBL_NO_NAME", ""),  @"");
    
    [groupCell putGroupIP:printerIP];
    GHAssertEqualStrings([groupCell groupIP].text, printerIP, @"");
    
    [groupCell putIndicator:YES];
    GHAssertEqualStrings([groupCell groupIndicator].text, @"+", @"");
    
    [groupCell putIndicator:NO];
    GHAssertEqualStrings([groupCell groupIndicator].text, @"-", @"");
    
    [groupCell putPrintJob:printJobName withResult:printJobResult withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:printJobResult withTimestamp:printJobDate];
    GHAssertTrue([[groupCell listPrintJobs] count] == 2, @"");
    
    [groupCell reloadContents];
    GHAssertTrue([[groupCell listPrintJobs] count] == 2, @"");
}

- (void)test007_PrintJobHistoryGroupCellIPad
{
    GHTestLog(@"# CHECK: PrintJobHistoryGroupCell (Ipad). #");
    UICollectionView* groupsView;
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;
    NSUInteger tag = 5;
    NSString* printerName = @"RISO Printer";
    NSString* printerIP = @"192.168.0.1";
    NSString* printJobName = @"Print Job 1";
    BOOL printJobResult = YES;
    NSDate* printJobDate = [NSDate date];
    
    GHTestLog(@"-- UICollectionView (iPad)");
    
    groupsView = [controller groupsView];
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];
    
    GHAssertNil([groupCell jobWithDelete], @"");
    
    [groupCell initWithTag:tag];
    GHAssertTrue([groupCell groupName].tag == tag, @"");
    GHAssertTrue([groupCell groupIP].tag == tag, @"");
    GHAssertTrue([groupCell groupIndicator].tag == tag, @"");
    GHAssertTrue([groupCell deleteAllButton].tag == tag, @"");
    GHAssertTrue([groupCell printJobsView].tag == tag, @"");
    GHAssertNotNil([groupCell listPrintJobs], @"");
    
    [groupCell putGroupName:printerName];
    GHAssertEqualStrings([groupCell groupName].text, printerName, @"");
    [groupCell putGroupName:nil];
    GHAssertEqualStrings([groupCell groupName].text, NSLocalizedString(@"IDS_LBL_NO_NAME", ""),  @"");
    [groupCell putGroupName:@""];
    GHAssertEqualStrings([groupCell groupName].text, NSLocalizedString(@"IDS_LBL_NO_NAME", ""),  @"");
    
    [groupCell putGroupIP:printerIP];
    GHAssertEqualStrings([groupCell groupIP].text, printerIP, @"");
    
    [groupCell putIndicator:YES];
    GHAssertEqualStrings([groupCell groupIndicator].text, @"+", @"");
    
    [groupCell putIndicator:NO];
    GHAssertEqualStrings([groupCell groupIndicator].text, @"-", @"");
    
    [groupCell putPrintJob:printJobName withResult:printJobResult withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:printJobResult withTimestamp:printJobDate];
    GHAssertTrue([[groupCell listPrintJobs] count] == 2, @"");
    
    [groupCell reloadContents];
    GHAssertTrue([[groupCell listPrintJobs] count] == 2, @"");
}

- (void)test008_PrintJobHistoryGroupCellJobs
{
    GHTestLog(@"# CHECK: PrintJobHistoryGroupCell (Ipad). #");
    UICollectionView* groupsView;
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;
    NSUInteger tag = 5;
    NSString* printerName = @"RISO Printer";
    NSString* printerIP = @"192.168.0.1";
    NSString* printJobName = @"Print Job 1";
    NSDate* printJobDate = [NSDate date];
    
    groupsView = [controller groupsView];
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];
    [groupCell initWithTag:tag];
    [groupCell putGroupName:printerName];
    [groupCell putGroupIP:printerIP];
    [groupCell putPrintJob:printJobName withResult:YES withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:NO withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:YES withTimestamp:printJobDate];
    [groupCell reloadContents];
    
    //get the UITableView
    NSArray* subViews = [groupCell.contentView subviews];
    UITableView* jobsView = nil;
    for (NSUInteger i = 0; i < [subViews count]; i++)
    {
        UIView* subView = [subViews objectAtIndex:i];
        if ([subView isKindOfClass:[UITableView class]])
        {
            jobsView = (UITableView*)subView;
            break;
        }
    }
    GHAssertNotNil(jobsView, @"");
    
    PrintJobItemCell* jobCell = (PrintJobItemCell*)[jobsView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
    GHAssertNotNil(jobCell, @"");
    GHAssertEqualStrings(jobCell.name.text, printJobName, @"");
    GHAssertEqualStrings(jobCell.timestamp.text, [printJobDate formattedString], @"");
    GHAssertNotNil(jobCell.result, @"");
}

- (void)test009_PrintJobHistoryGroupCellJobs_DeleteState_Reswipe
{
    UICollectionView* groupsView;
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;
    NSUInteger tag = 5;
    NSString* printerName = @"RISO Printer";
    NSString* printerIP = @"192.168.0.1";
    NSString* printJobName = @"Print Job 1";
    NSDate* printJobDate = [NSDate date];
    
    groupsView = [controller groupsView];
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];
    [groupCell initWithTag:tag];
    [groupCell putGroupName:printerName];
    [groupCell putGroupIP:printerIP];
    [groupCell putPrintJob:printJobName withResult:YES withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:NO withTimestamp:printJobDate];
    [groupCell putPrintJob:printJobName withResult:YES withTimestamp:printJobDate];
    [groupCell reloadContents];
    
    
    groupCell.delegate = self;
 
    PrintJobItemCell *jobCell = [[PrintJobItemCell alloc] init];
    NSIndexPath *testIndexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    
    UITableView*  originalPrintJobsView = groupCell.printJobsView;
    
    CGPoint point = CGPointMake(1.0f, 2.0f);
    
    id partialPrintJobsViewMock = OCMPartialMock(groupCell.printJobsView);
    
    [[[partialPrintJobsViewMock stub] andReturn:jobCell] cellForRowAtIndexPath:[OCMArg any]];
    [[[partialPrintJobsViewMock stub] andReturn:testIndexPath] indexPathForRowAtPoint:point];
    
    [groupCell setValue:partialPrintJobsViewMock forKey:@"printJobsView"];
    
    id mockGestureRecognizer = OCMClassMock([UIGestureRecognizer class]);
    [[[mockGestureRecognizer stub] andReturnValue:OCMOCK_VALUE(point)] locationInView:[OCMArg any]];
    
    //1st Swipe
    [groupCell performSelector:@selector(putDeleteButton:) withObject:mockGestureRecognizer];

    BOOL isReswipe = [[groupCell valueForKey:@"reswipeLeftOccured"] boolValue];
    GHAssertFalse(isReswipe, @"");
    
    //2nd Swipe
    [groupCell performSelector:@selector(putDeleteButton:) withObject:mockGestureRecognizer];
    
    isReswipe = [[groupCell valueForKey:@"reswipeLeftOccured"] boolValue];
    GHAssertTrue(isReswipe, @"");
    NSIndexPath *jobWithDelete = [groupCell valueForKey:@"jobWithDelete"];
    GHAssertNotNil(jobWithDelete, @"");
    GHAssertEqualObjects(jobWithDelete, testIndexPath,@"");
    
    //attempt to remove button after 2nd swipe
    GHAssertFalse([groupCell removeDeleteButton], @"");
    
    isReswipe = [[groupCell valueForKey:@"reswipeLeftOccured"] boolValue];
    GHAssertFalse(isReswipe, @"");
    jobWithDelete = [groupCell valueForKey:@"jobWithDelete"];
    GHAssertNotNil(jobWithDelete, @"");
    GHAssertEqualObjects(jobWithDelete, testIndexPath,@"");
    
    //3rd swipe
    [groupCell performSelector:@selector(putDeleteButton:) withObject:mockGestureRecognizer];
    isReswipe = [[groupCell valueForKey:@"reswipeLeftOccured"] boolValue];
    GHAssertTrue(isReswipe, @"");
    jobWithDelete = [groupCell valueForKey:@"jobWithDelete"];
    GHAssertNotNil(jobWithDelete, @"");
    GHAssertEqualObjects(jobWithDelete, testIndexPath,@"");
    
    //attemp to remove button after 3rd swipe
    GHAssertFalse([groupCell removeDeleteButton], @"");
    
    //assume another gesture triggered to remove button again, this time reswipe is not anymore called before attempt to remove button so reswipe did not occur
    GHAssertTrue([groupCell removeDeleteButton], @"");
    jobWithDelete = [groupCell valueForKey:@"jobWithDelete"];
    GHAssertNil(jobWithDelete, @"");
    
    [groupCell setValue:originalPrintJobsView forKey:@"printJobsView"];
    [partialPrintJobsViewMock stopMocking];
    [mockGestureRecognizer stopMocking];
    
}


- (void)test010_PrintJobHistory_SwipeNotDelete
{
    id mockGroupCell = OCMClassMock([PrintJobHistoryGroupCell class]);
    [[[mockGroupCell stub] andReturnValue:OCMOCK_VALUE(YES)] removeDeleteButton];
    
    UICollectionView *originalGroupsView = [controller groupsView];
    id mockGroupsView = OCMPartialMock([controller groupsView]);
    [[[mockGroupsView stub] andReturn:mockGroupCell] cellForItemAtIndexPath:[OCMArg any]];
    
    NSIndexPath *testIndexPath = [NSIndexPath indexPathForRow:1 inSection:2];
    [controller setValue:testIndexPath forKey:@"groupWithDelete"];
    [controller setValue:mockGroupsView forKey:@"groupsView"];
    
    {
        id mockGestureRecognizer = OCMClassMock([UIGestureRecognizer class]);
        [[[mockGestureRecognizer stub] andReturnValue:OCMOCK_VALUE(UIGestureRecognizerStateBegan)] state];
        [controller performSelector:@selector(swipedNotLeftCollection:) withObject:mockGestureRecognizer];
        NSIndexPath *groupWithDelete = [controller valueForKey:@"groupWithDelete"];
        GHAssertNotNil(groupWithDelete, @"");
        GHAssertEqualObjects(groupWithDelete, testIndexPath, @"");
        [mockGestureRecognizer stopMocking];
    }
    
    {
        id mockGestureRecognizer = OCMClassMock([UIGestureRecognizer class]);
        [[[mockGestureRecognizer stub] andReturnValue:OCMOCK_VALUE(UIGestureRecognizerStateEnded)] state];
        [controller performSelector:@selector(swipedNotLeftCollection:) withObject:mockGestureRecognizer];
        NSIndexPath *groupWithDelete = [controller valueForKey:@"groupWithDelete"];
        GHAssertNil(groupWithDelete, @"");
        [mockGestureRecognizer stopMocking];
    }
    
    [controller setValue:originalGroupsView forKey:@"groupsView"];
    [mockGroupsView stopMocking];
    [mockGroupCell stopMocking];
}


- (void)test011_PrintJobHistory_RemoveDelete
{
    
    NSIndexPath *testIndexPath = [NSIndexPath indexPathForRow:1 inSection:2];
    [controller setValue:testIndexPath forKey:@"groupWithDelete"];
    UICollectionView *originalGroupsView = [controller groupsView];

    //remove delete NO
    id mockGroupCell = OCMClassMock([PrintJobHistoryGroupCell class]);
    [[[mockGroupCell stub] andReturnValue:OCMOCK_VALUE(NO)] removeDeleteButton];
    
    id mockGroupsView = OCMPartialMock([controller groupsView]);
    [[[mockGroupsView stub] andReturn:mockGroupCell] cellForItemAtIndexPath:[OCMArg any]];
    [controller setValue:mockGroupsView forKey:@"groupsView"];
    
    [controller performSelector:@selector(removeDeleteButton) withObject:nil];
    
    NSIndexPath *groupWithDelete = [controller valueForKey:@"groupWithDelete"];
    GHAssertNotNil(groupWithDelete, @"");
    GHAssertEqualObjects(groupWithDelete, testIndexPath, @"");
    
    [mockGroupsView stopMocking];
    [mockGroupCell stopMocking];
    
    //remove delete YES
    mockGroupCell = OCMClassMock([PrintJobHistoryGroupCell class]);
    [[[mockGroupCell stub] andReturnValue:OCMOCK_VALUE(YES)] removeDeleteButton];
    
    mockGroupsView = OCMPartialMock([controller groupsView]);
    [[[mockGroupsView stub] andReturn:mockGroupCell] cellForItemAtIndexPath:[OCMArg any]];
    [controller setValue:mockGroupsView forKey:@"groupsView"];
    
    [controller performSelector:@selector(removeDeleteButton) withObject:nil];
    
    groupWithDelete = [controller valueForKey:@"groupWithDelete"];
    GHAssertNil(groupWithDelete, @"");
    GHAssertNotEqualObjects(groupWithDelete, testIndexPath, @"");
    
    [controller setValue:originalGroupsView forKey:@"groupsView"];
    [mockGroupsView stopMocking];
    [mockGroupCell stopMocking];
    
}


#pragma mark - PrintJobHistoryGroupCellDelegate Methods

- (BOOL)shouldHighlightGroupHeader
{
    return YES;
}

- (void)didTapGroupHeader:(NSUInteger)groupTag
{
    
}

- (BOOL)shouldHighlightDeleteGroupButton
{
    return YES;
}

- (void)didTapDeleteGroupButton:(DeleteButton *)button ofGroup:(NSUInteger)groupTag
{
    
}

- (BOOL)shouldPutDeleteJobButton:(NSUInteger)groupTag
{
    return YES;
}

- (void)didTapDeleteJobButton:(DeleteButton *)button ofJob:(NSUInteger)jobTag ofGroup:(NSUInteger)groupTag
{
    
}

- (BOOL)shouldHighlightJob
{
    return YES;
}

#pragma mark - Test Data preparation

-(void)prepareTestData
{
    mockListPrintJobHistoryGroup = [[NSMutableArray alloc] init];
    NSMutableArray* printerList = [[NSMutableArray alloc] init];
    NSString *baseIP = @"192.168.1.";
    NSString *basePrintName = @"Printer";
    NSString *baseJobName = @"Job";
    
    for(int i = 0;  i < TEST_NUM_PRINTERS; i++){
        Printer *printer = [Printer MR_createEntity];
        printer.name = [basePrintName stringByAppendingString:[NSString stringWithFormat:@"%d", i]];
        printer.ip_address = [baseIP stringByAppendingString:[NSString stringWithFormat:@"%d", i]];
        [printerList addObject:printer];
        
        PrintJobHistoryGroup* group = [PrintJobHistoryGroup initWithGroupName:printer.name
                                                                  withGroupIP:printer.ip_address
                                                                 withGroupTag:i];
        
        for(int j = 0; j < TEST_NUM_JOBS[i]; j++){
            PrintJob *job = [PrintJob MR_createEntity];
            job.name = [baseJobName stringByAppendingString:[NSString stringWithFormat:@"%d-%d", i, j]];
            job.result = 0;
            job.date = [NSDate dateWithTimeIntervalSinceNow:j*1000];
            job.printer = printer;
            [group addPrintJob:job];
        }

        [mockListPrintJobHistoryGroup addObject:group];
    }
}

@end
