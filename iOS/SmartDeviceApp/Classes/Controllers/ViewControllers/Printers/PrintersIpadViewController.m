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

/** 
 * Reference to the UICollectionView displaying the list of printers.
 */
@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;

/**
 * Spacing for the UICollectionView items in portrait orientation.
 */
@property (nonatomic) UIEdgeInsets insetPortrait;

/**
 * Spacing for the UICollectionView items in landscape orientation.
 */
@property (nonatomic) UIEdgeInsets insetLandscape;

/**
 * Item index of the printer whose default print settings are to be displayed.
 */
@property (nonatomic, strong) NSNumber *selectedPrinterIndex;

/**
 * List of PrinterStatusHelper objects monitoring the online/offline status of each printer.
 * There is one PrinterStatusHelper for each printer on the list.\n
 * These helpers are allocated during the initialization of the screen
 * and are deallocated when the screen is unloaded.
 */
@property (nonatomic, strong) NSMutableArray *statusHelpers;

#pragma mark - Instance Methods

/** 
 * Updates the database to set the specified printer as the default printer.
 * Uses PrinterManager to update the printer in the database.
 *
 * @param indexPath the printer's index path in the UICollectionView
 * @return YES if successful, NO otherwise
 */
- (BOOL)setDefaultPrinter:(NSIndexPath *)indexPath;

/**
 * Removes the specified printer from the display and from the database.
 * Uses PrinterManager to update the printer in the database.\n
 * If the removed printer is the default printer, the display is also updated to
 * indicate the new default printer (unless this is the last printer).
 * 
 * @param index item index of deleted printer in the UICollectionView
 */
- (void)deletePrinterAtIndex:(NSUInteger)index;

/**
 * Responds to the "Delete" button press.
 * Displays the delete confirmation message.\n
 * If the user confirms the deletion, the {@link deletePrinterAtIndex:} method is called.
 * If the user cancels, then the alert is just dismissed.
 *
 * @param sender the button object
 */
- (IBAction)printerDeleteButtonAction:(id)sender;

/**
 * Responds to the default print settings button press.
 * Displays the "Default Print Settings" screen.
 *
 * @param sender the button object
 */
- (IBAction)defaultSettingsButtonAction:(id)sender;

/**
 * Responds to the default print settings button press.
 * Displays the "Default Print Settings" screen.
 *
 * @param sender the button object
 */
- (IBAction)pressDefaultSettingsRowAction:(id)sender;

/**
 * Responds to the printer port selection action.
 * Updates the display and updates the Printer object using PrinterManager.
 * 
 * @param sender the port selection object
 */
- (IBAction)portSelectionAction:(id)sender;

/**
 * Responds to the "Set as Default Printer" switch action.
 * The display is updated to indicate that the printer is the default printer
 * and calls {@link setDefaultPrinter:}.
 *
 * @param sender the switch object
 */
- (IBAction)defaultPrinterSelectionAction:(id)sender;

/**
 * Shifts the tags of the UICollectionView cells.
 * This is used when a printer is removed from display.\n
 * The tags need to be shifted to still be continuous and consistent with the list.
 */
- (void)refreshControlTagsOfCellsAtIndexPaths:(NSArray *)indexPaths;

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
    self.statusHelpers = [[NSMutableArray alloc] init];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    for(PrinterStatusHelper *statusHelper in self.statusHelpers)
    {
        [statusHelper stopPrinterStatusPolling];
    }
    [self.statusHelpers removeAllObjects];
}

#pragma mark - CollectionViewDataSource

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
    PrinterCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell" forIndexPath:indexPath];
    
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
        cell.nameLabel.text = NSLocalizedString(IDS_LBL_NO_NAME, @"No name");
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
    [cell.portSelection setEnabled:[printer.enabled_raw boolValue] forSegmentAtIndex:1];
    
    if (![printer.enabled_raw boolValue])
    {
        cell.portSelection.userInteractionEnabled = NO;
    }
    else
    {
        cell.portSelection.userInteractionEnabled = YES;
    }
    
    cell.portSelection.tag = indexPath.row;
    cell.deleteButton.tag = indexPath.row;
    cell.deleteButton.delegate = nil;
    cell.defaultPrinterSelection.tag = indexPath.row;
    
    UILongPressGestureRecognizer *press = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(pressDefaultSettingsRowAction:)];
    press.minimumPressDuration = 0.1f;
    [cell.defaultSettingsRow addGestureRecognizer:press];
    [cell setDefaultSettingsRowToSelected:NO];
    
    if([self.statusHelpers count] <= indexPath.row)
    {
        PrinterStatusHelper *printerStatusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
        printerStatusHelper.delegate = self;
        [printerStatusHelper startPrinterStatusPolling];
        [self.statusHelpers addObject:printerStatusHelper];
    }

    [cell.statusView setStatus:[printer.onlineStatus boolValue]]; //initial status
    
    cell.deleteButton.highlightedColor = [UIColor purple2ThemeColor];
    cell.deleteButton.highlightedTextColor = [UIColor whiteThemeColor];
    
    return cell;
}

- (BOOL)collectionView:(UICollectionView *)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

#pragma mark - PrinterStatusHelper delegate

- (void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange:(BOOL)isOnline
{
    NSUInteger index = [self.statusHelpers indexOfObject:statusHelper];
    Printer *printer = [self.printerManager getPrinterAtIndex:index];
    
    printer.onlineStatus = [NSNumber numberWithBool:isOnline];
    PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:index inSection:0]];
    if(cell != nil) //cell returned will be nil if cell for row is not visible
    {
        [cell.statusView setStatus:isOnline];
    }
}

#pragma mark - IBActions

- (IBAction)printerDeleteButtonAction:(id)sender
{
    DeleteButton *deleteButton = (DeleteButton*)sender;
    [deleteButton keepHighlighted:YES];
    [deleteButton setHighlighted:YES];

    __weak PrintersIpadViewController* weakSelf = self;
    
    void (^cancelled)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];
        [deleteButton keepHighlighted:NO];
        [deleteButton setHighlighted:NO];
    };
    
    void (^confirmed)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [weakSelf deletePrinterAtIndex:deleteButton.tag];
        [alertView dismiss];
        [deleteButton keepHighlighted:NO];
        [deleteButton setHighlighted:NO];
    };

    [AlertHelper displayConfirmation:kAlertConfirmationDeletePrinter
                   withCancelHandler:cancelled
                  withConfirmHandler:confirmed];
}

- (IBAction)defaultPrinterSwitchAction:(id)sender
{
    UISegmentedControl *defaultPrinterSelection = (UISegmentedControl *)sender;
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:defaultPrinterSelection.tag inSection:0];
    
    if(defaultPrinterSelection.selectedSegmentIndex == 0) //yes
    {
        if([self setDefaultPrinter:indexPath])
        {
            if(self.defaultPrinterIndexPath != nil)
            {
                PrinterCollectionViewCell *oldDefaultCell =
                (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.defaultPrinterIndexPath];
                [oldDefaultCell setAsDefaultPrinterCell:FALSE];
            }
            
            PrinterCollectionViewCell *newDefaultCell =
            (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:indexPath];
            [newDefaultCell setAsDefaultPrinterCell:YES];
            
            self.defaultPrinterIndexPath = indexPath;
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrinters
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [defaultPrinterSelection setSelectedSegmentIndex:1];
                    }];
        }
    }
    //switch is automatically turned off when a new default printer is selected
}

- (IBAction)pressDefaultSettingsRowAction:(id)sender
{
    UILongPressGestureRecognizer *press = (UILongPressGestureRecognizer *) sender;
    NSIndexPath *indexPath = [self.collectionView indexPathForItemAtPoint: [press locationInView:self.collectionView]];

    if(press.state == UIGestureRecognizerStateBegan)
    {
        PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:indexPath];
        [cell setDefaultSettingsRowToSelected:YES];
        self.selectedPrinterIndex = [NSNumber numberWithInteger:indexPath.row];
    }
    else if(press.state == UIGestureRecognizerStateEnded)
    {
        if(self.selectedPrinterIndex != nil)
        {
            [self performSegueTo:[PrintSettingsViewController class]];
        }
    }
    else
    {
        PrinterCollectionViewCell *selectedCell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:[self.selectedPrinterIndex integerValue] inSection:0]];
        if(indexPath == nil || indexPath.row != [self.selectedPrinterIndex integerValue] ||
           CGRectContainsPoint(selectedCell.defaultSettingsRow.frame, [press locationInView:selectedCell.contentView]) == NO)
        {
            [selectedCell setDefaultSettingsRowToSelected:NO];
            self.selectedPrinterIndex = nil;
        }
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

- (IBAction)defaultPrinterSelectionAction:(id)sender
{
    UISegmentedControl *defaultPrinterSelection = (UISegmentedControl *)sender;
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:defaultPrinterSelection.tag inSection:0];
    
    if(defaultPrinterSelection.selectedSegmentIndex == 0) //yes
    {
        if([self setDefaultPrinter:indexPath])
        {
            if(self.defaultPrinterIndexPath != nil)
            {
                PrinterCollectionViewCell *oldDefaultCell =
                (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.defaultPrinterIndexPath];
                [oldDefaultCell setAsDefaultPrinterCell:FALSE];
            }
            
            PrinterCollectionViewCell *newDefaultCell =
            (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:indexPath];
            [newDefaultCell setAsDefaultPrinterCell:YES];
            
            self.defaultPrinterIndexPath = indexPath;
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrinters
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [defaultPrinterSelection setSelectedSegmentIndex:1];
                    }];
        }
    }
}

#pragma mark - private helper methods

- (BOOL)setDefaultPrinter:(NSIndexPath *)indexPath
{
    //get selected printer from list
    Printer* selectedPrinter = [self.printerManager getPrinterAtIndex:indexPath.row];
    
    //set as default printer
    return [self.printerManager registerDefaultPrinter:selectedPrinter];
}

- (void)deletePrinterAtIndex:(NSUInteger)index
{
    if ([self.printerManager deletePrinterAtIndex:index])
    {
        BOOL deletedDefault = (index == self.defaultPrinterIndexPath.item);
        BOOL hasNewDefault = [self.printerManager hasDefaultPrinter];
        if (!hasNewDefault)
        {
            self.defaultPrinterIndexPath = nil;
        }
        
        //set the view of the cell to stop polling for printer status
        PrinterStatusHelper *statusHelper = [self.statusHelpers objectAtIndex:index];
        [statusHelper stopPrinterStatusPolling];
        [self.statusHelpers removeObjectAtIndex:index];
        
        // update the collection
        __weak PrintersIpadViewController* weakSelf = self;
        [self.collectionView performBatchUpdates:^
        {
            // remove cell from view
            NSIndexPath *indexPathToDelete = [NSIndexPath indexPathForItem:index inSection:0];
            [weakSelf.collectionView deleteItemsAtIndexPaths:@[indexPathToDelete]];
            
        } completion:^(BOOL finished)
        {
            if (deletedDefault)
            {
                if (hasNewDefault)
                {
                    // assign the first printer as the new default printer
                    weakSelf.defaultPrinterIndexPath = [NSIndexPath indexPathForItem:0 inSection:0];
                    
                    // reload the new default printer
                    [weakSelf.collectionView reloadItemsAtIndexPaths:@[weakSelf.defaultPrinterIndexPath]];
                }
                //else, deleted printer is the last printer
            }
            else
            {
                if (weakSelf.defaultPrinterIndexPath.item != 0
                    && index < weakSelf.defaultPrinterIndexPath.item)
                {
                    NSIndexPath* oldIndexPath = weakSelf.defaultPrinterIndexPath;
                    weakSelf.defaultPrinterIndexPath = [NSIndexPath indexPathForRow:oldIndexPath.row-1
                                                                          inSection:0];
                }
            }
        }];
        
        // reload data of items after the deleted item to update the control tags of the next items
        NSMutableArray *indexPathsToReload = [[NSMutableArray alloc] init];
        NSInteger numberOfItems = [self.collectionView numberOfItemsInSection:0];
        for(NSInteger i = index; i < numberOfItems; i++)
        {
            NSIndexPath * indexPath = [NSIndexPath indexPathForItem:i inSection:0];
            [indexPathsToReload addObject:indexPath];
        }
        [self refreshControlTagsOfCellsAtIndexPaths:indexPathsToReload];
        
        self.toDeleteIndexPath = nil;
        
        self.emptyLabel.hidden = (self.printerManager.countSavedPrinters == 0 ? NO : YES);
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDB
                        withTitle:kAlertTitlePrinters
                      withDetails:nil];
    }
}

- (void)refreshControlTagsOfCellsAtIndexPaths:(NSArray *)indexPaths
{
    if(indexPaths == nil)
    {
        return;
    }
    
    for(NSIndexPath *indexPath in indexPaths)
    {
        PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:indexPath];
        cell.deleteButton.tag = indexPath.row;
        cell.portSelection.tag = indexPath.row;
        cell.defaultPrinterSelection.tag = indexPath.row;
    }
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

- (void)reloadPrinters
{
    [super reloadPrinters];
    if(self.selectedPrinterIndex != nil)
    {
        NSIndexPath* selectedIndexPath = [NSIndexPath indexPathForItem:[self.selectedPrinterIndex integerValue]
                                                             inSection:0];
        PrinterCollectionViewCell *selectedCell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:selectedIndexPath];
        [selectedCell setDefaultSettingsRowToSelected:NO];
        self.selectedPrinterIndex = nil;
    }
    else
    {
        [self.collectionView reloadData];
    }
}

@end
