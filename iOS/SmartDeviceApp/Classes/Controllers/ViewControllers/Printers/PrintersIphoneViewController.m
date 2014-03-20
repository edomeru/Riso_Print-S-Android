//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "HomeViewController.h"
#import "PrintersIphoneViewController.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "PrinterCell.h"
#import "AlertUtils.h"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()
/**
 Action when the PrinterCell is tapped
 */
- (IBAction)tapPrinterCellAction:(id)sender;


@end

@implementation PrintersIphoneViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Header

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.printerManager.countSavedPrinters;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString* cellIdentifier = PRINTERCELL;
    PrinterCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier
                                                        forIndexPath:indexPath];
    
    Printer* printer = [self.printerManager getPrinterAtIndex:indexPath.row];
    if ([self.printerManager isDefaultPrinter:printer])
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setCellStyleForDefaultCell];
    }
    
    cell.printerName.text = printer.name;
    cell.printerStatus.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.printerStatus.statusHelper.delegate = cell.printerStatus;

    [cell.printerStatus setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.printerStatus.statusHelper startPrinterStatusPolling];
    
    if (indexPath.row == self.printerManager.countSavedPrinters-1)
        [cell.separator setHidden:YES];
    
    return cell;
}

#pragma mark - IBAction
- (IBAction)tapPrinterCellAction:(id)sender
{
    [self removeDeleteState];
    
}

- (IBAction)tapTableView:(id)sender
{
    [self removeDeleteState];
}
- (IBAction)swipePrinterCellAction:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    if(selectedIndexPath != self.toDeleteIndexPath)
    {
        //remove delete state of other cells if any
        [self removeDeleteState];
    }
    PrinterCell  *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
    [cell setCellToBeDeletedState:YES];
    self.toDeleteIndexPath = selectedIndexPath;
}

- (IBAction)deleteButtonAction:(id)sender
{
    if ([self.printerManager deletePrinterAtIndex:self.toDeleteIndexPath.row])
    {
        //check if reference to default printer was also deleted
        if (![self.printerManager hasDefaultPrinter])
            self.defaultPrinterIndexPath = nil;
        
        //set the view of the cell to stop polling for printer status
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell.printerStatus.statusHelper stopPrinterStatusPolling];
        
        //set view to non default printer cell style
        [cell setCellStyleForNormalCell];
        
        //remove cell from view
        [self.tableView deleteRowsAtIndexPaths:@[self.toDeleteIndexPath]
                              withRowAnimation:UITableViewRowAnimationAutomatic];
        
        self.toDeleteIndexPath = nil;
    }
    else
    {
        [AlertUtils displayResult:ERR_DEFAULT
                        withTitle:ALERT_TITLE_PRINTERS
                      withDetails:nil];
    }
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
}

- (void)reloadData
{
    [super reloadData];
    [self.tableView reloadData];
}

#pragma mark - private helper methods
-(void) removeDeleteState
{
    if(self.toDeleteIndexPath != nil)
    {
        PrinterCell *cell   = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
        self.toDeleteIndexPath = nil;
    }
}
@end
