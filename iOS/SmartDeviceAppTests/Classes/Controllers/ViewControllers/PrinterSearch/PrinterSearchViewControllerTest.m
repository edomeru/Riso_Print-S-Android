//
//  PrinterSearchViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterSearchViewController.h"
#import "SearchResultCell.h"
#import "PrinterDetails.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"

@interface PrinterSearchViewController (UnitTest)

// expose private properties
- (UITableView*)tableView;
- (PrinterManager*)printerManager;
- (NSMutableArray*)listPrinterIP;
- (NSMutableDictionary*)listPrinterDetails;

// expose private methods
- (void)addPrinter:(NSUInteger)row;

@end

@interface SearchResultCell (UnitTest)

// expose private properties
- (UILabel*)printerIP;
- (UILabel*)printerName;
- (UIView*)separator;
- (UIButton*)oldIcon;
- (UIButton*)addIcon;

@end

@interface PrinterSearchViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrinterSearchViewController* controllerIphone;
    PrinterSearchViewController* controllerIpad;
}

@end

@implementation PrinterSearchViewControllerTest

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
    
    NSString* controllerIphoneName = @"PrinterSearchIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    [[controllerIphone printerManager] stopSearching];
    
    NSString* controllerIpadName = @"PrinterSearchIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
    [[controllerIpad printerManager] stopSearching];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
    
    SNMPManager* sm = [SNMPManager sharedSNMPManager];
    [sm cancelSearch];
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
    
    GHAssertNotNil([controllerIphone tableView], @"");
}

- (void)test002_IBOutletsBindingIpad
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIpad tableView], @"");
}

- (void)test003_UITableViewCellIphone
{
    GHTestLog(@"# CHECK: UITableViewCell Outlets.");
    
    GHTestLog(@"-- get SearchResultCell");
    UITableView* tableView = [controllerIphone tableView];
    SearchResultCell* cell = [tableView dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cell, @"");
    GHAssertNotNil([cell printerIP], @"");
    GHAssertNotNil([cell printerName], @"");
    GHAssertNotNil([cell separator], @"");
    GHAssertNotNil([cell oldIcon], @"");
    GHAssertNotNil([cell addIcon], @"");
}

- (void)test004_UITableViewCellIpad
{
    GHTestLog(@"# CHECK: UITableViewCell Outlets.");
    
    GHTestLog(@"-- get SearchResultCell");
    UITableView* tableView = [controllerIpad tableView];
    SearchResultCell* cell = [tableView dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cell, @"");
    GHAssertNotNil([cell printerIP], @"");
    GHAssertNotNil([cell printerName], @"");
    GHAssertNotNil([cell separator], @"");
    GHAssertNotNil([cell oldIcon], @"");
    GHAssertNotNil([cell addIcon], @"");
}

@end
