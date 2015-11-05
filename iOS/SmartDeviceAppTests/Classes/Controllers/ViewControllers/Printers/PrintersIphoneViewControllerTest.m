//
//  PrintersIphoneViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintersViewController.h"
#import "PrintersIphoneViewController.h"
#import "PrinterCell.h"
#import "DatabaseManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "PrinterStatusView.h"

@interface PrintersIphoneViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UIButton*)addPrinterButton;
- (UIButton*)printerSearchButton;
- (UITableView*)tableView;

@end

@interface PrinterCell (UnitTest)

// expose private properties
- (UIButton*)deleteButton;
- (UIImageView*)disclosureImage;
- (BOOL)isDefaultPrinterCell;
- (NSLayoutConstraint*)spaceDeleteButtonToSuperview;

@end

@interface PrintersIphoneViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrintersIphoneViewController* controller;
}

@end

@implementation PrintersIphoneViewControllerTest

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
    
    NSString* controllerName = @"PrintersIphoneViewController";
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
    GHAssertNotNil([controller tableView], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controller mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controller
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
    
    UIButton* addPrinterButton = [controller addPrinterButton];
    ibActions = [addPrinterButton actionsForTarget:controller
                                   forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"addPrinterAction:"], @"");
    
    UIButton* printerSearchButton = [controller printerSearchButton];
    ibActions = [printerSearchButton actionsForTarget:controller
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printerSearchAction:"], @"");
}

- (void)test003_UITableViewPopulate
{
    GHTestLog(@"# CHECK: UITableView Population. #");
    NSUInteger expectedPrinters = 2; //--from prepareTestData
    
    GHTestLog(@"-- get PrinterManager");
    PrinterManager* pm = controller.printerManager;
    GHAssertNotNil(pm, @"controller should have a non-nil PrinterManager");
    GHTestLog(@"-- #printers=%lu", (unsigned long)pm.countSavedPrinters);
    GHAssertTrue(pm.countSavedPrinters == expectedPrinters, @"#printers = test data");
    
    GHTestLog(@"-- get UITableView");
    UITableView* tableView = [controller tableView];
    GHTestLog(@"-- #sections=%ld", (long)[tableView numberOfSections]);
    GHAssertTrue([tableView numberOfSections] == 1, @"should only have 1 section");
    GHTestLog(@"-- #rows/section=%ld", (long)[tableView numberOfRowsInSection:0]);
    GHAssertTrue([tableView numberOfRowsInSection:0] == expectedPrinters, @"#rows = #printers");
    
    GHTestLog(@"-- check reloading");
    [controller reloadPrinters];
    GHAssertTrue(pm.countSavedPrinters == expectedPrinters, @"#printers = test data");
    GHAssertTrue([tableView numberOfSections] == 1, @"should only have 1 section");
    GHAssertTrue([tableView numberOfRowsInSection:0] == expectedPrinters, @"#rows = #printers");
}

- (void)test004_PrinterCellOutlets
{
    GHTestLog(@"# CHECK: PrinterCell Outlets. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(printerCell, @"");
    GHAssertNotNil(printerCell.printerName, @"");
    GHAssertNotNil(printerCell.printerStatus, @"");
    GHAssertNotNil(printerCell.separator, @"");
    GHAssertNotNil(printerCell.ipAddress, @"");
    GHAssertNotNil([printerCell deleteButton], @"");
    GHAssertNotNil([printerCell disclosureImage], @"");
}

- (void)test005_PrinterCellActions
{
    GHTestLog(@"# CHECK: PrinterCell Actions. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    NSArray* ibActions;
    
    GHTestLog(@"-- check cell");
    ibActions = [printerCell.contentView gestureRecognizers];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1	, @"");
    GHAssertTrue([[ibActions objectAtIndex:0] isKindOfClass:[UILongPressGestureRecognizer class]], @"");
}

- (void)test006_PrinterCellSetNormal
{
    GHTestLog(@"# CHECK: PrinterCell Set Normal. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    [printerCell setCellStyleForNormalCell];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] < 0, @"");
    GHAssertFalse([[printerCell disclosureImage] isHidden], @"");
    GHAssertFalse([printerCell isDefaultPrinterCell], @"");
}

- (void)test007_PrinterCellSetDefault
{
    GHTestLog(@"# CHECK: PrinterCell Set Default. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    [printerCell setCellStyleForDefaultCell];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] < 0, @"");
    GHAssertFalse([[printerCell disclosureImage] isHidden], @"");
    GHAssertTrue([printerCell isDefaultPrinterCell], @"");
}

- (void)test008_PrinterCellSetDeleteNormal
{
    GHTestLog(@"# CHECK: PrinterCell Set Normal->Delete. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    [printerCell setCellStyleForNormalCell];
    
    [printerCell setCellToBeDeletedState:YES];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] > 0, @"");
    GHAssertTrue([[printerCell disclosureImage] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] < 0, @"");
    GHAssertFalse([[printerCell disclosureImage] isHidden], @"");
}

- (void)test009_PrinterCellSetDeleteDefault
{
    GHTestLog(@"# CHECK: PrinterCell Set Default->Delete. #");
    
    GHTestLog(@"-- get PrinterCell");
    UITableView* tableView = [controller tableView];
    PrinterCell* printerCell = [tableView dequeueReusableCellWithIdentifier:@"PrinterCell"];
    
    [printerCell setCellStyleForDefaultCell];
    
    [printerCell setCellToBeDeletedState:YES];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] > 0, @"");
    GHAssertTrue([[printerCell disclosureImage] isHidden], @"");
    
    [printerCell setCellToBeDeletedState:NO];
    GHAssertTrue([[printerCell spaceDeleteButtonToSuperview] constant] < 0, @"");
    GHAssertFalse([[printerCell disclosureImage] isHidden], @"");
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
