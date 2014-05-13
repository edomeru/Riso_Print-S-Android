//
//  PrinterSearchViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrinterSearchViewController.h"
#import "SearchResultCell.h"
#import "PrinterDetails.h"
#import "CXAlertView.h"
#import "Swizzler.h"
#import "PrinterManager.h"
#import "PrinterManagerMock.h"

@interface PrinterSearchViewController (UnitTest)

// expose private properties

- (UITableView*)tableView;
- (UIRefreshControl*)refreshControl;

// expose private methods
- (void)refresh;

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
    Swizzler* swizzler = [[Swizzler alloc] init];
    
    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerIphoneName = @"PrinterSearchIphoneViewController";
    [swizzler swizzleInstanceMethod:[PrinterManager class] targetSelector:@selector(searchForAllPrinters) swizzleClass:[PrinterManagerMock class] swizzleSelector:@selector(searchForAllPrintersSuccessful)];
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    [swizzler deswizzle];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
    
    NSString* controllerIpadName = @"PrinterSearchIpadViewController";
    [swizzler swizzleInstanceMethod:[PrinterManager class] targetSelector:@selector(searchForAllPrinters) swizzleClass:[PrinterManagerMock class] swizzleSelector:@selector(searchForAllPrintersSuccessful)];
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
    [swizzler deswizzle];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
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
    NSIndexPath* indexPath;
    SearchResultCell* cell;
    
    GHAssertNotNil([controllerIphone tableView], @"");
    GHAssertNil(controllerIphone.printersViewController, @"will only be non-nil on segue from Printers");
    GHTestLog(@"-- get the first printer");
    indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    cell = (SearchResultCell*)[[controllerIphone tableView] cellForRowAtIndexPath:indexPath];
    GHAssertNotNil(cell, @"");
    
    GHAssertNotNil([controllerIpad tableView], @"");
    GHAssertNil(controllerIpad.printersViewController, @"will only be non-nil on segue from Printers");
    indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    cell = (SearchResultCell*)[[controllerIphone tableView] cellForRowAtIndexPath:indexPath];
    GHAssertNotNil(cell, @"");
}

- (void)test002_AddPrinter
{
    GHTestLog(@"# CHECK: Add Printer.");
    UITableView* listPrinters;
    NSUInteger numPrinters = 0;
    NSIndexPath* indexPath;
    SearchResultCell* cell;

    GHTestLog(@"-- checking the list");
    listPrinters = [controllerIphone tableView];
    GHAssertNotNil(listPrinters, @"");
    numPrinters = [listPrinters numberOfRowsInSection:0];
    GHAssertTrue(numPrinters > 0, @"");
    
    GHTestLog(@"-- adding the first printer");
    indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    [controllerIphone tableView:listPrinters didSelectRowAtIndexPath:indexPath];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
    [self removeSuccessAlert];
    
    GHTestLog(@"-- checking the list");
    listPrinters = [controllerIphone tableView];
    GHAssertNotNil(listPrinters, @"");
    numPrinters = [listPrinters numberOfRowsInSection:0];
    GHAssertTrue(numPrinters > 0, @"the added printer must be in the list");
    indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    cell = (SearchResultCell*)[[controllerIphone tableView] cellForRowAtIndexPath:indexPath];
    GHAssertNotNil(cell, @"");
    
    GHAssertTrue(controllerIphone.hasAddedPrinters, @"");
}

- (void)test003_AddPrinterButMaximum
{
    GHTestLog(@"# CHECK: Add Printer With Maximum.");
    UITableView* listPrinters;
    NSUInteger numPrinters = 0;
    NSIndexPath* indexPath;
    SearchResultCell* cell;
    
    GHTestLog(@"-- maxing-out the printers list");
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 10)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = @"RISO Printer";
        pd.ip = @"192.168.100.104";
        GHAssertTrue([pm registerPrinter:pd], @"");
    }
    
    GHTestLog(@"-- checking the list");
    listPrinters = [controllerIphone tableView];
    GHAssertNotNil(listPrinters, @"");
    numPrinters = [listPrinters numberOfRowsInSection:0];
    GHAssertTrue(numPrinters > 0, @"");
    
    GHTestLog(@"-- adding the last printer");
    indexPath = [NSIndexPath indexPathForRow:numPrinters-1 inSection:0];
    [controllerIphone tableView:listPrinters didSelectRowAtIndexPath:indexPath];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
    [self removeSuccessAlert];
    
    GHTestLog(@"-- checking the list");
    listPrinters = [controllerIphone tableView];
    GHAssertNotNil(listPrinters, @"");
    numPrinters = [listPrinters numberOfRowsInSection:0];
    GHAssertTrue(numPrinters > 0, @"the added printer must be in the list");
    indexPath = [NSIndexPath indexPathForRow:numPrinters-1 inSection:0];
    cell = (SearchResultCell*)[[controllerIphone tableView] cellForRowAtIndexPath:indexPath];
    GHAssertNotNil(cell, @"");
}

- (void)test004_Refresh
{
    GHTestLog(@"# CHECK: Refresh.");
    UITableView* listPrinters;
    Swizzler* swizzler = [[Swizzler alloc] init];
    
    GHTestLog(@"-- refreshing");
    [swizzler swizzleInstanceMethod:[PrinterManager class] targetSelector:@selector(searchForAllPrinters) swizzleClass:[PrinterManagerMock class] swizzleSelector:@selector(searchForAllPrintersSuccessful)];
    [controllerIphone refresh];
    [swizzler deswizzle];
    [self waitForCompletion:5 withMessage:nil]; //wait for the refresh to end
    listPrinters = [controllerIphone tableView];
    GHAssertNotNil(listPrinters, @"");
}

- (void)test005_SearchResultCellOutlets
{
    GHTestLog(@"# CHECK: SearchResultCell Outlets.");
    
    GHTestLog(@"-- get SearchResultCell (iPhone)");
    UITableView* tableViewIphone = [controllerIphone tableView];
    SearchResultCell* cellIphone = [tableViewIphone dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cellIphone, @"");
    GHAssertNotNil([cellIphone printerIP], @"");
    GHAssertNotNil([cellIphone printerName], @"");
    GHAssertNotNil([cellIphone separator], @"");
    GHAssertNotNil([cellIphone oldIcon], @"");
    GHAssertNotNil([cellIphone addIcon], @"");
    
    GHTestLog(@"-- get SearchResultCell (iPad)");
    UITableView* tableViewIpad = [controllerIpad tableView];
    SearchResultCell* cellIPad = [tableViewIpad dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cellIPad, @"");
    GHAssertNotNil([cellIPad printerIP], @"");
    GHAssertNotNil([cellIPad printerName], @"");
    GHAssertNotNil([cellIPad separator], @"");
    GHAssertNotNil([cellIPad oldIcon], @"");
    GHAssertNotNil([cellIPad addIcon], @"");
}

- (void)test006_SearchResultCellSetters
{
    GHTestLog(@"# CHECK: SearchResultCell Setters.");
    NSString* printerName = @"RISO Printer";
    NSString* printerIP = @"192.168.0.199";
    
    GHTestLog(@"-- get SearchResultCell (iPhone)");
    UITableView* tableViewIphone = [controllerIphone tableView];
    SearchResultCell* cellIphone = [tableViewIphone dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    [cellIphone setCellAsOldResult];
    GHAssertFalse([[cellIphone oldIcon] isHidden], @"");
    GHAssertTrue([[cellIphone addIcon] isHidden], @"");
    
    [cellIphone setCellAsNewResult];
    GHAssertTrue([[cellIphone oldIcon] isHidden], @"");
    GHAssertFalse([[cellIphone addIcon] isHidden], @"");
    
    [cellIphone setContentsUsingName:printerName usingIP:printerIP];
    GHAssertEqualStrings([[cellIphone printerName] text], printerName, @"");
    GHAssertEqualStrings([[cellIphone printerIP] text], printerIP, @"");
    
    [cellIphone setContentsUsingName:nil usingIP:printerIP];
    GHAssertEqualStrings([[cellIphone printerName] text], NSLocalizedString(@"IDS_LBL_NO_NAME", "No name"),  @"");
    [cellIphone setContentsUsingName:@"" usingIP:printerIP];
    GHAssertEqualStrings([[cellIphone printerName] text], NSLocalizedString(@"IDS_LBL_NO_NAME", "No name"),  @"");
    
    [cellIphone setStyle:NO]; //not last cell
    GHAssertFalse([[cellIphone separator] isHidden], @"");
    
    [cellIphone setStyle:YES]; //yes last cell
    GHAssertTrue([[cellIphone separator] isHidden], @"");
    
    GHTestLog(@"-- get SearchResultCell (iPad)");
    UITableView* tableViewIpad = [controllerIpad tableView];
    SearchResultCell* cellIpad = [tableViewIpad dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    [cellIpad setCellAsOldResult];
    GHAssertFalse([[cellIpad oldIcon] isHidden], @"");
    GHAssertTrue([[cellIpad addIcon] isHidden], @"");
    
    [cellIpad setCellAsNewResult];
    GHAssertTrue([[cellIpad oldIcon] isHidden], @"");
    GHAssertFalse([[cellIpad addIcon] isHidden], @"");
    
    [cellIpad setContentsUsingName:printerName usingIP:printerIP];
    GHAssertEqualStrings([[cellIpad printerName] text], printerName, @"");
    GHAssertEqualStrings([[cellIpad printerIP] text], printerIP, @"");
    
    [cellIpad setStyle:NO]; //not last cell
    GHAssertFalse([[cellIpad separator] isHidden], @"");
    
    [cellIpad setStyle:YES]; //yes last cell
    GHAssertTrue([[cellIpad separator] isHidden], @"");
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    UIAlertView* alert;

    if (msg != nil)
    {
        alert = [[UIAlertView alloc] initWithTitle:@"Printer Search Test"
                                           message:msg
                                          delegate:self
                                 cancelButtonTitle:@"HIDE"
                                 otherButtonTitles:nil];
        [alert show];
    }
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    if (msg != nil)
        [alert dismissWithClickedButtonIndex:0 animated:YES];
    
    return done;
}

- (void)removeSuccessAlert
{
    for (UIWindow* window in [UIApplication sharedApplication].windows)
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                [alert dismiss];
                [self waitForCompletion:2 withMessage:nil];
                alert = nil;
            }
        }
    }
}

@end
