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
#import "SearchResultCell.h"
#import "NetworkManager.h"
#import "AlertUtils.h"

#define SEARCHRESULTCELL    @"SearchResultCell"

@interface PrinterSearchViewController ()

/**
 A list of the names of the printers searched from the network that
 are already saved to the database ("old" printers).
 These printers will be displayed in the UITableView with a checkmark.
 */
@property (strong, nonatomic) NSMutableArray* listOldPrinterNames;

/**
 A list of the names of the printers searched from the network that
 are not yet saved to the database ("new" printers).
 These printers will be displayed in the UITableView with a '+' button.
 */
@property (strong, nonatomic) NSMutableArray* listNewPrinterNames;

/**
 A list of the IP addresses of the printers searched from the
 network that are not yet saved to the database.
 This is used for referencing the printer details.
 */
@property (strong, nonatomic) NSMutableArray* listNewPrinterIP;

/**
 A key-value listing of the details for each new printer found during
 the search, using the printer IP address as the key.
 */
@property (strong, nonatomic) NSMutableDictionary* listNewPrinterDetails;

/**
 Implements the pull-to-refresh gesture.
 */
@property (strong, nonatomic) UIRefreshControl* refreshControl;

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

/**
 Called when the user taps on the '+' button of a new printer.
 This method attempts to add the printer to the list of saved
 printers.
 @param row 
        UITableView row in the new printers section
 */
- (void)addPrinter:(UIButton*)plusButton withEvent:(UIEvent*)tapEvent;

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

- (void)viewDidAppear:(BOOL)animated
{
    
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
    self.listOldPrinterNames = [NSMutableArray array];
    self.listNewPrinterNames = [NSMutableArray array];
    self.listNewPrinterIP = [NSMutableArray array];
    self.listNewPrinterDetails = [NSMutableDictionary dictionary];
    self.hasAddedPrinters = NO;
    
    // setup pull-to-refresh
    self.refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self
                            action:@selector(refresh)
                  forControlEvents:UIControlEventValueChanged];
    [self.refreshControl setBackgroundColor:[UIColor blackColor]];
    [self.refreshControl setTintColor:[UIColor whiteColor]];
    [self.tableView addSubview:self.refreshControl];
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
    // check for network connection
    if (![NetworkManager isConnectedToNetwork])
    {
        [AlertUtils displayResult:ERR_NO_NETWORK withTitle:ALERT_PRINTER_SEARCH withDetails:nil];
        return;
    }

    // clear the lists
    [self.listOldPrinterNames removeAllObjects];
    [self.listNewPrinterNames removeAllObjects];
    [self.listNewPrinterIP removeAllObjects];
    [self.listNewPrinterDetails removeAllObjects];

    // start the search
    NSLog(@"initiated search");
    [self.printerManager searchForAllPrinters];
    NSLog(@"returned to screen controller");
    // callbacks for the search will be handled in delegate methods
    
    // if UI needs to do other things, do it here
    
    // show the searching indicator
    // note: content offset code is for fixing the bug in iOS7 where the view does not appear on load
    if (self.tableView.contentOffset.y == 0)
        self.tableView.contentOffset = CGPointMake(0, -self.refreshControl.frame.size.height);
    [self.refreshControl beginRefreshing];
    [self.refreshControl setHidden:NO];
}

#pragma mark - Add

- (void)addPrinter:(UIButton*)plusButton withEvent:(UIEvent*)tapEvent
{
    // get the row tapped
    CGPoint pt = [[[tapEvent touchesForView:plusButton] anyObject] locationInView:self.tableView];
    NSIndexPath* indexPath = [self.tableView indexPathForRowAtPoint:pt];
    
    // check if adding printers is allowed
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertUtils displayResult:ERR_MAX_PRINTERS withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
    else
    {
        // add the printer
        NSString* printerIP = [self.listNewPrinterIP objectAtIndex:indexPath.row];
        PrinterDetails* printerDetails = [self.listNewPrinterDetails valueForKey:printerIP];
        if ([self.printerManager registerPrinter:printerDetails])
        {
            [AlertUtils displayResult:INFO_PRINTER_ADDED withTitle:ALERT_ADD_PRINTER withDetails:nil];
            self.hasAddedPrinters = YES;
            
            // change the '+' button to a checkmark
            [self.listOldPrinterNames addObject:printerDetails.name];
            [self.listNewPrinterNames removeObjectAtIndex:indexPath.row];
            [self.listNewPrinterDetails removeObjectForKey:printerIP];
            [self.listNewPrinterIP removeObjectAtIndex:indexPath.row];
            [self.tableView reloadData];
        }
        else
        {
            [AlertUtils displayResult:ERR_CANNOT_ADD withTitle:ALERT_ADD_PRINTER withDetails:nil];
        }
    }
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    // hide the searching indicator
    [self.refreshControl endRefreshing];
    [self.refreshControl setHidden:YES];
}

- (void)updateForNewPrinter:(PrinterDetails*)printerDetails
{
    NSLog(@"update UI for NEW printer with IP=%@", printerDetails.ip);
    
    // save the printer name and IP
    [self.listNewPrinterNames addObject:printerDetails.name];
    [self.listNewPrinterIP addObject:printerDetails.ip];
    
    // save the entire printer details
    [self.listNewPrinterDetails setValue:printerDetails
                                  forKey:printerDetails.ip];
    
    // reload the tableView
    [self.tableView reloadData];
}

- (void)updateForOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
    NSLog(@"update UI for OLD printer with IP=%@", printerIP);
    
    // save the printer name
    [self.listOldPrinterNames addObject:printerName];
    
    // reload the tableView
    [self.tableView reloadData];
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 2; // old and new printers
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0)
        return [self.listOldPrinterNames count];
    else
        return [self.listNewPrinterNames count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SearchResultCell* cell = [tableView dequeueReusableCellWithIdentifier:SEARCHRESULTCELL
                                                            forIndexPath:indexPath];
   
    // set the cell text
    if (indexPath.section == 0)
    {
        //this is an old printer
        [cell setContents:[self.listOldPrinterNames objectAtIndex:indexPath.row]];
        [cell putCheckmark];
    }
    else
    {
        //this is a new printer
        [cell setContents:[self.listNewPrinterNames objectAtIndex:indexPath.row]];
        [cell putPlusButton:self tapHandler:@selector(addPrinter:withEvent:)];
    }
    
    return cell;
}

@end
