//
//  PrintersIpadViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintersViewController.h"
#import "PrintersIpadViewController.h"
#import "PrinterCollectionViewCell.h"
#import "DatabaseManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "PrinterStatusView.h"
#import "DeleteButton.h"
#import "SearchSettingsViewController.h"
#import "OCMock.h"

@interface PrintersIpadViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UIButton*)addPrinterButton;
- (UIButton*)printerSearchButton;
- (UICollectionView*)collectionView;
- (UIButton*)searchSettingsButton;

// expose private methods
- (BOOL)setDefaultPrinter:(NSIndexPath*)indexPath;
- (IBAction)defaultPrinterSwitchAction:(id)sender;
- (IBAction)defaultPrinterSelectionAction:(id)sender;
- (IBAction)unwindToPrinters:(UIStoryboardSegue *)sender;


@end

@interface PrinterCollectionViewCell (UnitTest)

// expose private properties
- (UIButton*) deleteButton;
- (BOOL)isDefaultPrinterCell;

@end

@interface PrintersIpadViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrintersIpadViewController* controller;
}

@end

@implementation PrintersIpadViewControllerTest

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
    
    [self prepareTestData];
    
    NSString* controllerName = @"PrintersIpadViewController";
    controller = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controller, @"unable to instantiate controller (%@)", controllerName);
    GHAssertNotNil(controller.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controller = nil;
    
    // remove all test data
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
    
    GHAssertNotNil([controller mainMenuButton], @"");
    GHAssertNotNil([controller addPrinterButton], @"");
    GHAssertNotNil([controller printerSearchButton], @"");
    GHAssertNotNil([controller collectionView], @"");
    GHAssertNotNil([controller searchSettingsButton], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    GHTestLog(@"-- check main menu button");
    UIButton* mainMenuButton = [controller mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controller
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
    
    GHTestLog(@"-- check add printer button");
    UIButton* addPrinterButton = [controller addPrinterButton];
    ibActions = [addPrinterButton actionsForTarget:controller
                                   forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"addPrinterAction:"], @"");
    
    GHTestLog(@"-- check printer search button");
    UIButton* printerSearchButton = [controller printerSearchButton];
    ibActions = [printerSearchButton actionsForTarget:controller
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printerSearchAction:"], @"");
    
    GHTestLog(@"-- check search settings button");
    UIButton* searchSettingsButton = [controller searchSettingsButton];
    ibActions = [searchSettingsButton actionsForTarget:controller
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"searchSettingsAction:"], @"");
}

- (void)test003_UICollectionViewPopulate
{
    GHTestLog(@"# CHECK: UICollectionView Population. #");
    NSUInteger expectedPrinters = 2; //--from prepareTestData
    
    GHTestLog(@"-- get PrinterManager");
    PrinterManager* pm = controller.printerManager;
    GHAssertNotNil(pm, @"controller should have a non-nil PrinterManager");
    GHTestLog(@"-- #printers=%lu", (unsigned long)pm.countSavedPrinters);
    GHAssertTrue(pm.countSavedPrinters == expectedPrinters, @"#printers = test data");
    
    GHTestLog(@"-- get UICollectionView");
    UICollectionView* collectionView = [controller collectionView];
    GHTestLog(@"-- #sections=%ld", (long)[collectionView numberOfSections]);
    GHAssertTrue([collectionView numberOfSections] == 1, @"should only have 1 section");
    GHTestLog(@"-- #items/section=%ld", (long)[collectionView numberOfItemsInSection:0]);
    GHAssertTrue([collectionView numberOfItemsInSection:0] == expectedPrinters, @"#items = #printers");
    
    GHTestLog(@"-- check reloading");
    [controller reloadPrinters];
    GHAssertTrue(pm.countSavedPrinters == expectedPrinters, @"#printers = test data");
    GHAssertTrue([collectionView numberOfSections] == 1, @"should only have 1 section");
    GHAssertTrue([collectionView numberOfItemsInSection:0] == expectedPrinters, @"#items = #printers");
}

- (void)test004_PrinterCollectionViewCellOutlets
{
    GHTestLog(@"# CHECK: PrinterCollectionViewCell Outlets. #");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(printerCell, @"");
    GHAssertNotNil(printerCell.nameLabel, @"");
    GHAssertNotNil(printerCell.ipAddressLabel, @"");
    GHAssertNotNil(printerCell.portSelection, @"");
    GHAssertNotNil(printerCell.defaultPrinterSelection, @"");
    GHAssertNotNil(printerCell.statusView, @"");
    GHAssertNotNil(printerCell.cellHeader, @"");
    GHAssertNotNil(printerCell.defaultSettingsButton, @"");
    GHAssertNotNil([printerCell deleteButton], @"");
}

- (void)test005_PrinterCollectionViewCellActions
{
    GHTestLog(@"# CHECK: PrinterCollectionViewCell Actions. #");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    
    NSArray* ibActions;
        
    GHTestLog(@"-- check default printer switch");
    ibActions = [printerCell.defaultPrinterSelection actionsForTarget:controller
                                            forControlEvent:UIControlEventValueChanged];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"defaultPrinterSelectionAction:"], @"");
    
    GHTestLog(@"-- check port selection switch");
    ibActions = [printerCell.portSelection actionsForTarget:controller
                                            forControlEvent:UIControlEventValueChanged];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"portSelectionAction:"], @"");
}

- (void)test006_SetCellDefault
{
    GHTestLog(@"# CHECK: PrinterCollectionViewCell Default. #");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    
    [printerCell setAsDefaultPrinterCell:NO];
    GHAssertFalse([printerCell isDefaultPrinterCell], @"");
    GHAssertFalse(printerCell.defaultPrinterSelection.selectedSegmentIndex == 0, @"");
    
    [printerCell setAsDefaultPrinterCell:YES];
    GHAssertTrue([printerCell isDefaultPrinterCell], @"");
    GHAssertTrue(printerCell.defaultPrinterSelection.selectedSegmentIndex == 0, @"");
    
    [printerCell setAsDefaultPrinterCell:NO];
    GHAssertFalse([printerCell isDefaultPrinterCell], @"");
    GHAssertFalse(printerCell.defaultPrinterSelection.selectedSegmentIndex == 0, @"");
}

- (void)test007_SetCellDeleteNormal
{
    GHTestLog(@"# CHECK: PrinterCollectionViewCell Delete. #");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell deleteButton] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:YES];
    GHAssertFalse([[printerCell deleteButton] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell deleteButton] isHidden], @"");
}

- (void)test008_SetCellDeleteDefault
{
    GHTestLog(@"# CHECK: PrinterCollectionViewCell Delete. #");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    
    [printerCell setAsDefaultPrinterCell:YES];
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell deleteButton] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:YES];
    GHAssertFalse([[printerCell deleteButton] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell deleteButton] isHidden], @"");
}

- (void)test009_SetDefaultPrinter
{
    GHTestLog(@"# CHECK: Set Default Printer. #");
    
    NSIndexPath* indexPath;
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    //GHAssertFalse([pm hasDefaultPrinter], @"");
    
    GHTestLog(@"-- set 1st printer as the default");
    indexPath = [NSIndexPath indexPathForItem:0 inSection:0];
    GHAssertTrue([controller setDefaultPrinter:indexPath], @"");
    GHAssertTrue([pm hasDefaultPrinter], @"");
    GHAssertTrue([pm isDefaultPrinter:[pm getPrinterAtIndex:0]], @"");
    GHAssertFalse([pm isDefaultPrinter:[pm getPrinterAtIndex:1]], @"");
    
    GHTestLog(@"-- set 2nd printer as the default");
    indexPath = [NSIndexPath indexPathForItem:1 inSection:0];
    GHAssertTrue([controller setDefaultPrinter:indexPath], @"");
    GHAssertTrue([pm hasDefaultPrinter], @"");
    GHAssertFalse([pm isDefaultPrinter:[pm getPrinterAtIndex:0]], @"");
    GHAssertTrue([pm isDefaultPrinter:[pm getPrinterAtIndex:1]], @"");
}

- (void)test010_DefaultPrinterSelectionAction
{
    GHTestLog(@"# CHECK: Default Switch Action.");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    
    UISegmentedControl* defaultPrinterSelection = [[UISegmentedControl alloc] init];
    
    defaultPrinterSelection.selectedSegmentIndex = 0;
    [controller defaultPrinterSelectionAction:defaultPrinterSelection];
    GHAssertTrue(controller.defaultPrinterIndexPath.row == index.row, @"");
    
    defaultPrinterSelection.selectedSegmentIndex = 1;
    [controller defaultPrinterSelectionAction:defaultPrinterSelection];
    GHAssertNil(controller.defaultPrinterIndexPath, @"");
    
    defaultPrinterSelection.selectedSegmentIndex = 0;
    [controller defaultPrinterSelectionAction:defaultPrinterSelection];
    GHAssertTrue(controller.defaultPrinterIndexPath.row == index.row, @"");
}

- (void)test011_SearchSettingsAction
{
    id mockController = OCMPartialMock(controller);
    [[mockController expect] performSegueTo:[SearchSettingsViewController class]];
    
    [mockController searchSettingsAction:[controller searchSettingsButton]];
    
    GHAssertFalse([[mockController searchSettingsButton] isEnabled], @"");
    
    [mockController verify];
    
    [mockController stopMocking];
}

- (void)test012_UnwindFromSearchSettings
{
    id mockStoryBoardSegue = OCMClassMock([UIStoryboardSegue class]);
    [[[mockStoryBoardSegue stub] andReturn:[[SearchSettingsViewController alloc] init]] sourceViewController];
    
    [[controller searchSettingsButton] setEnabled:NO];
    
    [controller unwindToPrinters: mockStoryBoardSegue];
    
    GHAssertTrue([[controller searchSettingsButton] isEnabled], @"");
    
    [mockStoryBoardSegue stopMocking];
}


#pragma mark - Utilities

- (void)prepareTestData
{
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    //-- register a valid printer
    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
    pd1.name = @"RISO Printer 1";
    pd1.ip = @"192.168.0.199";
    pd1.port = [NSNumber numberWithInt:0];
    pd1.enBookletFinishing = YES;
    pd1.enStaple = NO;
    pd1.enFinisher23Holes = YES;
    pd1.enFinisher24Holes = NO;
    pd1.enTrayFaceDown = NO;
    pd1.enTrayStacking = YES;
    pd1.enTrayTop = NO;
    pd1.enLpr = YES;
    pd1.enRaw = NO;
    GHAssertTrue([pm registerPrinter:pd1], @"check functionality of PrinterManager");
    GHAssertTrue(pm.countSavedPrinters == 1, @"");

    //-- register an invalid printer
    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
    //pd2.name should be nil
    pd2.ip = @"127.0.0.1";
    pd2.port = [NSNumber numberWithInt:0];
    pd2.enBookletFinishing = YES;
    pd2.enStaple = YES;
    pd2.enFinisher23Holes = NO;
    pd2.enFinisher24Holes = YES;
    pd2.enTrayFaceDown = YES;
    pd2.enTrayStacking = YES;
    pd2.enTrayTop = YES;
    pd2.enLpr = YES;
    pd2.enRaw = YES;
    GHAssertTrue([pm registerPrinter:pd2], @"check functionality of PrinterManager");
    GHAssertTrue(pm.countSavedPrinters == 2, @"");
}

@end
