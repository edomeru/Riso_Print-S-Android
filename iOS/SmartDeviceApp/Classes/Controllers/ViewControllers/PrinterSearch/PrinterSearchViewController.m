//
//  PrinterSearchScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterSearchViewController.h"
#import "PrinterDetails.h"
#import "SearchResultCell.h"
#import "NetworkManager.h"
#import "AlertHelper.h"
#import "UIColor+Theme.h"

#define SEARCHRESULTCELL    @"SearchResultCell"

#define OLD_PRINTERS    0
#define NEW_PRINTERS    1

@interface PrinterSearchViewController ()

#pragma mark - Data Properties

/** Handler for the Printer data. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

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

#pragma mark - UI Properties

/** Implements the pull-to-refresh gesture. */
@property (strong, nonatomic) UIRefreshControl* refreshControl;

/** UITableView for the printer search results */
@property (weak, nonatomic) IBOutlet UITableView *tableView;

#pragma mark - Internal Methods

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
        the selected printer to add
 */
- (void)addPrinter:(NSUInteger)row;

/** 
 Unwinds back to the Printers screen.
 Cancels any ongoing search operation.
 This is for the iPhone only.
 */
- (IBAction)onBack:(UIButton*)sender;

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
    self.printerManager = [PrinterManager sharedPrinterManager];
    self.printerManager.searchDelegate = self;
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
    [self.refreshControl setBackgroundColor:[UIColor gray4ThemeColor]];
    [self.refreshControl setTintColor:[UIColor whiteThemeColor]];
    [self.tableView addSubview:self.refreshControl];
    [self.refreshControl setHidden:YES];
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // if the SNMP is still searching, the search is canceled
    // the printer, if found, will not be added to the list of saved printers
    if ([self.refreshControl isRefreshing])
    {
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
        NSLog(@"[INFO][PrinterSearch] canceling search");
#endif
        [self.printerManager stopSearching];
    }
}

#pragma mark - Header

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (void)addPrinter:(NSUInteger)row
{
    // check if adding printers is allowed
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertHelper displayResult:ERR_MAX_PRINTERS
                        withTitle:ALERT_TITLE_PRINTERS_SEARCH
                      withDetails:nil];
        return;
    }
    
    // add the printer
    NSString* printerIP = [self.listNewPrinterIP objectAtIndex:row];
    PrinterDetails* printerDetails = [self.listNewPrinterDetails valueForKey:printerIP];
    if ([self.printerManager registerPrinter:printerDetails])
    {
        [AlertHelper displayResult:INFO_PRINTER_ADDED
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        self.hasAddedPrinters = YES;
        
        // change the '+' button to a checkmark
        [self.listOldPrinterNames addObject:printerDetails.name];
        [self.listNewPrinterNames removeObjectAtIndex:row];
        [self.listNewPrinterDetails removeObjectForKey:printerIP];
        [self.listNewPrinterIP removeObjectAtIndex:row];
        [self.tableView reloadData];
        
        // if this is an iPad, reload the center panel
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        {
            [self.printersViewController reloadData];
        }
    }
    else
    {
        [AlertHelper displayResult:ERR_CANNOT_ADD
                        withTitle:ALERT_TITLE_PRINTERS_SEARCH
                      withDetails:nil];
    }
}

#pragma mark - Refresh

- (void)refresh
{
    // check for network connection
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [AlertHelper displayResult:ERR_NO_NETWORK
                        withTitle:ALERT_TITLE_PRINTERS_SEARCH
                      withDetails:nil];
        
        if ([self.refreshControl isRefreshing])
            [self.refreshControl endRefreshing];
        [self.refreshControl setHidden:YES];
        
        return;
    }

    // clear the lists
    [self.listOldPrinterNames removeAllObjects];
    [self.listNewPrinterNames removeAllObjects];
    [self.listNewPrinterIP removeAllObjects];
    [self.listNewPrinterDetails removeAllObjects];

    // start the search
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
    NSLog(@"[INFO][PrinterSearch] initiating search");
#endif
    [self.printerManager searchForAllPrinters];
    // callbacks for the search will be handled in delegate methods
    
    // if UI needs to do other things, do it here
    
    // show the searching indicator
    // note: content offset code is for fixing the bug in iOS7 where the view does not appear on load
    if (self.tableView.contentOffset.y == 0)
        self.tableView.contentOffset = CGPointMake(0, -self.refreshControl.frame.size.height);
    [self.refreshControl beginRefreshing];
    [self.refreshControl setHidden:NO];
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    // hide the searching indicator
    [self.refreshControl endRefreshing];
    [self.refreshControl setHidden:YES];
}

- (void)printerSearchDidFoundNewPrinter:(PrinterDetails*)printerDetails
{
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
    NSLog(@"[INFO][PrinterSearch] received NEW printer with IP=%@", printerDetails.ip);
    NSLog(@"[INFO][PrinterSearch] updating UI");
#endif
    
    // save the printer name and IP
    [self.listNewPrinterNames addObject:printerDetails.name];
    [self.listNewPrinterIP addObject:printerDetails.ip];
    
    // save the entire printer details
    [self.listNewPrinterDetails setValue:printerDetails
                                  forKey:printerDetails.ip];
    
    // reload the tableView
    [self.tableView reloadData];
}

- (void)printerSearchDidFoundOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
    NSLog(@"[INFO][PrinterSearch] received OLD printer with IP=%@", printerIP);
    NSLog(@"[INFO][PrinterSearch] updating UI");
#endif
    
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

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == OLD_PRINTERS)
        return [self.listOldPrinterNames count];
    else
        return [self.listNewPrinterNames count];
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    SearchResultCell* cell = [tableView dequeueReusableCellWithIdentifier:SEARCHRESULTCELL
                                                            forIndexPath:indexPath];
    BOOL isLastCell = NO;
    
    // if this is the last new printer cell
    if ((indexPath.section == NEW_PRINTERS) && (indexPath.row == [self.listNewPrinterNames count]-1))
        isLastCell = YES;
    // if there are no new printers, and this is the last old printer cell
    else if ((indexPath.section == OLD_PRINTERS)
             && ([self.listNewPrinterNames count] == 0)
             && (indexPath.row == [self.listOldPrinterNames count]-1))
        isLastCell = YES;

    // set the cell style
    [cell setStyle:isLastCell];
   
    // set the cell text
    if (indexPath.section == OLD_PRINTERS)
    {
        //this is an old printer
        [cell setContents:[self.listOldPrinterNames objectAtIndex:indexPath.row]];
        [cell setCellAsOldResult];
    }
    else
    {
        //this is a new printer
        [cell setContents:[self.listNewPrinterNames objectAtIndex:indexPath.row]];
        [cell setCellAsNewResult];
    }
    
    return cell;
}

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath
{
    //tapping an old printer does nothing
    
    //tapping a new printer will add the printer
    if (indexPath.section == NEW_PRINTERS)
        [self addPrinter:indexPath.row];
}

@end
