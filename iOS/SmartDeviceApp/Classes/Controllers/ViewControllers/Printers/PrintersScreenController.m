//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersScreenController.h"
#import "AddPrinterScreenController.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "PrinterCell.h"
#import "DatabaseManager.h"

#define SEGUE_TO_ADD_PRINTER    @"PrintersToAddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersToPrinterSearch"
#define UNWIND_FROM_ADD_PRINTER @"AddPrinterToPrinters"

#define PRINTERCELL             @"PrinterCell"



BOOL isDummyDataEnabled = NO; //TODO REMOVE! For Debugging Purposes Only

@interface PrintersScreenController ()

@property (strong, nonatomic) DefaultPrinter* defaultPrinter; //default printer object
@property (strong, nonatomic) NSIndexPath* defaultPrinterIndexPath; //index path of default printer in printers list

/**
 Sets-up the Navigation Bar controls and contents.
 **/
- (void)setupNavBarItems;

/**
 Segues to the Add Printer screen.
 **/
- (void)onAdd:(id)sender;

/**
 Segues to the Printer Search screen.
 **/
- (void)onSearch:(id)sender;

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

/**
 FOR DEBUGGING ONLY.
 This method fills the list of saved printers with static test data.
 **/
- (void)initWithTestData;

@end

@implementation PrintersScreenController

#pragma mark - Lifecycle

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self)
    {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    [self setupNavBarItems];
    [self getSavedPrintersFromDB];
    [self getDefaultPrinterFromDB];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Navigation Bar

- (void)setupNavBarItems
{
    // "Manual Add" Button
    UIBarButtonItem* addButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                                               target:self
                                                                               action:@selector(onAdd:)];
    
    // "Printer Search" Button
    UIBarButtonItem* searchButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSearch
                                                                                  target:self
                                                                                  action:@selector(onSearch:)];
    
    self.navigationItem.rightBarButtonItems = @[searchButton, addButton];
}

- (void)onAdd:(id)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_ADD_PRINTER sender:sender];
}

- (void)onSearch:(id)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_PRINTER_SEARCH sender:sender];
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
                if([DatabaseManager deleteFromDB:self.defaultPrinter] == NO)
                {
                    //TODO Show delete error
                    //If deleting default printer reference failed, do not proceed with deletion of printer
                    return;
                }
                self.defaultPrinter = nil;
                self.defaultPrinterIndexPath = nil;
            }
            
            BOOL deleteSuccess = [DatabaseManager deleteFromDB:printerToDelete];
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
    
    NSString* pathToPList = [[NSBundle mainBundle] pathForResource:@"SmartDeviceApp-Settings" ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPList];
    BOOL isLoadWithInitialData = [[dict objectForKey:@"LoadWithTestData"] boolValue];
    isDummyDataEnabled = isLoadWithInitialData;
    
    if (isLoadWithInitialData)
    {
        [self initWithTestData];
    }
    else
    {
        self.listSavedPrinters = [PrinterManager getPrinters];
        //TODO: Check getPrinters status
    }
}


- (void)initWithTestData
{
    Printer* printer1 = [PrinterManager createPrinter];
    printer1.ip_address = @"111.111.111";
    printer1.name= @"Printer 1";
    
    Printer* printer2 = [PrinterManager createPrinter];
    printer2.ip_address = @"222.222.222";
    printer2.name = @"Printer 2";
    
    Printer* printer3 = [PrinterManager createPrinter];
    printer3.ip_address = @"333.333.333";
    printer3.name = @"Printer 3";
    
    [self.listSavedPrinters addObject:printer1];
    [self.listSavedPrinters addObject:printer2];
    [self.listSavedPrinters addObject:printer3];
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
    if([DatabaseManager saveDB] == NO)
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
        UINavigationController* navController = [segue destinationViewController];
        AddPrinterScreenController* destController = [[navController viewControllers] objectAtIndex:0];
        destController.listSavedPrinters = self.listSavedPrinters;
    }
    
    if ([segue.identifier isEqualToString:SEGUE_TO_PRINTER_SEARCH])
    {
    }
}

- (IBAction)unwindToPrinters:(UIStoryboardSegue*)unwindSegue
{
    UIViewController* sourceViewController = [unwindSegue sourceViewController];
    if ([sourceViewController isKindOfClass:[AddPrinterScreenController class]])
    {
        AddPrinterScreenController* adderScreen = (AddPrinterScreenController*)sourceViewController;
        if ([adderScreen.addedPrinters count] != 0)
        {
            [self.listSavedPrinters addObjectsFromArray:adderScreen.addedPrinters];
            [self.tableView reloadData];
            [DatabaseManager saveDB];
        }
    }
}

@end
