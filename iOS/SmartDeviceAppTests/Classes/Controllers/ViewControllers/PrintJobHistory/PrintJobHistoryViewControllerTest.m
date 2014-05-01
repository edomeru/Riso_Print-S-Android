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
    NSIndexPath* index;
    PrintJobHistoryGroupCell* groupCell;

    GHTestLog(@"-- UICollectionView (iPhone)");
    
    listPrintJobHistoryGroups = [controllerIphone listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIphone groupsView];
    GHTestLog(@"-- #sections=%u", [groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%u", [groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
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
    
    listPrintJobHistoryGroups = [controllerIpad listPrintJobHistoryGroups];
    GHAssertNotNil(listPrintJobHistoryGroups, @"");
    countGroups = [listPrintJobHistoryGroups count];
    GHAssertTrue(countGroups == TEST_NUM_PRINTERS, @"should be equal to defined test data");
    GHTestLog(@"-- checking sections");
    groupsView = [controllerIpad groupsView];
    GHTestLog(@"-- #sections=%u", [groupsView numberOfSections]);
    GHAssertTrue([groupsView numberOfSections] == 1, @"");
    GHTestLog(@"-- #items/section=%u", [groupsView numberOfItemsInSection:0]);
    GHAssertTrue([groupsView numberOfItemsInSection:0] == countGroups, @"");
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

- (void)test005_DisplayInvalidPrinter
{
    GHTestLog(@"# CHECK: Display No Name Printer. #");
    
    NSMutableArray* listGroups = [controllerIphone listPrintJobHistoryGroups];
    NSUInteger countGroups = [listGroups count];
    
    // add another group (nil name)
    PrintJobHistoryGroup* group1 = [PrintJobHistoryGroup initWithGroupName:nil
                                                               withGroupIP:@"888.88.8.1"
                                                              withGroupTag:801];
    PrintJob* job1 = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
    job1.name = @"Job 1";
    //no need to set printer, will be discarded later
    [group1 addPrintJob:job1];
    [listGroups addObject:group1];
    
    // add another group (empty name)
    PrintJobHistoryGroup* group2 = [PrintJobHistoryGroup initWithGroupName:@""
                                                               withGroupIP:@"888.88.8.2"
                                                              withGroupTag:802];
    [listGroups addObject:group2];
    
    GHAssertTrue([listGroups count] == countGroups+2, @"");
    [[controllerIphone groupsView] reloadData];
    GHAssertTrue([listGroups count] == countGroups+2, @"");
    
    // check that nil/@"" group names are displayed properly
    NSIndexPath* indexPath;
    countGroups = [listGroups count];
    PrintJobHistoryGroupCell* groupCell;
    for (NSInteger item = 0; item < countGroups; item++)
    {
        indexPath = [NSIndexPath indexPathForItem:item inSection:0];
        groupCell = (PrintJobHistoryGroupCell*)[[controllerIphone groupsView] cellForItemAtIndexPath:indexPath];
        NSString* groupIP = [[[groupCell groupIP] titleLabel] text];
        if ([groupIP hasPrefix:@"888"])
        {
            GHAssertEqualStrings([groupCell groupName], NSLocalizedString(@"IDS_LBL_NO_NAME", @""), @"");
        }
        else
            continue;
    }
}

@end
