//
//  PrintSettingsPrinterListTableViewController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsPrinterListTableViewController.h"
#import "PrinterManager.h"
#import "PrintSettingsOptionsItemCell.h"
#import "Printer.h"

#define PRINTERS_HEADER_CELL @"PrintersHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItem"

@interface PrintSettingsPrinterListTableViewController ()
@property (weak, nonatomic) PrinterManager *printerManager;
@property (nonatomic) NSInteger selectedIndex;
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
    return [self.printerManager countSavedPrinters] + 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    
    if (indexPath.row == 0)
    {
        UITableViewCell *headerCell = [tableView dequeueReusableCellWithIdentifier:PRINTERS_HEADER_CELL forIndexPath:indexPath];
        headerCell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell = headerCell;
    }
    else
    {
        PrintSettingsOptionsItemCell *itemCell = [tableView dequeueReusableCellWithIdentifier:PRINTER_ITEM_CELL forIndexPath:indexPath];
        Printer *printer = [self.printerManager getPrinterAtIndex:indexPath.row - 1];
        itemCell.optionLabel.text = printer.name;
        itemCell.separator.hidden = NO;
        if (indexPath.row == [self.printerManager countSavedPrinters])
        {
            itemCell.separator.hidden = YES;
        }
        
        if(printer == self.selectedPrinter)
        {
            self.selectedIndex = indexPath.row - 1;
            [tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
        }
        
        cell = itemCell;
    }
    
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)
    {
        [tableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:self.selectedIndex + 1 inSection:0] animated:NO scrollPosition:UITableViewScrollPositionNone];
        return;
    }
    
    int index = indexPath.row - 1;
    if (index != self.selectedIndex || self.selectedPrinter == nil)
    {
        self.selectedIndex = index;
        self.selectedPrinter = [self.printerManager getPrinterAtIndex:index];
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
