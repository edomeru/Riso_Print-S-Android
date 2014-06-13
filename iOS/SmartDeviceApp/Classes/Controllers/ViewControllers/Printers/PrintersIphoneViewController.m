//
//  PrintersScreenController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintersIphoneViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "AlertHelper.h"
#import "PrintSettingsViewController.h"
#import "CXAlertView.h"
#import "PrinterCell.h"

#define SEGUE_TO_PRINTER_INFO   @"PrintersIphone-PrinterInfo"
#define SEGUE_TO_PRINTSETTINGS  @"PrintersIphone-PrintSettings"
#define SEGUE_TO_ADD_PRINTER    @"PrintersIphone-AddPrinter"
#define SEGUE_TO_PRINTER_SEARCH @"PrintersIphone-PrinterSearch"
#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()

#pragma mark - Data Properties

/** NSIndexPath of the tapped printer cell **/
@property (strong, nonatomic) NSIndexPath *selectedPrinterIndexPath;

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) NSMutableArray *statusHelpers;

#pragma mark - Instance Methods

/**
 Action when the PrinterCell is tapped to segue to the PrinterInfo screen
 */
- (IBAction)tapTableViewAction:(id)sender;

@end

@implementation PrintersIphoneViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc]
                                   initWithTarget:self action:@selector(tapTableViewAction:)];
    [self.tableView addGestureRecognizer:tap];
    
    UISwipeGestureRecognizer* swipeLeft = [[UISwipeGestureRecognizer alloc]
                                           initWithTarget:self action:@selector(swipeLeftTableViewAction:)];
    swipeLeft.direction = UISwipeGestureRecognizerDirectionLeft;
    [self.tableView addGestureRecognizer:swipeLeft];
    
    UIPanGestureRecognizer* pan = [[UIPanGestureRecognizer alloc]
                                   initWithTarget:self action:@selector(swipeNotLeftTableViewAction:)];
    pan.minimumNumberOfTouches = 1;
    pan.delegate = self;
    [pan requireGestureRecognizerToFail:tap];
    [pan requireGestureRecognizerToFail:swipeLeft];
    [self.tableView addGestureRecognizer:pan];
    
    self.statusHelpers = [[NSMutableArray alloc] init];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)dealloc
{
    for(PrinterStatusHelper * statusHelper in self.statusHelpers)
    {
        [statusHelper stopPrinterStatusPolling];
    }
    [self.statusHelpers removeAllObjects];
    
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.printerManager.countSavedPrinters;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString* cellIdentifier = PRINTERCELL;
    PrinterCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier
                                                        forIndexPath:indexPath];
    
    Printer* printer = [self.printerManager getPrinterAtIndex:indexPath.row];
    if ([self.printerManager isDefaultPrinter:printer])
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setCellStyleForDefaultCell];
    }
    else
    {
        [cell setCellStyleForNormalCell];
    }
    if(printer.name == nil || [printer.name isEqualToString:@""] == YES)
    {
        cell.printerName.text = NSLocalizedString(IDS_LBL_NO_NAME, @"No name");
    }
    else
    {
        cell.printerName.text = printer.name;
    }
    
    cell.ipAddress.text = printer.ip_address;
    [cell.printerStatus setStatus:[printer.onlineStatus boolValue]];
    
    if([self.statusHelpers count] <= indexPath.row)
    {
        PrinterStatusHelper *printerStatusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
        printerStatusHelper.delegate = self;
        [printerStatusHelper startPrinterStatusPolling];
        [self.statusHelpers addObject:printerStatusHelper];
    }
    
    UILongPressGestureRecognizer *press = [[UILongPressGestureRecognizer alloc]
                                           initWithTarget:self action:@selector(pressTableViewAction:)];
    press.minimumPressDuration = 0.1f;
    press.delegate = self;
    [cell.contentView addGestureRecognizer:press];
    
    if (indexPath.row == self.printerManager.countSavedPrinters-1)
    {
        [cell.separator setHidden:YES];
    }
    else
    {
        [cell.separator setHidden:NO];
    }
    
    [cell setDeleteButtonLayout];
    
    return cell;
}

#pragma mark - PrinterStatusDelegate

- (void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange:(BOOL)isOnline
{
    NSUInteger index = [self.statusHelpers indexOfObject:statusHelper];
    Printer *printer = [self.printerManager getPrinterAtIndex:index];
    
    printer.onlineStatus = [NSNumber numberWithBool:isOnline];
    PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0]];
    if(cell != nil) //cell returned will be nil if cell for row is not visible
    {
        [cell.printerStatus setStatus:isOnline];
    }
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView*)scrollView
{
    //if a cell is in delete state, remove delete state
    if(self.toDeleteIndexPath != nil)
    {
        [self removeDeleteState];
    }
}

#pragma mark - UIGestureRecognizerDelegate

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    if([gestureRecognizer isKindOfClass:[UILongPressGestureRecognizer class]])
    {
        NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[gestureRecognizer locationInView:self.tableView]];
        
        if(self.toDeleteIndexPath != nil && indexPath != nil)
        {
            if(indexPath.row != self.toDeleteIndexPath.row)
            {
                 [self removeDeleteState];
            }
            return NO;
        }
        // don't accept if there is already a view controller infront 
        if(self.childViewControllers.count > 0)
        {
            return NO;
        }
    }
    else if ([gestureRecognizer isKindOfClass:[UIPanGestureRecognizer class]])
    {
        if (self.toDeleteIndexPath == nil)
        {
            // block the panning gesture when there is no delete button
            // let the view scroll instead
            return NO;
        }
        else
        {
            // cancel the delete button for the first swipe gesture
            return YES;
        }
    }
    
    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    if ([gestureRecognizer isKindOfClass:[UIPanGestureRecognizer class]])
    {
        // allow both the UIScrollView's PanGesture and our PanGesture to react at the same time
        // (allows simultaneous hiding of the delete button and scrolling)
        return YES;
    }
    else
    {
        return NO;
    }
}

#pragma mark - IBActions

- (IBAction)tapDeleteButtonAction:(DeleteButton*)button
{
    DeleteButton *deleteButton = (DeleteButton*)button;
    [deleteButton keepHighlighted:YES];
    [deleteButton setHighlighted:YES];
    
    __weak PrintersIphoneViewController* weakSelf = self;
    
    void (^cancelled)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];
        [weakSelf removeDeleteState];
        [deleteButton keepHighlighted:NO];
        [deleteButton setHighlighted:NO];
    };
    
    void (^confirmed)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [weakSelf deletePrinter];
        [weakSelf removeDeleteState];
        [alertView dismiss];
        [deleteButton keepHighlighted:NO];
        [deleteButton setHighlighted:NO];
    };
    
    [AlertHelper displayConfirmation:kAlertConfirmationDeletePrinter
                   withCancelHandler:cancelled
                  withConfirmHandler:confirmed];
}

- (void)tapTableViewAction:(id)sender
{
    //if a cell is in delete state, remove delete state
    if(self.toDeleteIndexPath != nil)
    {
        [self removeDeleteState];
    }
}

- (void)pressTableViewAction:(id)sender
{
    UILongPressGestureRecognizer *press = (UILongPressGestureRecognizer *) sender;
    //else segue to printer info screen
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    
    if(indexPath == nil)
    {
        PrinterCell  *selectedCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        [selectedCell setHighlighted:NO];
        self.selectedPrinterIndexPath = nil;
        return;
    }
    
    UIGestureRecognizerState state = press.state;
    if(press.state == UIGestureRecognizerStateChanged && self.selectedPrinterIndexPath.row == indexPath.row)
    {
        return;
    }
    
    PrinterCell  *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:indexPath];
    if(state == UIGestureRecognizerStateBegan)
    {
        //else segue to printer info screen
        [cell setHighlighted:YES];
        self.selectedPrinterIndexPath = indexPath;
    }
    else if(state == UIGestureRecognizerStateEnded)
    {
        if(self.selectedPrinterIndexPath != nil)
        {
            [self performSegueTo:[PrinterInfoViewController class]];
            [cell performSelector:@selector(setHighlighted:) withObject:NO afterDelay:0.1f];//make the highlight linger
        }
    }
    else
    {
        //touch went to another row
        PrinterCell  *selectedCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        [selectedCell setHighlighted:NO];
        self.selectedPrinterIndexPath = nil;
    }
}

- (void)swipeLeftTableViewAction:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    if(self.toDeleteIndexPath != nil)
    {
        if(selectedIndexPath.row == self.toDeleteIndexPath.row)
        {
            //swiped the same row that already has a delete button
            return;
        }
        else
        
        {
            //remove delete state of other cells if any
            [self removeDeleteState];
        }
    }
    else
    {
        PrinterCell  *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
        [cell setCellToBeDeletedState:YES];
        self.toDeleteIndexPath = selectedIndexPath;
    }
}

- (void)swipeNotLeftTableViewAction:(id)sender
{
    // method will only be called when gestureRecognizerShouldBegin returns YES,
    // which will only happen when there is a delete button to be cancelled
    
    [self removeDeleteState];
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTER_INFO])
    {
        PrinterInfoViewController *destController = [segue destinationViewController];
        destController.indexPath = self.selectedPrinterIndexPath;
        destController.isDefaultPrinter = NO;
        if(self.defaultPrinterIndexPath != nil &&  self.selectedPrinterIndexPath.row == self.defaultPrinterIndexPath.row)
        {
            destController.isDefaultPrinter = YES;
        }
        destController.delegate = self;
    }
    else if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS])
    {
        ((PrintSettingsViewController *)segue.destinationViewController).printerIndex = [NSNumber numberWithInteger:self.selectedPrinterIndexPath.row];
    }
    else if ([segue.identifier isEqualToString:SEGUE_TO_ADD_PRINTER])
    {
        if (self.toDeleteIndexPath != nil)
            [self removeDeleteState];
    }
    else if ([segue.identifier isEqualToString:SEGUE_TO_PRINTER_SEARCH])
    {
        if (self.toDeleteIndexPath != nil)
            [self removeDeleteState];
    }
}

- (void)segueToPrintSettings
{
    //The PrintersiPhoneViewController is the main controller in the root view controller so it is the one that should call a slide segue to the PrintSettingsViewController
    [self performSegueTo:[PrintSettingsViewController class]];
}

- (IBAction)unwindFromPrinterInfo:(UIStoryboardSegue*)unwindSegue
{
    if ([unwindSegue.sourceViewController isKindOfClass:[PrinterInfoViewController class]])
    {
        PrinterInfoViewController* printerInfoScreen = (PrinterInfoViewController*)unwindSegue.sourceViewController;
        [self setPrinterCell:printerInfoScreen.indexPath asDefault: printerInfoScreen.isDefaultPrinter];
        self.selectedPrinterIndexPath = nil;
    }
}

#pragma mark - Reload

- (void)reloadPrinters
{
    [super reloadPrinters];
    [self.tableView reloadData];
}

#pragma mark - Utilities

- (void)setPrinterCell:(NSIndexPath *)indexPath asDefault:(BOOL)isDefault
{
    if(self.defaultPrinterIndexPath != nil)
    {
        //don't do anything if cell is still the default cell
        //or if the the cell is not the default cell and is being set to not default
        if((self.defaultPrinterIndexPath.row == indexPath.row && isDefault == YES)
           || (self.defaultPrinterIndexPath.row != indexPath.row && isDefault == NO))
        {
            return;
        }
        //unselect the previous default cell
        PrinterCell *previousDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.defaultPrinterIndexPath];
        [previousDefaultCell setCellStyleForNormalCell];
        self.defaultPrinterIndexPath = nil;
    }
    
    if(isDefault == NO)
    {
        return;
    }

    //set the formatting of the selected cell to the default printer cell
    self.defaultPrinterIndexPath = indexPath;
    PrinterCell *selectedDefaultCell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:indexPath];
    [selectedDefaultCell setCellStyleForDefaultCell];
}

- (void)deletePrinter
{
    if ([self.printerManager deletePrinterAtIndex:self.toDeleteIndexPath.row])
    {
        NSInteger rowDeleted = self.toDeleteIndexPath.row;
        
        BOOL deletedDefault = (rowDeleted == self.defaultPrinterIndexPath.row);
        BOOL hasNewDefault = [self.printerManager hasDefaultPrinter];
        if (!hasNewDefault)
        {
            self.defaultPrinterIndexPath = nil;
        }
        
        //set the view of the cell to stop polling for printer status
        PrinterStatusHelper *statusHelper = [self.statusHelpers objectAtIndex:rowDeleted];
        [statusHelper stopPrinterStatusPolling];
        [self.statusHelpers removeObjectAtIndex:rowDeleted];
        
        // update the collection
        [self.tableView deleteRowsAtIndexPaths:@[self.toDeleteIndexPath]
                              withRowAnimation:UITableViewRowAnimationFade];
        __weak PrintersIphoneViewController* weakSelf = self;
        [UIView animateWithDuration:0.5 animations:
         ^{
            if (deletedDefault)
            {
                if (hasNewDefault)
                {
                    // assign the first printer as the new default printer
                    weakSelf.defaultPrinterIndexPath = [NSIndexPath indexPathForRow:0 inSection:0];
                    
                    // reload the new default printer
                    [weakSelf.tableView reloadRowsAtIndexPaths:@[weakSelf.defaultPrinterIndexPath]
                                              withRowAnimation:UITableViewRowAnimationFade];
                }
                //else, deleted printer is the last printer
            }
            else
            {
                if (weakSelf.defaultPrinterIndexPath.row != 0
                    && rowDeleted < weakSelf.defaultPrinterIndexPath.row)
                {
                    NSIndexPath* oldIndexPath = weakSelf.defaultPrinterIndexPath;
                    weakSelf.defaultPrinterIndexPath = [NSIndexPath indexPathForRow:oldIndexPath.row-1
                                                                          inSection:0];
                }
            }
        }];
        
        // update the separator for the new last row
        if(rowDeleted == [self.tableView numberOfRowsInSection:0])
        {
            NSIndexPath *lastIndexPath = [NSIndexPath indexPathForRow:rowDeleted-1 inSection:0];
            [self.tableView reloadRowsAtIndexPaths:@[lastIndexPath] withRowAnimation:UITableViewRowAnimationNone];
        }
        
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

- (void)removeDeleteState
{
    if(self.toDeleteIndexPath != nil)
    {
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
        self.toDeleteIndexPath = nil;
    }
}

@end
