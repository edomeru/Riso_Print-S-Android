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
#import "PrinterManager.h"

#define SEGUE_TO_ADD_PRINTER    @"PrintersToAddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersToPrinterSearch"
#define UNWIND_FROM_ADD_PRINTER @"AddPrinterToPrinters"

#define PRINTERCELL             @"PrinterCell"

@interface PrintersScreenController ()

/**
 Sets-up the Navigation Bar controls and contents.
 **/
- (void)setupNavBarItems;

/**
 Handler for the "Manual Add" button.
 **/
- (void)onAdd:(id)sender;

/**
 Handler for the "Printer Search" button.
 **/
- (void)onSearch:(id)sender;

/**
 Retrieves the Printer objects from DB.
 **/
- (void)getSavedPrintersFromDB;

/**
 FOR DEBUGGING ONLY.
 This method fills the list of saved printers with initial data.
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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Setup Navigation Bar

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

#pragma mark - Navigation Bar Actions

- (void)onAdd:(id)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_ADD_PRINTER sender:sender];
}

- (void)onSearch:(id)sender
{
    [self performSegueWithIdentifier:SEGUE_TO_PRINTER_SEARCH sender:sender];
}

#pragma mark - Setup TableView

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
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier
                                                            forIndexPath:indexPath];
    
    Printer* printer = [self.listSavedPrinters objectAtIndex:indexPath.row];
    cell.textLabel.text = printer.name;
    cell.detailTextLabel.text = printer.ip_address;
    
    return cell;
}

#pragma mark - TableView Actions

#pragma mark - Setup Printers

- (void)getSavedPrintersFromDB
{
    self.listSavedPrinters = [NSMutableArray array];
    
    NSString* pathToPList = [[NSBundle mainBundle] pathForResource:@"SmartDeviceApp-Settings" ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPList];
    BOOL isLoadWithInitialData = [[dict objectForKey:@"LoadWithInitialData"] boolValue];
    if (isLoadWithInitialData)
    {
        [self initWithTestData];
    }
    else
    {
        //TODO: get from DB
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

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:SEGUE_TO_ADD_PRINTER])
    {
        UINavigationController* navController = [segue destinationViewController];
        AddPrinterScreenController* destController = [[navController viewControllers] objectAtIndex:0];
        destController.listSavedPrinters = self.listSavedPrinters;
        destController.addedPrinter = nil;
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
        [self.listSavedPrinters addObject:adderScreen.addedPrinter];
        [self.tableView reloadData];
    }
}

@end
