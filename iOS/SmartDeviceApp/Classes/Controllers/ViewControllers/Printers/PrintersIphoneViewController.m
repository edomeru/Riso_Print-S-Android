//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIphoneViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "PrinterCell.h"

#define SEGUE_TO_ADD_PRINTER    @"PrintersIphone-AddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersIphone-PrinterSearch"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()

/** 
 Internal manager for adding printers to the DB, removing printers from the DB, and
 setting the default printer. It also maintains the list of the Printer objects from
 the DB. This is shared amongst all the child screens of the Printer screen.
 */
@property (strong, nonatomic) PrinterManager* printerManager;

/** NSIndexPath of the default printer in the Printers list **/
@property (strong, nonatomic) NSIndexPath* defaultPrinterIndexPath;

/**
 Action when the PrinterCell is tapped to select as Default Printer
 */
- (IBAction)onTapPrinterCell:(id)sender;

/**
 Action when the PrinterCell Disclosure is tapped to segue to the PrinterInfo screen
 */
- (IBAction)onTapPrinterCellDisclosure:(id)sender;

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

    // setup the PrinterManager
    self.printerManager = [[PrinterManager alloc] init];
    [self.printerManager getPrinters];
    [self.printerManager getDefaultPrinter];
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
    
    Printer* printer = [self.printerManager.listSavedPrinters objectAtIndex:indexPath.row];
    if([self.printerManager.defaultPrinter.printer isEqual:printer] == YES)
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setAsDefaultPrinterCell:YES];
    }
    
    cell.printerName.text = printer.name;
    cell.printerStatus.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.printerStatus.statusHelper.delegate = cell.printerStatus;

    [cell.printerStatus setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.printerStatus.statusHelper startPrinterStatusPolling];
    
    return cell;
}

-(UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

-(BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

-(void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
        if(editingStyle == UITableViewCellEditingStyleDelete)
        {
            //Get the printer to be deleted
            Printer *printerToDelete = [self.printerManager.listSavedPrinters objectAtIndex:indexPath.row];
            
            NSLog(@"Deleting Printer %@ at row %d", printerToDelete.name, indexPath.row);
  
            //check if printer to be deleted is the default printer
            if(self.defaultPrinterIndexPath != nil && self.defaultPrinterIndexPath.row == indexPath.row)
            {
                
                NSLog(@"Printer to be deleted is default printer. Removing default printer reference");
                [self.printerManager deleteDefaultPrinter];
                self.defaultPrinterIndexPath = nil;
            }
            [self.printerManager deletePrinter:printerToDelete];

            //set the view of the cell to stop polling for printer status
            PrinterCell *cell = (PrinterCell *)[tableView cellForRowAtIndexPath:indexPath];
            [cell.printerStatus.statusHelper stopPrinterStatusPolling];
            
            //set view to non default printer cell style
            [cell setAsDefaultPrinterCell:NO];
            
            //remove cell from view
            [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
        }
}
#pragma mark - Printers Data

#pragma mark - Gesture Recognizer Handler
- (IBAction)onTapPrinterCell:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    
    //get selected printer from list
    Printer *selectedPrinter = [self.printerManager.listSavedPrinters objectAtIndex:selectedIndexPath.row];
    
    //set as default printer
    [self.printerManager registerDefaultPrinter:selectedPrinter];
    
    if(self.defaultPrinterIndexPath != nil)
    {
        PrinterCell *previousDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.defaultPrinterIndexPath];
        [previousDefaultCell setAsDefaultPrinterCell:NO];
    }
    
    //set the formatting of the selected cell to the default printer cell
    self.defaultPrinterIndexPath = selectedIndexPath;
    PrinterCell *selectedDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
    [selectedDefaultCell setAsDefaultPrinterCell:YES];
    
}

- (IBAction)onTapPrinterCellDisclosure:(id)sender
{
    NSLog(@"PrinterCell Tapped");
    //TODO Add segue to printer info
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
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue*)unwindSegue
{
    UIViewController* sourceViewController = [unwindSegue sourceViewController];
    if ([sourceViewController isKindOfClass:[AddPrinterViewController class]])
    {
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
    if ([sourceViewController isKindOfClass:[PrinterSearchViewController class]])
    {
        PrinterSearchViewController* adderScreen = (PrinterSearchViewController*)sourceViewController;
        if (adderScreen.hasAddedPrinters)
            [self.tableView reloadData];
    }
}

- (IBAction)unwindFromSlidingDrawer:(UIStoryboardSegue *)segue
{
}

@end
