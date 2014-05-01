//
//  PrintSettingsTableViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintSettingsTableViewController.h"
#import "PrintDocument.h"
#import "PrintSettingsPrinterHeaderCell.h"
#import "PrintSettingsPrinterItemCell.h"
#import "PrintSettingsHeaderCell.h"
#import "PrintSettingsItemSwitchCell.h"
#import "PrintSettingsItemInputCell.h"
#import "PrintSettingsItemOptionCell.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintPreviewHelper.h"

#define PRINTER_HEADER_CELL_ID @"PrinterHeaderCell"
#define PRINTER_ITEM_CELL_ID @"PrinterItemCell"
#define PRINTER_ITEM_DEFAULT_CELL_ID @"PrinterItemDefaultCell"
#define SETTING_HEADER_CELL_ID @"SettingHeaderCell"
#define SETTING_ITEM_OPTION_CELL_ID @"SettingItemOptionCell"
#define SETTING_ITEM_INPUT_CELL_ID @"SettingItemInputCell"
#define SETTING_ITEM_SWITCH_CELL_ID @"SettingItemSwitchCell"
#define PINCODE_INPUT_CELL_ID @"PincodeInputCell"

@interface PrintSettingsTableViewController(Test)

@property (nonatomic) BOOL isDefaultSettingsMode;
@property (nonatomic, strong) PreviewSetting *previewSetting;
@property (nonatomic, strong) PrintDocument *printDocument;
@property (nonatomic, weak) Printer *printer;
@property (nonatomic, weak) NSDictionary *printSettingsTree;
@property (nonatomic, strong) NSMutableArray *expandedSections;
@property (nonatomic, weak) NSDictionary *currentSetting;
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, strong) NSMutableDictionary *textFieldBindings;
@property (nonatomic, strong) NSMutableDictionary *switchBindings;
@property (nonatomic, strong) NSMutableArray *supportedSettings;
@property (nonatomic, strong) NSMutableDictionary *indexPathsForSettings;
@property (nonatomic, strong) NSMutableArray *indexPathsToUpdate;
@property (nonatomic) BOOL isRedrawFullSettingsTable;

- (BOOL)isSettingEnabled:(NSString*)settingKey;
- (BOOL)isSettingApplicable:(NSString*)settingKey;
- (BOOL)isSettingSupported:(NSString*)settingKey;
-(BOOL) isSettingOptionSupported:(NSString *) option;

@end

@interface PrintSettingsTableViewControllerTest : GHTestCase

@end

@implementation PrintSettingsTableViewControllerTest
{
    NSInteger printerTesDataCount;
    NSString* storyboardId;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PrintSettingsTableViewController";
    
    pdfOriginalFileURL= [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[pdfOriginalFileURL.path lastPathComponent]]];
    
    testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    printerTesDataCount = 5;
}

- (void) setUp
{
    NSError *error;
    [[NSFileManager defaultManager] copyItemAtPath:[pdfOriginalFileURL path] toPath: [testURL path] error:&error];

    //setup managers to init with data
    //add test printer data
    for(int i = 0; i < printerTesDataCount; i++)
    {
        PrinterDetails *pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"Printer %d", i];
        pd.ip = [NSString stringWithFormat:@"192.168.2.%d", i];
        [[PrinterManager sharedPrinterManager] registerPrinter:pd];
    }
    //create test default printer
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:0];
    [[PrinterManager sharedPrinterManager] registerDefaultPrinter:printer];
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
}

-(void)tearDown
{
    while([[PrinterManager sharedPrinterManager] countSavedPrinters] > 0)
    {
        [[PrinterManager sharedPrinterManager] deletePrinterAtIndex:0];
    }
}

- (void)test001_UIViewBindings
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.tableView, @"");
    
    UITableViewCell *cell =[viewController.tableView dequeueReusableCellWithIdentifier:PRINTER_HEADER_CELL_ID];
     GHAssertNotNil(cell, @"");
    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_CELL_ID];
    GHAssertNotNil(cell, @"");
    
    PrintSettingsPrinterItemCell *printerItemCell = (PrintSettingsPrinterItemCell *)cell;
    GHAssertNotNil(printerItemCell.printerIPLabel, @"");
    GHAssertNotNil(printerItemCell.printerNameLabel, @"");
    GHAssertNotNil(printerItemCell.selectPrinterLabel, @"");

    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_DEFAULT_CELL_ID];
    GHAssertNotNil(cell, @"");
    
    printerItemCell = (PrintSettingsPrinterItemCell *)cell;
    GHAssertNotNil(printerItemCell.printerIPLabel, @"");
    GHAssertNotNil(printerItemCell.printerNameLabel, @"");
    
    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL_ID];
    GHAssertNotNil(cell, @"");
    PrintSettingsHeaderCell *printSettingsHeaderCell = (PrintSettingsHeaderCell *)cell;
    GHAssertNotNil(printSettingsHeaderCell.groupLabel, @"");

    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_INPUT_CELL_ID];
    GHAssertNotNil(cell, @"");
    PrintSettingsItemInputCell *itemInputCell = (PrintSettingsItemInputCell *) cell;
    GHAssertNotNil(itemInputCell.settingLabel, @"");
    GHAssertNotNil(itemInputCell.valueTextField, @"");
    GHAssertNotNil(itemInputCell.separator, @"");
    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_OPTION_CELL_ID];
    GHAssertNotNil(cell, @"");
    PrintSettingsItemOptionCell *itemOptionCell = (PrintSettingsItemOptionCell *)cell;
    GHAssertNotNil(itemOptionCell.settingLabel, @"");
    GHAssertNotNil(itemOptionCell.valueLabel, @"");
    GHAssertNotNil(itemOptionCell.separator, @"");
    GHAssertNotNil(itemOptionCell.subMenuImage, @"");
    
    cell =[viewController.tableView dequeueReusableCellWithIdentifier:SETTING_ITEM_SWITCH_CELL_ID];
    GHAssertNotNil(cell, @"");
    PrintSettingsItemSwitchCell *itemSwitchCell = (PrintSettingsItemSwitchCell *) cell;
    GHAssertNotNil(itemSwitchCell.settingLabel, @"");
    GHAssertNotNil(itemSwitchCell.valueSwitch, @"");
    GHAssertNotNil(itemOptionCell.separator, @"");
}

- (void)test002_UIViewLoading_InPrintPreviewPrintSetting
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    [viewController view];
    
    GHAssertNotNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printDocument, [[PDFFileManager sharedManager] printDocument], @"");
    
    GHAssertEqualObjects(viewController.printer, [[PDFFileManager sharedManager] printDocument].printer, @"");
    GHAssertFalse(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");
}

- (void)test003_UIViewLoading_InDefaultPrintSetting
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    [viewController view];
    
    GHAssertNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printer ,[[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex], @"");
    
    GHAssertTrue(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");
}

-(void)test004_isSettingSupported
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    //printer is still nil - all supported
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET_FINISH],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_STAPLE],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_PUNCH],@"");
    
    [viewController view];
    
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex];
    printer.enabled_booklet = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET],@"");
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET_LAYOUT],@"");
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET_FINISH],@"");
    
    printer.enabled_booklet = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingSupported:KEY_BOOKLET_FINISH],@"");
    
    printer.enabled_staple = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingSupported:KEY_STAPLE],@"");

    printer.enabled_staple = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingSupported:KEY_STAPLE],@"");

    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:NO];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingSupported:KEY_PUNCH],@"");
    
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:YES];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:NO];
    GHAssertTrue([viewController isSettingSupported:KEY_PUNCH],@"");
    
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:NO];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingSupported:KEY_PUNCH],@"");
}

-(void)test005_isSettingOptionSupported
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_facedown"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_top"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_stacking"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_2holes"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_3holes"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_4holes"],@"");
    
    [viewController view];
    
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex];
    printer.enabled_tray_face_down = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_outputtray_facedown"],@"");
    
    printer.enabled_tray_face_down = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_facedown"],@"");
    
    printer.enabled_tray_top = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_outputtray_top"],@"");
    
    printer.enabled_tray_top = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_top"],@"");
    
    printer.enabled_tray_stacking = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_outputtray_stacking"],@"");
    
    printer.enabled_tray_stacking= [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_stacking"],@"");
    
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:NO];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_punch_2holes"],@"");
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_punch_4holes"],@"");
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_punch_3holes"],@"");
    
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:YES];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:NO];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_2holes"],@"");
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_punch_4holes"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_3holes"],@"");
    
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:NO];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:YES];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_2holes"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_4holes"],@"");
    GHAssertFalse([viewController isSettingOptionSupported:@"ids_lbl_punch_3holes"],@"");
}

-(void)test006_isSettingEnabled
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    [viewController view];
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = YES;
    previewSetting.imposition = kImpositionOff;
    GHAssertTrue([viewController isSettingEnabled:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_BOOKLET_FINISH],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_DUPLEX],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_FINISHING_SIDE],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_STAPLE],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_PUNCH],@"");
    
    previewSetting.imposition= kImposition2Pages;
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.booklet = NO;
    previewSetting.imposition = kImpositionOff;
    GHAssertFalse([viewController isSettingEnabled:KEY_BOOKLET_LAYOUT],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_BOOKLET_FINISH],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_DUPLEX],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_FINISHING_SIDE],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_IMPOSITION],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_STAPLE],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_PUNCH],@"");
    
    previewSetting.imposition = kImposition2Pages;
    GHAssertTrue([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertTrue([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
}

-(void)test007_isSettingApplicable
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    [viewController view];
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = YES;
    previewSetting.imposition = kImpositionOff;
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_FINISH],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_DUPLEX],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_FINISHING_SIDE],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_IMPOSITION],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_STAPLE],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_PUNCH],@"");
    
    previewSetting.imposition = kImposition2Pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.booklet = NO;
    previewSetting.imposition = kImpositionOff;
    GHAssertFalse([viewController isSettingApplicable:KEY_BOOKLET_LAYOUT],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_BOOKLET_FINISH],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_DUPLEX],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_FINISHING_SIDE],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION],@"");
    GHAssertFalse([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_STAPLE],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_PUNCH],@"");
    
    previewSetting.imposition = kImposition2Pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
}
@end
