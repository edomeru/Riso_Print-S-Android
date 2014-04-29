//
//  PrintJobHistoryViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryLayout.h"
#import "PrinterManager.h"
#import "DatabaseManager.h"
#import "PrintJobHistoryGroup.h"
#import "PrinterDetails.h"
#import "PrintJob.h"
#import "PrintJobHistoryGroupCell.h"

@interface PrintJobHistoryViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UICollectionView*)groupsView;
- (PrintJobHistoryLayout*)groupsViewLayout;
- (NSMutableArray*)listPrintJobHistoryGroups;

// expose private methods
- (void)findGroupWithTag:(NSInteger)tag outIndex:(NSInteger*)index outGroup:(PrintJobHistoryGroup**)group;

@end

@interface PrintJobHistoryGroupCell (UnitTest)

// expose private properties
- (UIButton*) groupName;
- (UIButton*) groupIP;
- (UIButton*) groupIndicator;
- (UIButton*) deleteAllButton;
- (UITableView*) printJobsView;

@end

@interface PrintJobHistoryViewControllerTest : GHTestCase
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

- (void)test001_IBOutletsBindingIphone
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIphone mainMenuButton], @"");
    GHAssertNotNil([controllerIphone groupsView], @"");
    GHAssertNotNil([controllerIphone groupsViewLayout], @"");
}

- (void)test002_IBOutletsBindingIpad
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIpad mainMenuButton], @"");
    GHAssertNotNil([controllerIpad groupsView], @"");
    GHAssertNotNil([controllerIpad groupsViewLayout], @"");
}

- (void)test003_IBActionsBindingIphone
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controllerIphone mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controllerIphone
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

- (void)test004_IBActionsBindingIpad
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controllerIpad mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controllerIpad
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

- (void)test005_UICollectionViewIphone
{
    GHTestLog(@"# CHECK: UICollectionView. #");
    
    const NSUInteger TEST_NUM_PRINTERS = 8; //--defined in PrintJobHistoryHelper
    
    NSMutableArray* listPrintJobHistoryGroups = [controllerIphone listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    NSUInteger countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    
    GHTestLog(@"-- checking sections");
    UICollectionView* groupsView = [controllerIphone groupsView];
    GHTestLog(@"-- #sections=%u", [groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%u", [groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- checking cell binding");
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrintJobHistoryGroupCell* groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                                forIndexPath:index];
    GHAssertNotNil(groupCell, @"");
    GHAssertNotNil([groupCell groupName], @"");
    GHAssertNotNil([groupCell groupIP], @"");
    GHAssertNotNil([groupCell groupIndicator], @"");
    GHAssertNotNil([groupCell deleteAllButton], @"");
    GHAssertNotNil([groupCell printJobsView], @"");
}

- (void)test006_UICollectionViewIpad
{
    GHTestLog(@"# CHECK: UICollectionView. #");
    
    const NSUInteger TEST_NUM_PRINTERS = 8; //--defined in PrintJobHistoryHelper
    
    NSMutableArray* listPrintJobHistoryGroups = [controllerIpad listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    NSUInteger countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    
    GHTestLog(@"-- checking sections");
    UICollectionView* groupsView = [controllerIpad groupsView];
    GHTestLog(@"-- #sections=%u", [groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%u", [groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
    
    GHTestLog(@"-- checking cell binding");
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrintJobHistoryGroupCell* groupCell = [groupsView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                                forIndexPath:index];
    GHAssertNotNil(groupCell, @"");
    GHAssertNotNil([groupCell groupName], @"");
    GHAssertNotNil([groupCell groupIP], @"");
    GHAssertNotNil([groupCell groupIndicator], @"");
    GHAssertNotNil([groupCell deleteAllButton], @"");
    GHAssertNotNil([groupCell printJobsView], @"");
}

- (void)test007_FindGroupWithTag
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
        
        NSString* tagString = [NSString stringWithFormat:@"%u", group.tag];
        
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
         
         GHTestLog(@"--- actual=[%u]", groupFoundIdx);
         GHTestLog(@"--- expected=[%u]", [expectedIdx integerValue]);
         GHAssertTrue(groupFoundIdx == [expectedIdx integerValue], @"");
    }];
    
    // list is unchanged
    GHAssertTrue([listPrintJobHistoryGroups count] == countGroups, @"list of groups should be unchanged");
}

@end
