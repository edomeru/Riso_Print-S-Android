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

@interface PrintersIpadViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UIButton*)addPrinterButton;
- (UIButton*)printerSearchButton;
- (UICollectionView*)collectionView;

// expose private methods
- (BOOL)setDefaultPrinter:(NSIndexPath*)indexPath;

@end

@interface PrinterCollectionViewCell (UnitTest)

// expose private properties
- (UIButton*) deleteButton;
- (BOOL)isDefaultPrinterCell;

// expose private methods
- (IBAction)defaultSwitchAction:(id)sender;

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
    [controller reloadData];
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
    GHAssertNotNil(printerCell.defaultSwitch, @"");
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
    
    GHTestLog(@"-- check cell");
    ibActions = [printerCell gestureRecognizers];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([[ibActions objectAtIndex:0] isKindOfClass:[UITapGestureRecognizer class]], @"");
    
    GHTestLog(@"-- check cell header");
    ibActions = [printerCell.cellHeader gestureRecognizers];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([[ibActions objectAtIndex:0] isKindOfClass:[UILongPressGestureRecognizer class]], @"");
    
    GHTestLog(@"-- check delete button");
    ibActions = [[printerCell deleteButton] actionsForTarget:controller
                                             forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printerDeleteButtonAction:"], @"");
    
    GHTestLog(@"-- check default settings button");
    ibActions = [printerCell.defaultSettingsButton actionsForTarget:controller
                                                    forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"defaultSettingsButtonAction:"], @"");
    
    GHTestLog(@"-- check default printer switch");
    ibActions = [printerCell.defaultSwitch actionsForTarget:printerCell
                                            forControlEvent:UIControlEventValueChanged];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"defaultSwitchAction:"], @"");
    
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
    GHAssertFalse(printerCell.defaultSwitch.on, @"");
    
    [printerCell setAsDefaultPrinterCell:YES];
    GHAssertTrue([printerCell isDefaultPrinterCell], @"");
    GHAssertTrue(printerCell.defaultSwitch.on, @"");
    
    [printerCell setAsDefaultPrinterCell:NO];
    GHAssertFalse([printerCell isDefaultPrinterCell], @"");
    GHAssertFalse(printerCell.defaultSwitch.on, @"");
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
    GHAssertFalse([pm hasDefaultPrinter], @"");
    
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

- (void)test010_DefaultSwitchAction
{
    GHTestLog(@"# CHECK: Default Switch Action.");
    
    GHTestLog(@"-- get PrinterCollectionViewCell");
    UICollectionView* collectionView = [controller collectionView];
    NSIndexPath* index = [NSIndexPath indexPathForItem:0 inSection:0];
    PrinterCollectionViewCell* printerCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                       forIndexPath:index];
    
    UISwitch* defaultSwitch = [[UISwitch alloc] init];
    
    defaultSwitch.on = YES;
    [printerCell defaultSwitchAction:defaultSwitch];
    GHAssertTrue([printerCell isDefaultPrinterCell], @"");
    
    defaultSwitch.on = NO;
    [printerCell defaultSwitchAction:defaultSwitch];
    GHAssertFalse([printerCell isDefaultPrinterCell], @"");
    
    defaultSwitch.on = YES;
    [printerCell defaultSwitchAction:defaultSwitch];
    GHAssertTrue([printerCell isDefaultPrinterCell], @"");
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
    pd1.enBooklet = YES;
    pd1.enStaple = NO;
    pd1.enFinisher23Holes = YES;
    pd1.enFinisher24Holes = NO;
    pd1.enTrayAutoStacking = YES;
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
    pd2.enBooklet = YES;
    pd2.enStaple = YES;
    pd2.enFinisher23Holes = NO;
    pd2.enFinisher24Holes = YES;
    pd2.enTrayAutoStacking = YES;
    pd2.enTrayFaceDown = YES;
    pd2.enTrayStacking = YES;
    pd2.enTrayTop = YES;
    pd2.enLpr = YES;
    pd2.enRaw = YES;
    GHAssertTrue([pm registerPrinter:pd2], @"check functionality of PrinterManager");
    GHAssertTrue(pm.countSavedPrinters == 2, @"");
}

@end
