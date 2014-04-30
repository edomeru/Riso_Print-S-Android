//
//  PrintSettingsPrinterListTableViewControllerTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/30/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintSettingsPrinterListTableViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintDocument.h"

#import "PrintSettingsOptionsItemCell.h"

#define PRINTER_ITEM @"PrinterItem"

@interface PrintSettingsPrinterListTableViewController (Test)
@property (nonatomic) NSUInteger selectedIndex;
@end

@interface PrintSettingsOptionsItemCell(Test)
@property (nonatomic, weak) IBOutlet UIImageView *radioImageView;@end
@interface PrintSettingsPrinterListTableViewControllerTest : GHTestCase

@end
@implementation PrintSettingsPrinterListTableViewControllerTest
{
    NSInteger printerTesDataCount;
    NSString* storyboardId;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;
    NSUInteger testSelectedIndex;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PrintSettingsPrinterListTableViewController";
    
    pdfOriginalFileURL= [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[pdfOriginalFileURL.path lastPathComponent]]];
    
    testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    printerTesDataCount = 5;
    testSelectedIndex = 0;
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
    //remove added test printer data
    while([[PrinterManager sharedPrinterManager] countSavedPrinters] > 0)
    {
        [[PrinterManager sharedPrinterManager] deletePrinterAtIndex:0];
    }
}

- (void)test001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsPrinterListTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.tableView, @"");

    PrintSettingsOptionsItemCell *cell = [viewController.tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM];
    
    GHAssertNotNil(cell, @"");
    GHAssertNotNil(cell.separator, @"");
    GHAssertNotNil(cell.optionLabel,@"");
    GHAssertNotNil(cell.statusView, @"");
    GHAssertNotNil(cell.radioImageView, @"");
    GHAssertNotNil(cell.subLabel, @"");
}

- (void)test002_UIViewLoading
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsPrinterListTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];

    [viewController view];
    
    GHAssertEquals(viewController.selectedIndex, testSelectedIndex, @"");
    NSUInteger rowCount = [viewController.tableView numberOfRowsInSection:0];
    GHAssertEquals(rowCount, [[PrinterManager sharedPrinterManager] countSavedPrinters], @"");

    for(int i = 0; i < rowCount; i++)
    {
        PrintSettingsOptionsItemCell *cell = (PrintSettingsOptionsItemCell *)[viewController.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForItem:i inSection:0]];
        Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:i];
        GHAssertEqualCStrings([cell.optionLabel.text cStringUsingEncoding:NSUTF8StringEncoding], [printer.name cStringUsingEncoding:NSUTF8StringEncoding], @"");
        if(i == testSelectedIndex)
        {
             GHAssertTrue(cell.isSelected, @"");
             GHAssertTrue(cell.radioImageView.isHighlighted, @"");
        }
        else
        {
            GHAssertFalse(cell.isSelected, @"");
            GHAssertFalse(cell.radioImageView.isHighlighted, @"");
        }
    }
}

- (void)test003_selectRow
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsPrinterListTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    [viewController view];
    
    NSUInteger rowCount = [viewController.tableView numberOfRowsInSection:0];
    testSelectedIndex = 2;
    [viewController tableView:viewController.tableView didSelectRowAtIndexPath:[NSIndexPath indexPathForItem:testSelectedIndex inSection:0]];
    
    for(int i = 0; i < rowCount; i++)
    {
        PrintSettingsOptionsItemCell *cell = (PrintSettingsOptionsItemCell *)[viewController.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForItem:i inSection:0]];
        
        if(i == testSelectedIndex)
        {
            GHAssertTrue(cell.isSelected, @"");
            GHAssertTrue(cell.radioImageView.isHighlighted, @"");
        }
        else
        {
            GHAssertFalse(cell.isSelected, @"");
            GHAssertFalse(cell.radioImageView.isHighlighted, @"");
        }
    }
    
    GHAssertEquals(viewController.selectedIndex, testSelectedIndex, @"");
    GHAssertEqualObjects([[PDFFileManager sharedManager] printDocument].printer, [[PrinterManager sharedPrinterManager] getPrinterAtIndex:testSelectedIndex], @"");
};

@end
