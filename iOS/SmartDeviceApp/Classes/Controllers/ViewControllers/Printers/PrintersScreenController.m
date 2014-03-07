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


#pragma mark - Printers Data

- (void)getSavedPrintersFromDB
{
    self.listSavedPrinters = [NSMutableArray array];
    
    NSString* pathToPList = [[NSBundle mainBundle] pathForResource:@"SmartDeviceApp-Settings" ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPList];
    BOOL isLoadWithInitialData = [[dict objectForKey:@"LoadWithTestData"] boolValue];
    if (isLoadWithInitialData)
    {
        [self initWithTestData];
    }
    else
    {
        //TODO: get from DB
        self.listSavedPrinters = [PrinterManager getPrinters];
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
    
    if(self.defaultPrinter == nil)
    {
        self.defaultPrinter = [PrinterManager createDefaultPrinter:selectedPrinter];
    }
    else
    {
        if([self.defaultPrinter.printer isEqual:selectedPrinter] == NO)
        {
            self.defaultPrinter.printer = selectedPrinter; // replace
        }
        else
        {
            return; //Do nothing;
        }
    }
    
    
    if(self.defaultPrinterIndexPath != nil)
    {
        PrinterCell *previousDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.defaultPrinterIndexPath];
        [previousDefaultCell setAsDefaultPrinterCell:NO];
    }
    
    //set the formatting of the selected cell
    self.defaultPrinterIndexPath = selectedIndexPath;
    PrinterCell *selectedDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
    [selectedDefaultCell setAsDefaultPrinterCell:YES];
    
    //save changes to DB
    [DatabaseManager saveDB];
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
        }
    }
}

@end
