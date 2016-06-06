//
//  PrinterInfoViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrinterInfoViewController.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "DatabaseManager.h"

#define VALID_PRINTER_NAME  @"RISO Printer 1"
#define VALID_PRINTER_IP    @"192.168.0.199"
#define VALID_PRINTER_IDX   0

#define INVALID_PRINTER_IP  @"127.0.0.1"
#define INVALID_PRINTER_IDX 1

@interface PrinterInfoViewController (UnitTest)

// expose private properties
- (UILabel*)printerName;
- (UILabel*)ipAddress;
- (UISegmentedControl*)portSelection;
- (UISegmentedControl*)defaultPrinterSelection;

@end

@interface PrinterInfoViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrinterInfoViewController* controllerValid;
    PrinterInfoViewController* controllerInvalid;
}

@end

@implementation PrinterInfoViewControllerTest

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
    
    NSString* controllerName = @"PrinterInfoViewController";
    
    controllerValid = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controllerValid, @"unable to instantiate controller (%@)", controllerName);
    controllerValid.indexPath = [NSIndexPath indexPathForRow:VALID_PRINTER_IDX inSection:0];
    GHAssertNotNil(controllerValid.view, @"");
    
    controllerInvalid = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controllerInvalid, @"unable to instantiate controller (%@)", controllerName);
    controllerInvalid.indexPath = [NSIndexPath indexPathForRow:INVALID_PRINTER_IDX inSection:0];
    GHAssertNotNil(controllerInvalid.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerValid = nil;
    controllerInvalid = nil;
    
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
    
    GHAssertNotNil([controllerValid printerName], @"");
    GHAssertNotNil([controllerValid ipAddress], @"");
    GHAssertNotNil([controllerValid portSelection], @"");
    GHAssertNotNil([controllerValid defaultPrinterSelection], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    ibActions = [[controllerValid portSelection] actionsForTarget:controllerValid
                                                  forControlEvent:UIControlEventValueChanged];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"selectPortAction:"], @"");
    
    ibActions = [[controllerValid defaultPrinterSelection] actionsForTarget:controllerValid
                                                         forControlEvent:UIControlEventValueChanged];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"defaultPrinterSelectionAction:"], @"");
}

- (void)test003_DisplayValidPrinter
{
    GHTestLog(@"# CHECK: Name of a Valid Printer");
    
    UILabel* printerName = [controllerValid printerName];
    GHAssertEqualStrings(printerName.text, VALID_PRINTER_NAME, @"");
    
    UILabel* ipAddress = [controllerValid ipAddress];
    GHAssertEqualStrings(ipAddress.text, VALID_PRINTER_IP, @"");
}

- (void)test004_DisplayInvalidPrinter
{
    GHTestLog(@"# CHECK: Name of an Invalid Printer");
    
    UILabel* printerName = [controllerInvalid printerName];
    GHAssertEqualStrings(printerName.text, NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name"), @"");
    
    UILabel* ipAddress = [controllerInvalid ipAddress];
    GHAssertEqualStrings(ipAddress.text, INVALID_PRINTER_IP, @"");
}

#pragma mark - Utilities

- (void)prepareTestData
{
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    //-- register a valid printer
    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
    pd1.name = VALID_PRINTER_NAME;
    pd1.ip = VALID_PRINTER_IP;
    pd1.port = [NSNumber numberWithInt:1];
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
    GHAssertTrue(pm.countSavedPrinters == VALID_PRINTER_IDX+1, @"");
    
    //-- register an invalid printer
    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
    //pd2.name should be nil
    pd2.ip = INVALID_PRINTER_IP;
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
    GHAssertTrue(pm.countSavedPrinters == INVALID_PRINTER_IDX+1, @"");
}

@end
