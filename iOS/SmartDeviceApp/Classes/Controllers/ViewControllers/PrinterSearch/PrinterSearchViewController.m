//
//  PrinterSearchViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterSearchViewController.h"
#import "PrinterDetails.h"
#import "SearchResultCell.h"
#import "NetworkManager.h"
#import "AlertHelper.h"
#import "UIColor+Theme.h"

#define SEARCHRESULTCELL    @"SearchResultCell"
#define SORT_SEARCH_RESULTS 0

#if SORT_SEARCH_RESULTS
#define OLD_PRINTERS    0
#define NEW_PRINTERS    1
#endif

@interface PrinterSearchViewController ()

#pragma mark - Data Properties

/** Handler for the Printer data. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

#if SORT_SEARCH_RESULTS
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
#else
/**
 A list of the IP addresses of the printers searched from the network.
 This is used for referencing the printer details.
 */
@property (strong, nonatomic) NSMutableArray* listPrinterIP;

/**
 A key-value listing of the details for each printer found during
 the search, using the printer IP address as the key. The value can
 either be just the printer name (for old printers) or an actual
 PrinterDetails object (for new printers).
 */
@property (strong, nonatomic) NSMutableDictionary* listPrinterDetails;
#endif

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
#if SORT_SEARCH_RESULTS
    self.listOldPrinterNames = [NSMutableArray array];
    self.listNewPrinterNames = [NSMutableArray array];
    self.listNewPrinterIP = [NSMutableArray array];
    self.listNewPrinterDetails = [NSMutableDictionary dictionary];
#else
    self.listPrinterIP = [NSMutableArray array];
    self.listPrinterDetails = [NSMutableDictionary dictionary];
#endif

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
        [AlertHelper displayResult:kAlertResultErrMaxPrinters
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil];
        return;
    }
    
    // add the printer
#if SORT_SEARCH_RESULTS
    NSString* printerIP = [self.listNewPrinterIP objectAtIndex:row];
    PrinterDetails* printerDetails = [self.listNewPrinterDetails valueForKey:printerIP];
#else
    NSString* printerIP = [self.listPrinterIP objectAtIndex:row];
    PrinterDetails* printerDetails = [self.listPrinterDetails valueForKey:printerIP];
#endif
    if ([self.printerManager registerPrinter:printerDetails])
    {
        [AlertHelper displayResult:kAlertResultInfoPrinterAdded
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        self.hasAddedPrinters = YES;
        
        // change the '+' button to a checkmark
#if SORT_SEARCH_RESULTS
        [self.listOldPrinterNames addObject:printerDetails.name];
        [self.listNewPrinterNames removeObjectAtIndex:row];
        [self.listNewPrinterDetails removeObjectForKey:printerIP];
        [self.listNewPrinterIP removeObjectAtIndex:row];
        [self.tableView reloadData];
#else
        [self.listPrinterDetails setValue:printerDetails.name forKey:printerIP];
        [self.tableView reloadData];
#endif
        
        // if this is an iPad, reload the center panel
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        {
            [self.printersViewController reloadData];
        }
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrPrinterCannotBeAdded
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil];
    }
}

#pragma mark - Refresh

- (void)refresh
{
    // check for network connection
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [AlertHelper displayResult:kAlertResultErrNoNetwork
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil];
        
        if ([self.refreshControl isRefreshing])
            [self.refreshControl endRefreshing];
        [self.refreshControl setHidden:YES];
        
        return;
    }

    // clear the lists
#if SORT_SEARCH_RESULTS
    [self.listOldPrinterNames removeAllObjects];
    [self.listNewPrinterNames removeAllObjects];
    [self.listNewPrinterIP removeAllObjects];
    [self.listNewPrinterDetails removeAllObjects];
#else
    [self.listPrinterIP removeAllObjects];
    [self.listPrinterDetails removeAllObjects];
#endif

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
    
#if SORT_SEARCH_RESULTS
    // save the printer name and IP
    [self.listNewPrinterNames addObject:printerDetails.name];
    [self.listNewPrinterIP addObject:printerDetails.ip];
    
    // save the entire printer details
    [self.listNewPrinterDetails setValue:printerDetails
                                  forKey:printerDetails.ip];
#else
    // save the printer IP
    [self.listPrinterIP addObject:printerDetails.ip];
    
    // save the entire printer details
    [self.listPrinterDetails setValue:printerDetails
                               forKey:printerDetails.ip];
#endif
    
    // reload the tableView
    [self.tableView reloadData];
}

- (void)printerSearchDidFoundOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
    NSLog(@"[INFO][PrinterSearch] received OLD printer with IP=%@", printerIP);
    NSLog(@"[INFO][PrinterSearch] updating UI");
#endif

#if SORT_SEARCH_RESULTS
    // save the printer name
    [self.listOldPrinterNames addObject:printerName];
#else
    // save the printer name
    [self.listPrinterIP addObject:printerIP];
    [self.listPrinterDetails setValue:printerName forKey:printerIP];
#endif
    
    // reload the tableView
    [self.tableView reloadData];
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
#if SORT_SEARCH_RESULTS
    return 2; // old and new printers
#else
    return 1; // list results as is
#endif
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
#if SORT_SEARCH_RESULTS
    if (section == OLD_PRINTERS)
        return [self.listOldPrinterNames count];
    else
        return [self.listNewPrinterNames count];
#else
    return [self.listPrinterIP count];
#endif
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    SearchResultCell* cell = [tableView dequeueReusableCellWithIdentifier:SEARCHRESULTCELL
                                                            forIndexPath:indexPath];
    BOOL isLastCell = NO;
    
#if SORT_SEARCH_RESULTS
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
#else
    // check if this is the last cell
    if (indexPath.row == [self.listPrinterIP count]-1)
        isLastCell = YES;

    [cell setStyle:isLastCell];
    
    // set the cell text
    NSString* printerIP = [self.listPrinterIP objectAtIndex:indexPath.row];
    id printerValue = [self.listPrinterDetails valueForKey:printerIP];
    if ([printerValue isKindOfClass:[NSString class]])
    {
        // this is an old printer
        [cell setContentsUsingName:(NSString*)printerValue usingIP:printerIP];
        [cell setCellAsOldResult];
    }
    else
    {
        // this is a new printer
        PrinterDetails* pd = (PrinterDetails*)printerValue;
        [cell setContentsUsingName:pd.name usingIP:pd.ip];
        [cell setCellAsNewResult];
    }
#endif
    
    return cell;
}

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath
{
#if SORT_SEARCH_RESULTS
    //tapping an old printer does nothing
    
    //tapping a new printer will add the printer
    if (indexPath.section == NEW_PRINTERS)
        [self addPrinter:indexPath.row];
#else
    [self addPrinter:indexPath.row];
#endif
}

- (CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath
{
    return 60.0f;
}

@end
