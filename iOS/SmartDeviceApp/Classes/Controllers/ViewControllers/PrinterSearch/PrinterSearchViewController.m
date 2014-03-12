//
//  PrinterSearchScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterSearchViewController.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"

#define SEARCHRESULTCELL    @"SearchResultCell"

@interface PrinterSearchViewController ()

/**
 A list of the names of the printers searched from the network.
 This is initially nil and will remain nil if no printer/s is/are added.
 */
@property (strong, nonatomic) NSMutableArray* listSearchResults;

/**
 A key-value listing of the details for each new printer using the
 printer IP address as the key. This is used when adding a printer
 when the user presses the + button.
 */
@property (strong, nonatomic) NSMutableDictionary* listNewPrinterDetails;

/**
 Called when screen loads.
 Sets-up this controller's properties and views.
 */
- (void)setup;

/**
 Called when screen loads and in reaction to pull-to-refresh.
 Searches for printers on the network and updates the display.
 */
- (void)refresh;

@end

@implementation PrinterSearchViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    self.isFixedSize = YES;
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setup];
    [self refresh];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Setup

- (void)setup
{
    // setup properties
    self.printerManager.delegate = self;
    self.hasAddedPrinters = NO;
}

#pragma mark - Header

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self.printerManager stopSearching];
    [self unwindFromOverTo:[self.parentViewController class]];
}

#pragma mark - Refresh

- (void)refresh
{
    // initialize the list of search results
    self.listSearchResults = [NSMutableArray array];
    self.listNewPrinterDetails = [NSMutableDictionary dictionary];
    
    //TODO: check if already searching
    
    //TODO: check for network connection
    
    // start the search
    NSLog(@"initiated search");
    [self.printerManager searchForAllPrinters];
    NSLog(@"returned to screen controller");
    // callbacks for the search will be handled in delegate methods
    
    // if UI needs to do other things, do it here
    //TODO: show the searching indicator
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    //TODO: hide the searching indicator
}

- (void)updateForNewPrinter:(PrinterDetails*)printerDetails
{
    NSLog(@"update UI for NEW printer with IP=%@", printerDetails.ip);
    
    // save the printer name
    [self.listSearchResults addObject:printerDetails.name];
    
    // save the printer details
    [self.listNewPrinterDetails setValue:printerDetails forKey:printerDetails.ip];
    
    //TODO: sort the list of printer names
    
    // reload the tableView
    [self.tableView reloadData];
}

- (void)updateForOldPrinter:(NSString*)printerIP withExtra:(NSArray*)otherDetails
{
    NSLog(@"update UI for OLD printer with IP=%@", printerIP);
    
    // save the printer name
    [self.listSearchResults addObject:[otherDetails objectAtIndex:0]];
    
    // save empty printer details
    [self.listNewPrinterDetails setValue:nil forKey:printerIP];

    //TODO: sort the list of printer names
    
    // reload the tableView
    [self.tableView reloadData];
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.listSearchResults count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:SEARCHRESULTCELL
                                                            forIndexPath:indexPath];
    
    cell.textLabel.text = (NSString*)[self.listSearchResults objectAtIndex:indexPath.row];
    
    //TODO: add checkmark for old printers
    //TODO: add + button for new printers
    cell.accessoryType = UITableViewCellAccessoryCheckmark;
    //cell.accessoryType = UITableViewCellAccessoryNone;
    
    return cell;
}

@end
