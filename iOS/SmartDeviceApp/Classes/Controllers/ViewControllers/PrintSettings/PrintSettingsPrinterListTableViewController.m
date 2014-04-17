//
//  PrintSettingsPrinterListTableViewController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsPrinterListTableViewController.h"
#import "PrintSettingsOptionsItemCell.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "Printer.h"

#define PRINTERS_HEADER_CELL @"PrintersHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItem"

@interface PrintSettingsPrinterListTableViewController ()
@property (weak, nonatomic) PrinterManager *printerManager;
@property (weak, nonatomic) PrintDocument *printDocument;
@property (nonatomic) NSUInteger selectedIndex;
@end

@implementation PrintSettingsPrinterListTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.printerManager = [PrinterManager sharedPrinterManager];
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    
    NSUInteger printerCount = self.printerManager.countSavedPrinters;
    for (NSUInteger i = 0; i < printerCount; i++)
    {
        Printer *printer = [self.printerManager getPrinterAtIndex:i];
        if ([self.printDocument.printer isEqual:printer])
        {
            self.selectedIndex = i;
            break;
        }
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [self.printerManager countSavedPrinters];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    PrintSettingsOptionsItemCell *itemCell = [tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_CELL forIndexPath:indexPath];
    Printer *printer = [self.printerManager getPrinterAtIndex:indexPath.row];
    itemCell.optionLabel.text = printer.name;
    itemCell.separator.hidden = NO;
    if (indexPath.row == [self.printerManager countSavedPrinters])
    {
        itemCell.separator.hidden = YES;
    }
    
    if ([self.printDocument.printer isEqual:printer])
    {
        self.selectedIndex = indexPath.row;
        [tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
    }
    
    return itemCell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger index = indexPath.row;
    if (index != self.selectedIndex || self.printDocument.printer == nil)
    {
        self.selectedIndex = index;
        Printer *printer = [self.printerManager getPrinterAtIndex:index];
        self.printDocument.printer = printer;
    }
}

#pragma mark - Navigation
/*
// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}

 */

@end
