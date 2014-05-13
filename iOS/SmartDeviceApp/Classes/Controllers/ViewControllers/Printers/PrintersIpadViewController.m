//
//  PrintersIpadViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintersIpadViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterStatusView.h"
#import "AlertHelper.h"
#import "PrintSettingsViewController.h"
#import "UIView+Localization.h"
#import "PrinterLayout.h"
#import "CXAlertView.h"
#import "UIColor+Theme.h"
#import "DeleteButton.h"

#define SEGUE_TO_ADD    @"PrintersIpad-AddPrinter"
#define SEGUE_TO_SEARCH @"PrintersIpad-PrinterSearch"
#define SEGUE_TO_PRINTSETTINGS @"PrintersIpad-PrintSettings"

@interface PrintersIpadViewController ()

#pragma mark - Data Properties

#pragma mark - UI Properties

@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;
@property (nonatomic) UIEdgeInsets insetPortrait;
@property (nonatomic) UIEdgeInsets insetLandscape;
@property (nonatomic, strong) NSNumber *selectedPrinterIndex;

#pragma mark - Instance Methods

- (BOOL) setDefaultPrinter: (NSIndexPath *) indexPath;

@end

@implementation PrintersIpadViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
   
    self.collectionView.delegate = self;
    self.collectionView.dataSource = self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -
#pragma mark CollectionViewDataSource
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return self.printerManager.countSavedPrinters;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    PrinterCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell"
                                                                                forIndexPath:indexPath];
    cell.delegate = self;
    cell.indexPath = indexPath;
    
    Printer *printer = [self.printerManager getPrinterAtIndex:[indexPath item]];
    if ([self.printerManager isDefaultPrinter:printer])
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setAsDefaultPrinterCell:YES];
    }
    else
    {
        [cell setAsDefaultPrinterCell:NO];
    }
    
    if(printer.name == nil || [printer.name isEqualToString:@""] == YES)
    {
        cell.nameLabel.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
    }
    else
    {
        cell.nameLabel.text = printer.name;
    }
    
    cell.ipAddressLabel.text = printer.ip_address;
    
    // Port
    [cell.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_LPR, @"LPR") forSegmentAtIndex:0];
    [cell.portSelection setTitle:NSLocalizedString(IDS_LBL_PORT_RAW, @"RAW") forSegmentAtIndex:1];
    [cell.portSelection setSelectedSegmentIndex:[printer.port integerValue]];
    
    cell.defaultSettingsButton.tag = indexPath.row;
    cell.portSelection.tag = indexPath.row;
    cell.deleteButton.tag = indexPath.row;
    
    // fix for the unconnected helper still polling when the
    // cell and the PrinterStatusViews are reused on reload
    // (the status view is not dealloc'd and it still sets
    // the status on its previous cell)
    if ([cell.statusView.statusHelper isPolling])
    {
        [cell.statusView.statusHelper stopPrinterStatusPolling];
        cell.statusView.statusHelper.delegate = nil;
    }
    
    // since cells may be reused, create a new helper for this cell
    cell.statusView.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.statusView.statusHelper.delegate = cell.statusView;

    //[cell.statusView setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.statusView setStatus:NO];
    [cell.statusView.statusHelper startPrinterStatusPolling];
    
    cell.deleteButton.highlightedColor = [UIColor purple2ThemeColor];
    cell.deleteButton.highlightedTextColor = [UIColor whiteThemeColor];
    
    return cell;
}

- (BOOL)collectionView:(UICollectionView *)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

- (BOOL) setDefaultPrinter: (NSIndexPath *) indexPath
{
    //get selected printer from list
    Printer* selectedPrinter = [self.printerManager getPrinterAtIndex:indexPath.row];
    
    //set as default printer
    return [self.printerManager registerDefaultPrinter:selectedPrinter];
}

#pragma mark - PrinterCollectioViewCellDelegate methods
-(void) setDefaultPrinterCell:(BOOL) isDefaultOn forIndexPath:(NSIndexPath *) indexPath;
{
    if(isDefaultOn == YES)
    {
        if(indexPath != self.defaultPrinterIndexPath)
        {
            [self setDefaultPrinter:indexPath];
            if(self.defaultPrinterIndexPath != nil)
            {
                PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.defaultPrinterIndexPath];
            
                [cell setAsDefaultPrinterCell:FALSE];
            }
    
            self.defaultPrinterIndexPath = indexPath;
        }
    }
    else
    {
        if(indexPath == self.defaultPrinterIndexPath)
        {
            [self.printerManager deleteDefaultPrinter];
            self.defaultPrinterIndexPath = nil;
        }
    }
}

#pragma mark - IBActions
- (IBAction)printerDeleteButtonAction:(id)sender
{
    DeleteButton *deleteButton = (DeleteButton*)sender;
    [deleteButton keepHighlighted:YES];
    [deleteButton setHighlighted:YES];

    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:NSLocalizedString(@"IDS_LBL_PRINTERS", @"") message:NSLocalizedString(@"IDS_INFO_MSG_DELETE_JOBS", @"") cancelButtonTitle:nil];
    
    [alertView addButtonWithTitle:NSLocalizedString(@"IDS_LBL_CANCEL", @"")
                             type:CXAlertViewButtonTypeDefault
                          handler:^(CXAlertView *alertView, CXAlertButtonItem *button) {
                              [alertView dismiss];
                              [deleteButton keepHighlighted:NO];
                              [deleteButton setHighlighted:NO];
                          }];
    
    [alertView addButtonWithTitle:NSLocalizedString(@"IDS_LBL_OK", @"")
                             type:CXAlertViewButtonTypeDefault
                          handler:^(CXAlertView *alertView, CXAlertButtonItem *button) {
                              [self deletePrinterAtIndex:deleteButton.tag];
                              [alertView dismiss];
                              [deleteButton keepHighlighted:NO];
                              [deleteButton setHighlighted:NO];
                          }];
    [alertView show];
}

- (void) deletePrinterAtIndex:(NSUInteger)index
{
    if ([self.printerManager deletePrinterAtIndex:index])
    {
        //check if reference to default printer was also deleted
        if (![self.printerManager hasDefaultPrinter])
            self.defaultPrinterIndexPath = nil;
        NSIndexPath *indexPathToDelete = [NSIndexPath indexPathForRow:index inSection:0];
        //set the view of the cell to stop polling for printer status
        PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:indexPathToDelete];
        [cell.statusView.statusHelper stopPrinterStatusPolling];
        cell.statusView.statusHelper.delegate = nil;
        
        cell.indexPath = nil;
        //set view to non default printer cell style
        [cell setAsDefaultPrinterCell:NO];
        
        //remove cell from view
        [self.collectionView deleteItemsAtIndexPaths:@[indexPathToDelete]];
        self.toDeleteIndexPath = nil;
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDefault
                        withTitle:kAlertTitlePrinters
                      withDetails:nil];
    }
}

- (IBAction)defaultSettingsButtonAction:(id)sender
{
    UIButton* button = (UIButton *) sender;
    self.selectedPrinterIndex = [NSNumber numberWithInteger:button.tag];
    [self performSegueTo:[PrintSettingsViewController class]];
}

- (IBAction)portSelectionAction:(id)sender
{
    UISegmentedControl* segmentedControl = (UISegmentedControl *) sender;
    Printer *printer = [self.printerManager getPrinterAtIndex:segmentedControl.tag];
    printer.port = [NSNumber numberWithInteger:segmentedControl.selectedSegmentIndex];
    [self.printerManager savePrinterChanges];
}


#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue*)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:SEGUE_TO_ADD])
    {
        AddPrinterViewController* adderScreen = (AddPrinterViewController*)segue.destinationViewController;
        adderScreen.printersViewController = self;
    }
    else if ([segue.identifier isEqualToString:SEGUE_TO_SEARCH])
    {
        PrinterSearchViewController* searchScreen = (PrinterSearchViewController*)segue.destinationViewController;
        searchScreen.printersViewController = self;
    }
    else if ([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS])
    {
        PrintSettingsViewController* settingsScreen = (PrintSettingsViewController*)segue.destinationViewController;
        settingsScreen.printerIndex = self.selectedPrinterIndex;
    }
}

#pragma mark - Reload

- (void)reloadData
{
    [super reloadData];
    [self.collectionView reloadData];
}

@end
