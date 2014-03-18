//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "HomeViewController.h"
#import "PrintersIphoneViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "PrinterCell.h"
#import "AlertUtils.h"

#define SEGUE_TO_ADD_PRINTER    @"PrintersIphone-AddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersIphone-PrinterSearch"
#define SEGUE_TO_PRINTER_INFO   @"PrintersIphone-PrinterInfo"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()
/**
 Action when the PrinterCell is tapped to segue to the PrinterInfo screen
 */
- (IBAction)tapPrinterCellAction:(id)sender;

/** NSIndexPath of the tapped printer cell **/
@property (strong, nonatomic) NSIndexPath *selectedPrinterIndexPath;
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
    return [self.printerManager.listSavedPrinters count];
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
    
    if (indexPath.row == [self.printerManager.listSavedPrinters count]-1)
        [cell.separator setHidden:YES];
    
    return cell;
}

#pragma mark - IBAction
- (IBAction)tapPrinterCellAction:(id)sender
{
    NSLog(@"[INFO][Printers] PrinterCell Tapped");
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    self.selectedPrinterIndexPath = indexPath;
    [self removeDeleteState];
    [self performSegueTo:[PrinterInfoViewController class]];
}

- (IBAction)tapTableView:(id)sender
{
    [self removeDeleteState];
}
- (IBAction)swipePrinterCellAction:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    
    PrinterCell *cell = nil;
    if(self.toDeleteIndexPath != nil)
    {
        NSLog(@"Not nil");
        cell   = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
    }
    
    cell   = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
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
        [AlertUtils displayResult:ERR_DEFAULT withTitle:ALERT_PRINTER withDetails:nil];
    }
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:SEGUE_TO_ADD_PRINTER])
    {
        AddPrinterViewController* destController = [segue destinationViewController];
        
        //give the child screen a reference to the printer manager
        destController.printerManager = self.printerManager;
    }
    
    if ([segue.identifier isEqualToString:SEGUE_TO_PRINTER_SEARCH])
    {
        PrinterSearchViewController* destController = [segue destinationViewController];
        
        //give the child screen a reference to the printer manager
        destController.printerManager = self.printerManager;
    }
    
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTER_INFO])
    {
        PrinterInfoViewController *destController = [segue destinationViewController];
        destController.indexPath = self.selectedPrinterIndexPath;
        destController.isDefaultPrinter = NO;
        if(self.defaultPrinterIndexPath != nil &&  self.selectedPrinterIndexPath.row == self.defaultPrinterIndexPath.row)
        {
            destController.isDefaultPrinter = YES;
                
        }
        destController.delegate = self;
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        cell.printerStatus.statusHelper.delegate = destController;
        
    }
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue*)unwindSegue
{
    UIViewController* sourceViewController = [unwindSegue sourceViewController];
    
    if ([sourceViewController isKindOfClass:[HomeViewController class]])
    {
        [self.mainMenuButton setEnabled:YES];
    }
    else if ([sourceViewController isKindOfClass:[AddPrinterViewController class]])
    {
        [self.addPrinterButton setEnabled:YES];
        
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
    else if ([sourceViewController isKindOfClass:[PrinterSearchViewController class]])
    {
        [self.printerSearchButton setEnabled:YES];
        
        PrinterSearchViewController* adderScreen = (PrinterSearchViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
    if ([sourceViewController isKindOfClass:[PrinterInfoViewController class]])
    {
        PrinterInfoViewController* printerInfoScreen = (PrinterInfoViewController*)sourceViewController;
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        cell.printerStatus.statusHelper.delegate = cell.printerStatus;
        [self setPrinterCell:printerInfoScreen.indexPath asDefault: printerInfoScreen.isDefaultPrinter];
        self.selectedPrinterIndexPath = nil;
    }
}

- (IBAction)unwindFromSlidingDrawer:(UIStoryboardSegue *)segue
{
}

#pragma mark - private helper methods
-(void) setPrinterCell:(NSIndexPath *) indexPath asDefault: (BOOL) isDefault
{
    if(self.defaultPrinterIndexPath != nil)
    {
        //don't do anything if the previous default cell is still the default cell 
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

#pragma mark - PrinterInfoViewControllerMethods
-(void) updateDefaultPrinter:(BOOL) isDefaultOn atIndexPath: (NSIndexPath *) indexPath;
{
    if(isDefaultOn == YES)
    {
        [self setDefaultPrinter:indexPath];
    }
    else
    {
        if(indexPath.row == self.defaultPrinterIndexPath.row)
        {
            [self.printerManager deleteDefaultPrinter];
        }
    }
}

-(Printer *) getPrinterAtIndexPath: (NSIndexPath *) indexPath
{
    return [self.printerManager.listSavedPrinters objectAtIndex:indexPath.row];
}
@end
