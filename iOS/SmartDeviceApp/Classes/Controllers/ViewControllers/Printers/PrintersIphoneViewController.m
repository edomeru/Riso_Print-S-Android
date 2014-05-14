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

#define SEGUE_TO_PRINTER_INFO   @"PrintersIphone-PrinterInfo"
#define SEGUE_TO_PRINTSETTINGS  @"PrintersIphone-PrintSettings"
#define PRINTERCELL             @"PrinterCell"

@interface PrintersIphoneViewController ()

#pragma mark - Data Properties

/** NSIndexPath of the tapped printer cell **/
@property (strong, nonatomic) NSIndexPath *selectedPrinterIndexPath;

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UITableView *tableView;

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
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapTableViewAction:)];
    [self.tableView addGestureRecognizer:tap];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
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
    cell.delegate = self;
    
    Printer* printer = [self.printerManager getPrinterAtIndex:indexPath.row];
    if ([self.printerManager isDefaultPrinter:printer])
    {
        self.defaultPrinterIndexPath = indexPath;
        [cell setCellStyleForDefaultCell];
    }
    if(printer.name == nil || [printer.name isEqualToString:@""] == YES)
    {
        cell.printerName.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
    }
    else
    {
        cell.printerName.text = printer.name;
    }
    
    cell.ipAddress.text = printer.ip_address;
    
    cell.printerStatus.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.printerStatus.statusHelper.delegate = cell.printerStatus;
    
    UILongPressGestureRecognizer *press = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(pressTableViewAction:)];
    press.minimumPressDuration = 0.1f;
    press.delegate = self;
    [cell.contentView addGestureRecognizer:press];
    
    //[cell.printerStatus setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.printerStatus setStatus:NO];
    [cell.printerStatus.statusHelper startPrinterStatusPolling];
    
    if (indexPath.row == self.printerManager.countSavedPrinters-1)
    {
        [cell.separator setHidden:YES];
    }
    else
    {
        [cell.separator setHidden:NO];
    }
    
    return cell;
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView*)scrollView
{
    if (self.toDeleteIndexPath != nil)
        [self removeDeleteState];
}

#pragma mark - IBActions

- (IBAction)tapTableViewAction:(id)sender
{
    //if a cell is in delete state, remove delete state
    if(self.toDeleteIndexPath != nil)
    {
        [self removeDeleteState];
        return;
    }
}

- (IBAction)pressTableViewAction:(id)sender
{
    UILongPressGestureRecognizer *press = (UILongPressGestureRecognizer *) sender;
    //else segue to printer info screen
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[sender locationInView:self.tableView]];
    PrinterCell  *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:indexPath];
    if(press.state == UIGestureRecognizerStateBegan)
    {
        if(self.toDeleteIndexPath != nil)
        {
            [self removeDeleteState];
            return;
        }
        
        if(self.selectedPrinterIndexPath != nil)
        {
            return;
        }
        //else segue to printer info screen
        [cell setHighlighted:YES];
        if(indexPath != nil)
        {
            self.selectedPrinterIndexPath = indexPath;
            [self performSegueTo:[PrinterInfoViewController class]];
        }
    }
    else{
        [cell setHighlighted:NO];
    }
}

-(BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    if([gestureRecognizer isKindOfClass:[UILongPressGestureRecognizer class]])
    {
        NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:[gestureRecognizer locationInView:self.tableView]];
    
        if(self.toDeleteIndexPath != nil && indexPath != nil && indexPath.row == self.toDeleteIndexPath.row)
        {
            return NO;
        }
        
    }
    return YES;
}
- (IBAction)swipePrinterCellAction:(id)sender
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

    PrinterCell  *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:selectedIndexPath];
    [cell setCellToBeDeletedState:YES];
    self.toDeleteIndexPath = selectedIndexPath;
}

- (void)didTapDeleteButton:(DeleteButton*)button
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
        [alertView dismiss];
        [deleteButton keepHighlighted:NO];
        [deleteButton setHighlighted:NO];
    };
    
    [AlertHelper displayConfirmation:kAlertConfirmationDeletePrinter
                   withCancelHandler:cancelled
                  withConfirmHandler:confirmed];
}

- (void) deletePrinter
{
    if ([self.printerManager deletePrinterAtIndex:self.toDeleteIndexPath.row])
    {
        //check if reference to default printer was also deleted
        if (![self.printerManager hasDefaultPrinter])
            self.defaultPrinterIndexPath = nil;
        
        //set the view of the cell to stop polling for printer status
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell.printerStatus.statusHelper stopPrinterStatusPolling];
        
        //set view to non default printer cell style
        [cell setCellStyleForNormalCell];
        
        //remove cell from view
        [self.tableView deleteRowsAtIndexPaths:@[self.toDeleteIndexPath]
                              withRowAnimation:UITableViewRowAnimationAutomatic];
        
        self.toDeleteIndexPath = nil;
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDelete
                         withTitle:kAlertTitlePrinters
                       withDetails:nil];
    }
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
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        destController.delegate = self;
        [cell.printerStatus.statusHelper stopPrinterStatusPolling];
    }
    else if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS])
    {
        ((PrintSettingsViewController *)segue.destinationViewController).printerIndex = [NSNumber numberWithInteger:self.selectedPrinterIndexPath.row];
    }
}

-(void) segueToPrintSettings
{
    //The PrintersiPhoneViewController is the main controller in the root view controller so it is the one that should call a slide segue to the PrintSettingsViewController
    [self performSegueTo:[PrintSettingsViewController class]];
}

- (IBAction)unwindFromPrinterInfo:(UIStoryboardSegue*)unwindSegue
{
    if ([unwindSegue.sourceViewController isKindOfClass:[PrinterInfoViewController class]])
    {
        PrinterInfoViewController* printerInfoScreen = (PrinterInfoViewController*)unwindSegue.sourceViewController;
        PrinterCell *cell = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.selectedPrinterIndexPath];
        [cell.printerStatus.statusHelper startPrinterStatusPolling];
        [self setPrinterCell:printerInfoScreen.indexPath asDefault: printerInfoScreen.isDefaultPrinter];
        self.selectedPrinterIndexPath = nil;
    }
}

#pragma mark - Reload

- (void)reloadData
{
    [super reloadData];
    [self.tableView reloadData];
}

#pragma mark - private helper methods

-(void) setPrinterCell:(NSIndexPath *) indexPath asDefault: (BOOL) isDefault
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

-(void) removeDeleteState
{
    if(self.toDeleteIndexPath != nil)
    {
        PrinterCell *cell   = (PrinterCell *)[self.tableView cellForRowAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
        self.toDeleteIndexPath = nil;
    }
}

#pragma mark - Rotation

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if (self.toDeleteIndexPath != nil)
        [self removeDeleteState];
}

@end
