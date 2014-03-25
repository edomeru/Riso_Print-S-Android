//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIphoneViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterCell.h"
#import "AlertUtils.h"

#define SEGUE_TO_PRINTER_INFO   @"PrintersIphone-PrinterInfo"
#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()

#pragma mark - Data Properties

/** NSIndexPath of the tapped printer cell **/
@property (strong, nonatomic) NSIndexPath *selectedPrinterIndexPath;

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UITableView *tableView;

#pragma mark - Instance Methods

/**
 Action when the PrinterCell is tapped to segue to the PrinterInfo screen
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

#pragma mark - IBActions

- (IBAction)tapPrinterCellAction:(id)sender
{
    //if a cell is in delete state, remove delete state
    if(self.toDeleteIndexPath != nil)
    {
        [self removeDeleteState];
        return;
    }
    //else segue to printer info screen
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    self.selectedPrinterIndexPath = indexPath;
    [self performSegueTo:[PrinterInfoViewController class]];
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
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTER_INFO])
    {
        PrinterInfoViewController *destController = [segue destinationViewController];
        destController.indexPath = self.selectedPrinterIndexPath;
        destController.isDefaultPrinter = NO;
        if(self.defaultPrinterIndexPath != nil &&  self.selectedPrinterIndexPath.row == self.defaultPrinterIndexPath.row)
        {
            destController.isDefaultPrinter = YES;
        }
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        destController.onlineStatus = cell.printerStatus.onlineStatus;
        cell.printerStatus.statusHelper.delegate = destController;
    }
}

- (IBAction)unwindFromPrinterInfo:(UIStoryboardSegue*)unwindSegue
{
    if ([unwindSegue.sourceViewController isKindOfClass:[PrinterInfoViewController class]])
    {
        PrinterInfoViewController* printerInfoScreen = (PrinterInfoViewController*)unwindSegue.sourceViewController;
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        [cell.printerStatus.statusHelper startPrinterStatusPolling];
        [self setPrinterCell:printerInfoScreen.indexPath asDefault: printerInfoScreen.isDefaultPrinter];
        self.selectedPrinterIndexPath = nil;
    }
}

#pragma mark - Reload

- (void)reloadData
{
    [super reloadData];
    [self.tableView reloadData];
}

#pragma mark - private helper methods

-(void) setPrinterCell:(NSIndexPath *) indexPath asDefault: (BOOL) isDefault
{
    if(self.defaultPrinterIndexPath != nil)
    {
        //don't do anything if cell is still the default cell
        //or if the the cell is not the default cell and is being set to not default
        if((self.defaultPrinterIndexPath.row == indexPath.row && isDefault == YES)
           || (self.defaultPrinterIndexPath.row != indexPath.row && isDefault == NO))
        {
            return;
        }
        //unselect the previous default cell
        PrinterCell *previousDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.defaultPrinterIndexPath];
        [previousDefaultCell setCellStyleForNormalCell];
        self.defaultPrinterIndexPath = nil;
    }
    
    if(isDefault == NO)
    {
        return;
    }

    //set the formatting of the selected cell to the default printer cell
    self.defaultPrinterIndexPath = indexPath;
    PrinterCell *selectedDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:indexPath];
    [selectedDefaultCell setCellStyleForDefaultCell];
}

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
