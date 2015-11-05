//
//  PrintJobHistoryViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
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
    PrintJobHistoryViewController* controllerIphone;
    PrintJobHistoryViewController* controllerIpad;
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
    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerIphoneName = @"PrintJobHistoryIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    
    NSString* controllerIpadName = @"PrintJobHistoryIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
    
    // remove all test data
    // (this will remove the auto-generated test printers and jobs since Use_PrintJobHistoryData=YES)
    [DatabaseManager discardChanges];
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
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

- (void)test001_IBOutletsBinding
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIphone mainMenuButton], @"");
    GHAssertNotNil([controllerIphone groupsView], @"");
    GHAssertNotNil([controllerIphone groupsViewLayout], @"");
   
    GHAssertNotNil([controllerIpad mainMenuButton], @"");
    GHAssertNotNil([controllerIpad groupsView], @"");
    GHAssertNotNil([controllerIpad groupsViewLayout], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButtonIphone = [controllerIphone mainMenuButton];
    ibActions = [mainMenuButtonIphone actionsForTarget:controllerIphone
                                       forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
    
    UIButton* mainMenuButtonIpad = [controllerIpad mainMenuButton];
    ibActions = [mainMenuButtonIpad actionsForTarget:controllerIpad
                                     forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

- (void)test003_UICollectionView
{
    GHTestLog(@"# CHECK: UICollectionView. #");
    const NSUInteger TEST_NUM_PRINTERS = 8; //--defined in PrintJobHistoryHelper
    
    NSMutableArray* listPrintJobHistoryGroups;
    NSUInteger countGroups;
    UICollectionView* groupsView;

    GHTestLog(@"-- List of Print Jobs (iPhone)");
    listPrintJobHistoryGroups = [controllerIphone listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    
    GHTestLog(@"-- UICollectionView (iPhone-Portrait)");
    [[controllerIphone groupsViewLayout] setupForOrientation:UIInterfaceOrientationPortrait
                                                   forDevice:UIUserInterfaceIdiomPhone];
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIphone groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- UICollectionView (iPhone-Landscape)");
    [[controllerIphone groupsViewLayout] setupForOrientation:UIInterfaceOrientationLandscapeLeft
                                                   forDevice:UIUserInterfaceIdiomPhone];
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIphone groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- List of Print Jobs (iPad)");
    listPrintJobHistoryGroups = [controllerIpad listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    
    GHTestLog(@"-- UICollectionView (iPad-Portrait)");
    [[controllerIphone groupsViewLayout] setupForOrientation:UIInterfaceOrientationPortrait
                                                   forDevice:UIUserInterfaceIdiomPad];
    
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIpad groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- UICollectionView (iPad-Landscape)");
    [[controllerIphone groupsViewLayout] setupForOrientation:UIInterfaceOrientationLandscapeLeft
                                                   forDevice:UIUserInterfaceIdiomPad];
    
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIpad groupsView];
    GHTestLog(@"-- #sections=%ld", (long)[groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%ld", (long)[groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
}

- (void)test004_FindGroupWithTag
{
    GHTestLog(@"# CHECK: Find Group With Tag. #");
    
    NSMutableArray* listPrintJobHistoryGroups = [controllerIphone listPrintJobHistoryGroups];
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
        [controllerIphone findGroupWithTag:[tag integerValue]   //same method for iPad, no need to test twice
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
         [controllerIphone findGroupWithTag:[tag integerValue]   //same method for iPad, no need to test twice
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
    groupsView = [controllerIphone groupsView];
    GHTestLog(@"-- checking cell bindings");
    index = [NSIndexPath indexPathForItem:0 inSection:0];
    groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL forIndexPath:index];
    GHAssertNotNil(groupCell, @"");
    GHAssertNotNil([groupCell groupName], @"");
    GHAssertNotNil([groupCell groupIP], @"");
    GHAssertNotNil([groupCell groupIndicator], @"");
    GHAssertNotNil([groupCell deleteAllButton], @"");
    GHAssertNotNil([groupCell printJobsView], @"");
    
    GHTestLog(@"-- UICollectionView (iPad)");
    groupsView = [controllerIpad groupsView];
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
    
    groupsView = [controllerIphone groupsView];
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
    
    groupsView = [controllerIpad groupsView];
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
    
    groupsView = [controllerIpad groupsView];
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

@end
