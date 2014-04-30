//
//  PrintSettingsTableViewController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/29/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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

- (void)executePrint;
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

@end
