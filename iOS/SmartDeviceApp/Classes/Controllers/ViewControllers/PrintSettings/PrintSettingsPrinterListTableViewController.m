//
//  PrintSettingsPrinterListTableViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsPrinterListTableViewController.h"
#import "PrintSettingsOptionsItemCell.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "Printer.h"
#import "PrinterStatusHelper.h"
#import "PrinterStatusView.h"

#define PRINTERS_HEADER_CELL @"PrintersHeaderCell"
#define PRINTER_ITEM_CELL @"PrinterItem"

@interface PrintSettingsPrinterListTableViewController ()
@property (weak, nonatomic) PrinterManager *printerManager;
@property (weak, nonatomic) PrintDocument *printDocument;
@property (nonatomic) NSUInteger selectedIndex;
@property (nonatomic, strong) NSMutableArray *statusHelpers;
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
    self.statusHelpers = [[NSMutableArray alloc] init];
    
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

-(void) dealloc
{
    for(PrinterStatusHelper * statusHelper in self.statusHelpers)
    {
        [statusHelper stopPrinterStatusPolling];
    }
    [self.statusHelpers removeAllObjects];
    
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
    if(printer.name == nil || [printer.name isEqualToString:@""])
    {
        itemCell.optionLabel.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
    }
    else
    {
        itemCell.optionLabel.text = printer.name;
    }
    
    if([self.statusHelpers count] <= indexPath.row)
    {
        PrinterStatusHelper *printerStatusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
        printerStatusHelper.delegate = self;
        [printerStatusHelper startPrinterStatusPolling];
        [self.statusHelpers addObject:printerStatusHelper];
    }
    
    itemCell.subLabel.text = printer.ip_address;
    itemCell.separator.hidden = NO;
    if (indexPath.row == [self.printerManager countSavedPrinters] - 1)
    {
        itemCell.separator.hidden = YES;
    }
    
    [itemCell.statusView setStatus:[printer.onlineStatus boolValue]];
    
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

#pragma mark - PrinterStatusDelegate
-(void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange :(BOOL)isOnline
{
    NSUInteger index = [self.statusHelpers indexOfObject:statusHelper];
    Printer *printer = [self.printerManager getPrinterAtIndex:index];
    
    printer.onlineStatus = [NSNumber numberWithBool:isOnline];
    PrintSettingsOptionsItemCell *cell = (PrintSettingsOptionsItemCell *)[self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0]];
    if(cell != nil) //cell returned will be nil if cell for row is not visible
    {
        [cell.statusView setStatus:isOnline];
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
