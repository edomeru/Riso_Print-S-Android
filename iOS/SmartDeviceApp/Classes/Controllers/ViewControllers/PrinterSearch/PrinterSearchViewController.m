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
#import "SearchingIndicator.h"

#define SEGUE_IPHONE_TO_SEARCH_TABLE    @"PrinterSearchIphone-PrinterSearchTable"
#define SEGUE_IPAD_TO_SEARCH_TABLE      @"PrinterSearchIpad-PrinterSearchTable"
#define SEARCHRESULTCELL                @"SearchResultCell"

#define SORT_SEARCH_RESULTS 0
#if SORT_SEARCH_RESULTS
#define OLD_PRINTERS    0
#define NEW_PRINTERS    1
#endif

@interface PrinterSearchViewController ()

#pragma mark - Data Properties

/**
 * Reference to the PrinterManager singleton.
 */
@property (strong, nonatomic) PrinterManager* printerManager;

/**
 * Flag that will be set to YES when a printer is successfully added.
 */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

#if SORT_SEARCH_RESULTS
/**
 * A list of the names of the printers searched from the network that
 * are already saved in the database ("old" printers).
 * These printers will be displayed in the list with a checkmark.
 */
@property (strong, nonatomic) NSMutableArray* listOldPrinterNames;

/**
 * A list of the names of the printers searched from the network that
 * are not yet saved in the database ("new" printers).
 * These printers will be displayed in the list with a '+' button.
 */
@property (strong, nonatomic) NSMutableArray* listNewPrinterNames;

/**
 * A list of the IP addresses of the printers searched from the
 * network that are not yet saved in the database ("new" printers).
 * This is used as keys for the {@link listNewPrinterDetails}.
 */
@property (strong, nonatomic) NSMutableArray* listNewPrinterIP;

/**
 * A key-value listing of the details of each "new" printer found during
 * the search, using the printer IP address as the key.
 * "New" here means the printer is not yet saved in the database.
 */
@property (strong, nonatomic) NSMutableDictionary* listNewPrinterDetails;
#else
/**
 * A list of the IP addresses of the printers searched from the network.
 * This is used as keys for the {@link listPrinterDetails}.
 */
@property (strong, nonatomic) NSMutableArray* listPrinterIP;

/**
 * A key-value listing of the details of each printer found during
 * the search, using the printer IP address as the key.
 * The value can either be just the printer name (for "old" printers)
 * or an actual PrinterDetails object (for "new" printers).\n
 * Note that "new" and "old" here refers to whether the printer is already
 * saved in the database.
 */
@property (strong, nonatomic) NSMutableDictionary* listPrinterDetails;
#endif

#pragma mark - UI Properties

/** 
 * Reference to the animated searching indicator.
 * This is displayed while the printer search is ongoing.
 */
@property (weak, nonatomic) SearchingIndicator* refreshControl;

/**
 * Reference to the search results list.
 */
@property (weak, nonatomic) UITableView* searchResultsTable;

/** 
 * Flag that indicates whether a search is currently ongoing.
 */
@property (assign, nonatomic) BOOL isSearching;

/**
 * Flag that will be set to YES when the device is a tablet.
 */
@property (assign, nonatomic) BOOL isIpad;

/**
 * Reference to the "No Printers Found" label that is displayed if there are no printers. 
 */
@property (weak, nonatomic) IBOutlet UILabel *emptyLabel;

#pragma mark - Internal Methods

/**
 * Sets-up this controller's properties and views.
 */
- (void)setupScreen;

/**
 * Starts the printer search and displays the animated searching indicator.
 * If the device is not connected to a network, then the search is not started,
 * the searching indicator is hidden, and an error message is displayed instead.\n\n
 * The results of the search are handled in the PrinterSearchDelegate methods.
 */
- (void)refreshScreen;

/**
 * Closes the "Printer Search" screen.
 */
- (void)dismissScreen;

/**
 * Displays the animated searching indicator and disables the pull-to-refresh gesture.
 */
- (void)startSearchingAnimation;

/**
 * Hides the animated searching indicator and re-enables the pull-to-refresh gesture.
 */
- (void)stopSearchingAnimation;

/**
 * Responds to pressing the add (+) button.
 * The details of the printer is retrieved from {@link listPrinterDetails}
 * and adds the printer to the database (using PrinterManager).\n
 *
 * @param row the index of the printer on the list
 */
- (void)addPrinter:(NSUInteger)row;

/**
 * Sets the properties of the SlidingViewController.
 */
- (void)initialize;

#pragma mark - IBAction Methods

/**
 * Responds to pressing the back (<) button in the header (for phones only).
 * Calls the {@link dismissScreen} method.
 *
 * @param sender the button object
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
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setupScreen];
    [self refreshScreen];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue*)segue sender:(id)sender
{
    NSString* segueId = [segue identifier];
    if ([segueId isEqualToString:SEGUE_IPHONE_TO_SEARCH_TABLE]
        || [segueId isEqualToString:SEGUE_IPAD_TO_SEARCH_TABLE])
    {
        //embed
        
        UITableViewController* destController = (UITableViewController*)segue.destinationViewController;
        self.refreshControl = (SearchingIndicator*)destController.refreshControl;
        self.searchResultsTable = destController.tableView;
        self.searchResultsTable.delegate = self;
        self.searchResultsTable.dataSource = self;
    }
    else
    {
        //back/slide
        
        if (self.isSearching)
        {
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
            NSLog(@"[INFO][PrinterSearch] canceling search");
#endif
            self.isSearching = NO;
            [self.printerManager stopSearching];
            
            return;
        }
    }
}

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self dismissScreen];
}

#pragma mark - Screen Actions

- (void)setupScreen
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
    self.isSearching = NO;
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        self.isIpad = YES;
    else
        self.isIpad = NO;
    
    [self.refreshControl addTarget:self
                            action:@selector(refreshScreen)
                  forControlEvents:UIControlEventValueChanged];
    
    // fix for initial tint color of the searching indicator in iOS7
    [self.searchResultsTable setContentOffset:CGPointMake(0, -self.refreshControl.frame.size.height)];
}

- (void)refreshScreen
{
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
    [self.searchResultsTable reloadData];
    
    [self startSearchingAnimation];
    self.emptyLabel.hidden = YES;
    
    // check for network connection
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [AlertHelper displayResult:kAlertResultErrNoNetwork
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil];
        
        if ([self.refreshControl isRefreshing])
        {
            [self stopSearchingAnimation];
        }
        
        return;
    }

    // start the search
#if DEBUG_LOG_PRINTER_SEARCH_SCREEN
    NSLog(@"[INFO][PrinterSearch] initiating search");
#endif
    [self.printerManager searchForAllPrinters];
    self.isSearching = YES;
}

- (void)dismissScreen
{
    if (self.isIpad)
        [self close];
    else
        [self unwindFromOverTo:[self.parentViewController class]];
}

#pragma mark - Add

- (void)addPrinter:(NSUInteger)row
{
#if SORT_SEARCH_RESULTS
    NSString* printerIP = [self.listNewPrinterIP objectAtIndex:row];
    PrinterDetails* printerDetails = [self.listNewPrinterDetails valueForKey:printerIP];
#else
    NSString* printerIP = [self.listPrinterIP objectAtIndex:row];
    PrinterDetails* printerDetails = [self.listPrinterDetails valueForKey:printerIP];
#endif
    if ([self.printerManager registerPrinter:printerDetails])
    {
        self.hasAddedPrinters = YES;
        [AlertHelper displayResult:kAlertResultInfoPrinterAdded
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self dismissScreen];
                }];
        
        // change the '+' button to a checkmark
#if SORT_SEARCH_RESULTS
        [self.listOldPrinterNames addObject:printerDetails.name];
        [self.listNewPrinterNames removeObjectAtIndex:row];
        [self.listNewPrinterDetails removeObjectForKey:printerIP];
        [self.listNewPrinterIP removeObjectAtIndex:row];
        [self.tableView reloadData];
#else
        if (printerDetails.name == nil)
            [self.listPrinterDetails setValue:@"" forKey:printerIP];
        else
            [self.listPrinterDetails setValue:printerDetails.name forKey:printerIP];
        [self.searchResultsTable reloadData];
#endif
        
        // if this is an iPad, reload the center panel
        if (self.isIpad)
            [self.printersViewController reloadPrinters];
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDB
                         withTitle:kAlertTitlePrintersSearch
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    // cancel the cell highlight
                    NSIndexPath* rowIndexPath = [NSIndexPath indexPathForRow:row inSection:0];
                    [self.searchResultsTable reloadRowsAtIndexPaths:@[rowIndexPath]
                                                   withRowAnimation:UITableViewRowAnimationNone];
                }];
    }
}

#pragma mark - PrinterSearchDelegate

- (void)printerSearchEndedwithResult:(BOOL)printerFound
{
    self.isSearching = NO;
    [self stopSearchingAnimation];
    self.emptyLabel.hidden = ([self.listPrinterIP count] == 0 ? NO : YES);
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
    [self.searchResultsTable reloadData];
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
    if (printerName == nil)
        [self.listPrinterDetails setValue:@"" forKey:printerIP];
    else
        [self.listPrinterDetails setValue:printerName forKey:printerIP];
#endif
    
    // reload the tableView
    [self.searchResultsTable reloadData];
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
    NSString* printerIP = [self.listPrinterIP objectAtIndex:indexPath.row];
    if ([[self.listPrinterDetails valueForKey:printerIP] isKindOfClass:[PrinterDetails class]])
    {
        // tapping a new printer will add the printer
        [self addPrinter:indexPath.row];
    }
    // tapping an old printer does nothing
#endif
}

- (CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath
{
    return 60.0f;
}

#pragma mark - Refresh Control

- (void)startSearchingAnimation
{
    [self.refreshControl beginRefreshing];
    
    // fix for the table view not moving to its proper place after the refresh indicator appears
    [self.searchResultsTable setContentOffset:CGPointMake(0, self.refreshControl.frame.size.height)];
    
    // prevent repeated pull-to-refresh
    [self.searchResultsTable setBounces:NO];
}

- (void)stopSearchingAnimation
{
    [self.refreshControl endRefreshing];
    
    // fix for the table view not moving to its proper place after the refresh indicator disappears
    [self.searchResultsTable setContentOffset:CGPointMake(0, 0) animated:YES];
    
    // allow pull-to-refresh
    [self.searchResultsTable setBounces:YES];
}

@end
