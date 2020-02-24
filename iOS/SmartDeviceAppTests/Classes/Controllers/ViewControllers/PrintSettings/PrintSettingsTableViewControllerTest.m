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
#import "PrintSettingsHelper.h"
#import "OCMock.h"

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
- (BOOL)isSettingOptionSupported:(NSString *) option;
- (void)fillSupportedSettings;
- (void)reloadRowsForIndexPathsToUpdate;

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

    int noNameIndex = 3;
    int nilIndex = 4;
    for(int i = 0; i < printerTesDataCount; i++)
    {
        PrinterDetails *pd = [[PrinterDetails alloc] init];
        pd.enBookletFinishing = YES;
        pd.enFinisher23Holes = NO;
        pd.enFinisher24Holes = YES;
        pd.enStaple = YES;
        pd.enTrayStacking = YES;
        pd.enTrayFaceDown = YES;
        pd.enTrayTop = YES;
        if(i == noNameIndex)
        {
            pd.name = @"";;
        }
        else if (i == nilIndex)
        {
            pd.name = nil;
        }
        else
        {
            pd.name = [NSString stringWithFormat:@"Printer %d", i];
        }
 
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
    
    UITableViewCell *cell = [viewController.tableView dequeueReusableCellWithIdentifier:SETTING_HEADER_CELL_ID];
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

- (void)test002_UIViewLoading_InPrintPreviewPrintSetting_AllSupported
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertNotNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printDocument, [[PDFFileManager sharedManager] printDocument], @"");
    
    GHAssertEqualObjects(viewController.printer, [[PDFFileManager sharedManager] printDocument].printer, @"");
    GHAssertFalse(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");
    
    NSDictionary *settingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [settingsTree objectForKey:@"group"];
    
    NSInteger sectionCount = [viewController.tableView numberOfSections];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:1 inSection:0];
    
    
    GHAssertEquals((NSUInteger)sectionCount, [groups count] + 1, @"");
    for(NSUInteger sectionNumber = 0; sectionNumber < sectionCount - 1 && (sectionNumber - 1) < [groups count]; sectionNumber++)
    {
        NSDictionary *group = [groups objectAtIndex:sectionNumber - 1];
        NSArray *settings = [group objectForKey:@"setting"];
        NSInteger rowCount = [viewController.tableView numberOfRowsInSection:sectionNumber];
        
        GHAssertEquals((NSUInteger)rowCount, [settings count] + 1, @"");
        
        indexPath = [NSIndexPath indexPathForRow:0 inSection:sectionNumber];
        [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
        
        PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[viewController.tableView cellForRowAtIndexPath:indexPath];
        
        NSString* compareString = NSLocalizedString([[group objectForKey:@"text"] uppercaseString], @"");
        GHAssertEqualStrings(headerCell.groupLabel.text, [compareString uppercaseString], @"");
        
        for(NSUInteger rowNumber = 1; rowNumber< rowCount && (rowNumber - 1) < [settings count]; rowNumber++)
        {
            indexPath = [NSIndexPath indexPathForRow:rowNumber inSection:sectionNumber];
            [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
            NSDictionary* setting = [settings objectAtIndex:rowNumber - 1];
            UITableViewCell *cell = [viewController.tableView cellForRowAtIndexPath:indexPath];
            NSString *type = [setting objectForKey:@"type"];
            if([type isEqualToString:@"list"])
            {
                PrintSettingsItemOptionCell* optionCell = (PrintSettingsItemOptionCell*)cell;
                GHAssertEqualStrings(optionCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
            if([type isEqualToString:@"numeric"])
            {
                PrintSettingsItemInputCell* inputCell = (PrintSettingsItemInputCell*)cell;
                GHAssertEqualStrings(inputCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
            if([type isEqualToString:@"switch"])
            {
                PrintSettingsItemSwitchCell*switchCell = (PrintSettingsItemSwitchCell*)cell;
                GHAssertEqualStrings(switchCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
        }
    }
}

- (void)test003_UIViewLoading_InDefaultPrintSetting_AllSupported
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printer ,[[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex], @"");
    
    GHAssertTrue(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");
    
    NSDictionary *settingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [settingsTree objectForKey:@"group"];
    
    NSInteger sectionCount = [viewController.tableView numberOfSections];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
    
    
    GHAssertEquals((NSUInteger)sectionCount, [groups count], @"");
    for(NSUInteger sectionNumber = 0; sectionNumber < sectionCount && (sectionNumber - 1) < [groups count]; sectionNumber++)
    {
        NSDictionary *group = [groups objectAtIndex:sectionNumber - 1];
        NSArray *settings = [group objectForKey:@"setting"];
        NSInteger rowCount = [viewController.tableView numberOfRowsInSection:sectionNumber];
        
        GHAssertEquals((NSUInteger)rowCount, [settings count] + 1, @"");
        
        indexPath = [NSIndexPath indexPathForRow:0 inSection:sectionNumber];
        [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
        
        PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *)[viewController.tableView cellForRowAtIndexPath:indexPath];

        NSString* compareString = NSLocalizedString([[group objectForKey:@"text"] uppercaseString], @"");
        GHAssertEqualStrings(headerCell.groupLabel.text, [compareString uppercaseString], @"");
        
        for(NSUInteger rowNumber = 1; rowNumber< rowCount && (rowNumber - 1) < [settings count]; rowNumber++)
        {
            indexPath = [NSIndexPath indexPathForRow:rowNumber inSection:sectionNumber];
             [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
            NSDictionary* setting = [settings objectAtIndex:rowNumber - 1];
            UITableViewCell *cell = [viewController.tableView cellForRowAtIndexPath:indexPath];
            NSString *type = [setting objectForKey:@"type"];
            if([type isEqualToString:@"list"])
            {
                PrintSettingsItemOptionCell* optionCell = (PrintSettingsItemOptionCell*)cell;
                GHAssertEqualStrings(optionCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
            if([type isEqualToString:@"numeric"])
            {
                PrintSettingsItemInputCell* inputCell = (PrintSettingsItemInputCell*)cell;
                GHAssertEqualStrings(inputCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
            if([type isEqualToString:@"switch"])
            {
                PrintSettingsItemSwitchCell*switchCell = (PrintSettingsItemSwitchCell*)cell;
                GHAssertEqualStrings(switchCell.settingLabel.text, NSLocalizedString([[setting objectForKey:@"text"] uppercaseString], @""), @"");
            }
        }
    }
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
    
    GHAssertNotNil(viewController.view, @"");

#ifndef DISABLE_FAILED_TESTS
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex];
    printer.enabled_booklet_finishing = [NSNumber numberWithBool:NO];
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET],@"");
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET_LAYOUT],@"");
    GHAssertFalse([viewController isSettingSupported:KEY_BOOKLET_FINISH],@"");
    
    printer.enabled_booklet_finishing = [NSNumber numberWithBool:YES];
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
#endif //DISABLE_FAILED_TESTS
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
    //GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_3holes"],@"");
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_punch_4holes"],@"");
    
    GHAssertNotNil(viewController.view, @"");
    
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:testPrinterIndex];
    printer.enabled_tray_face_down = [NSNumber numberWithBool:NO];
    GHAssertTrue([viewController isSettingOptionSupported:@"ids_lbl_outputtray_facedown"],@"");
    
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
    
    GHAssertNotNil(viewController.view, @"");
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = YES;
    previewSetting.imposition = kImpositionOff;
    GHAssertTrue([viewController isSettingEnabled:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_BOOKLET_FINISH],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_DUPLEX],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_FINISHING_SIDE],@"");
    GHAssertTrue([viewController isSettingEnabled:KEY_IMPOSITION],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_STAPLE],@"");
    GHAssertFalse([viewController isSettingEnabled:KEY_PUNCH],@"");
    
    previewSetting.imposition= kImposition2Pages;
    previewSetting.booklet = YES;
    GHAssertFalse([viewController isSettingEnabled:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    previewSetting.booklet = YES;
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
    
    GHAssertNotNil(viewController.view, @"");
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = YES;
    previewSetting.imposition = kImpositionOff;
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_FINISH],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_DUPLEX],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_FINISHING_SIDE],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_STAPLE],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_PUNCH],@"");
    
    previewSetting.imposition = kImposition2Pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertTrue([viewController isSettingApplicable:KEY_IMPOSITION_ORDER],@"");
    
    previewSetting.booklet = NO;
    previewSetting.imposition = kImpositionOff;
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_LAYOUT],@"");
    GHAssertTrue([viewController isSettingApplicable:KEY_BOOKLET_FINISH],@"");
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

- (void)test008_selectRowHeadersExpanded
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    GHAssertNotNil(viewController.tableView, @"");
    GHAssertNotNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printDocument, [[PDFFileManager sharedManager] printDocument], @"");
    
    GHAssertEqualObjects(viewController.printer, [[PDFFileManager sharedManager] printDocument].printer, @"");
    GHAssertFalse(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");
    
    NSInteger sectionCount = [viewController.tableView numberOfSections];
    
    for(NSInteger index = 1; index < sectionCount; index++)
    {
        NSIndexPath *headerIndexPath = [NSIndexPath indexPathForRow:0 inSection:index];
        [viewController tableView:viewController.tableView didSelectRowAtIndexPath:headerIndexPath];
        UITableViewCell * cell = [viewController.tableView cellForRowAtIndexPath:headerIndexPath];
        PrintSettingsHeaderCell *headerCell = (PrintSettingsHeaderCell *) cell;
        GHAssertFalse(headerCell.expanded, @"");
    }
}

- (void)test009_changePrinter
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    GHAssertNotNil(viewController.tableView, @"");
    GHAssertNotNil(viewController.printDocument, @"");
    GHAssertNotNil(viewController.previewSetting, @"");
    GHAssertEqualObjects(viewController.printDocument, [[PDFFileManager sharedManager] printDocument], @"");
    
    GHAssertEqualObjects(viewController.printer, [[PDFFileManager sharedManager] printDocument].printer, @"");
    GHAssertFalse(viewController.isDefaultSettingsMode, @"");
    GHAssertNotNil(viewController.printSettingsTree, @"");

    [[PDFFileManager sharedManager] printDocument].printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:1];
    GHAssertEqualObjects(viewController.printer, [[PDFFileManager sharedManager] printDocument].printer, @"");
}

-(void)test010_applyConstraints_Imposition
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    GHAssertNotNil(viewController.view, @"");
    
    //scroll view to set layout section in middle
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:2 inSection:2];
    [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = NO;
    previewSetting.imposition = kImposition4pages;

    GHAssertEquals(previewSetting.impositionOrder, (NSInteger) kImpositionOrderUpperLeftToRight, @"");
    
    previewSetting.impositionOrder = kImpositionOrderUpperRightToBottom;
    previewSetting.imposition = kImposition2Pages;
    GHAssertEquals(previewSetting.impositionOrder, (NSInteger)kImpositionOrderRightToLeft, @"");
    
    previewSetting.imposition = kImposition4pages;
    GHAssertEquals(previewSetting.impositionOrder, (NSInteger)kImpositionOrderUpperRightToLeft, @"");
    
    previewSetting.impositionOrder = kImpositionOrderUpperLeftToBottom;
    previewSetting.imposition = kImposition2Pages;
    GHAssertEquals(previewSetting.impositionOrder, (NSInteger)kImpositionOrderLeftToRight, @"");
    
    previewSetting.impositionOrder = kImpositionOrderRightToLeft;
    previewSetting.imposition = kImpositionOff;
    GHAssertEquals(previewSetting.impositionOrder, (NSInteger)kImpositionOrderLeftToRight, @"");
    
    previewSetting.imposition = kImposition4pages + 1;
    GHAssertEquals(previewSetting.imposition, (NSInteger)kImpositionOff, @"");
}

-(void)test011_applyConstraints_FinishingSideToStaple
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    NSUInteger testPrinterIndex = 1;
    
    viewController.printerIndex = [NSNumber numberWithUnsignedInteger:testPrinterIndex];
    
    GHAssertNotNil(viewController.view, @"");
    
    //scroll view to set layout section in middle
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:2 inSection:2];
    [viewController.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
    
    PreviewSetting *previewSetting = viewController.previewSetting;
    
    previewSetting.booklet = NO;
    previewSetting.imposition = kImpositionOff;
    previewSetting.finishingSide = kFinishingSideLeft;
    previewSetting.staple = kStapleType1Pos;
    
    previewSetting.finishingSide = kFinishingSideTop;
    GHAssertEquals(previewSetting.staple, (NSInteger)kStapleTypeUpperLeft, @"");
    
    previewSetting.finishingSide = kFinishingSideRight;
    GHAssertEquals(previewSetting.staple, (NSInteger)kStapleType1Pos, @"");
    
    previewSetting.finishingSide = kFinishingSideTop;
    GHAssertEquals(previewSetting.staple, (NSInteger)kStapleTypeUpperRight, @"");
    
    previewSetting.finishingSide = kFinishingSideRight + 1;
    GHAssertEquals(previewSetting.finishingSide, (NSInteger)kFinishingSideTop, @"");
}


-(void)test015_viewDidAppear
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    
    id mockViewController = OCMPartialMock(viewController);
    
    [mockViewController setValue:[NSNumber numberWithBool:YES] forKey:@"isRedrawFullSettingsTable"];
    
    [[mockViewController expect] fillSupportedSettings];
    
    [mockViewController viewDidAppear:NO];
    
    BOOL isRedrawFullSettingsTable = [[mockViewController valueForKey:@"isRedrawFullSettingsTable"] boolValue];

    GHAssertFalse(isRedrawFullSettingsTable, @"");
    
    [mockViewController verify];
    
    [[mockViewController expect] reloadRowsForIndexPathsToUpdate];
    [[mockViewController reject] fillSupportedSettings];
    
    [mockViewController viewDidAppear:NO];
    
    [mockViewController verify];
    
    [mockViewController stopMocking];
}


@end
