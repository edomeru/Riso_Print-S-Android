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
#import "DatabaseManager.h"

#define SEGUE_TO_MAINMENU       @"Printers-MainMenu"
#define SEGUE_TO_ADD_PRINTER    @"PrintersIphone-AddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersIphone-PrinterSearch"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()

@property (strong, nonatomic) DefaultPrinter* defaultPrinter; //default printer object
@property (strong, nonatomic) NSIndexPath* defaultPrinterIndexPath; //index path of default printer in printers list

/**
 Retrieves the Printer objects from DB.
 **/
- (void)getSavedPrintersFromDB;

/**
 Retrieves the DefaultPrinter object from DB.
 **/
- (void) getDefaultPrinterFromDB;

/**
 Action when the PrinterCell is tapped to select as Default Printer
 **/
- (IBAction)onTapPrinterCell:(id)sender;

/**
 Action when the PrinterCell Disclosure is tapped to segue to the PrinterInfo screen
 **/
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

    [self getSavedPrintersFromDB];
    [self getDefaultPrinterFromDB];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Header

- (IBAction)onAdd:(UIButton *)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_ADD_PRINTER sender:self];
}

- (IBAction)onSearch:(UIButton *)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_PRINTER_SEARCH sender:self];
}

- (IBAction)onMenu:(UIButton *)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_MAINMENU sender:self];
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.listSavedPrinters count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString* cellIdentifier = PRINTERCELL;
    PrinterCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier
                                                        forIndexPath:indexPath];
    
    Printer* printer = [self.listSavedPrinters objectAtIndex:indexPath.row];
    if([self.defaultPrinter.printer isEqual:printer] == YES)
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
            Printer *printerToDelete = [self.listSavedPrinters objectAtIndex:indexPath.row];
            
            NSLog(@"Deleting Printer %@ at row %d", printerToDelete.name, indexPath.row);
  
            //check if printer to be deleted is the default printer
            if(self.defaultPrinterIndexPath != nil && self.defaultPrinterIndexPath.row == indexPath.row)
            {
                
                NSLog(@"Printer to be deleted is default printer. Removing default printer reference");
                if([DatabaseManager deleteObject:self.defaultPrinter] == NO)
                {
                    //TODO Show delete error
                    //If deleting default printer reference failed, do not proceed with deletion of printer
                    return;
                }
                self.defaultPrinter = nil;
                self.defaultPrinterIndexPath = nil;
            }
            
            BOOL deleteSuccess = [DatabaseManager deleteObject:printerToDelete];
            if(deleteSuccess == YES)
            {
                //remove the object from list
                [self.listSavedPrinters removeObjectAtIndex:indexPath.row];
                //set the view of the cell to stop polling for printer status
                PrinterCell *cell = (PrinterCell *)[tableView cellForRowAtIndexPath:indexPath];
                [cell.printerStatus.statusHelper stopPrinterStatusPolling];
                //set view to non default printer cell style
                [cell setAsDefaultPrinterCell:NO];
                //remove cell from view
                [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
            }
            else
            {
                //TODO Show delete error;
                NSLog(@"Delete Printer from DB failed");
            }
            
        }
}
#pragma mark - Printers Data

- (void)getSavedPrintersFromDB
{
    self.listSavedPrinters = [NSMutableArray array];
    self.listSavedPrinters = [PrinterManager getPrinters];
    //TODO: Check getPrinters status
}

- (void)getDefaultPrinterFromDB
{
    self.defaultPrinter = [PrinterManager getDefaultPrinter];
}

#pragma mark - Gesture Recognizer Handler
- (IBAction)onTapPrinterCell:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    //get selected printer from list
    Printer *selectedPrinter = [self.listSavedPrinters objectAtIndex:selectedIndexPath.row];
    
    //if there is no default printer, create a default printer
    if(self.defaultPrinter == nil)
    {
        self.defaultPrinter = [PrinterManager createDefaultPrinter:selectedPrinter];
    }
    else //the default printer is changed
    {
        if([self.defaultPrinter.printer isEqual:selectedPrinter] == NO)
        {
            self.defaultPrinter.printer = selectedPrinter; // replace
        }
        else
        {
            //If selected default printer is still the same printer, do not update UI and database
            return; //Do nothing;
        }
    }
    
    //save changes to DB
    if([DatabaseManager saveChanges] == NO)
    {
        //If changes to default printer is not saved. Do not update UI
        //TODO Show Error
        return;
    }
    
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
        // give the Add Printer screen a copy of the list of saved printers
        AddPrinterViewController* destController = [segue destinationViewController];
        destController.listSavedPrinters = self.listSavedPrinters;
    }
    
    if ([segue.identifier isEqualToString:SEGUE_TO_PRINTER_SEARCH])
    {
        // give the Printer Search screen a copy of the list of saved printers
        PrinterSearchViewController* destController = [segue destinationViewController];
        destController.listSavedPrinters = self.listSavedPrinters;
    }
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue*)unwindSegue
{
    UIViewController* sourceViewController = [unwindSegue sourceViewController];
    if ([sourceViewController isKindOfClass:[AddPrinterViewController class]])
    {
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)sourceViewController;
        if ([adderScreen.addedPrinters count] != 0)
        {
            [self.listSavedPrinters addObjectsFromArray:adderScreen.addedPrinters];
            [self.tableView reloadData];
        }
    }
}

- (IBAction)unwindFromSlidingDrawer:(UIStoryboardSegue *)segue
{
}

@end
