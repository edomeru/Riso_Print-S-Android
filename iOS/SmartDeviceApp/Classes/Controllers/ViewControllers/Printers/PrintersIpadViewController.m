//
//  PrintersIpadViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIpadViewController.h"
#import "AddPrinterViewController.h"
#import "PrinterSearchViewController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterStatusView.h"
#import "AlertHelper.h"

#define SEGUE_TO_ADD    @"PrintersIpad-AddPrinter"
#define SEGUE_TO_SEARCH @"PrintersIpad-PrinterSearch"

@interface PrintersIpadViewController ()

#pragma mark - Data Properties

#pragma mark - UI Properties

@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;
@property (nonatomic) UIEdgeInsets insetPortrait;
@property (nonatomic) UIEdgeInsets insetLandscape;

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
   
    // Create insets for landscape and portrait orientations
    CGRect frame = [[UIScreen mainScreen] bounds];
    CGFloat basePortrait = MIN(frame.size.width, frame.size.height);
    CGFloat baseLandscape = MAX(frame.size.width, frame.size.height);
    int hInset;
    hInset = (basePortrait  - (320.0f * 2 + 10.0f * 1)) / 2.0f;
    self.insetPortrait = UIEdgeInsetsMake(10.0f, hInset, 10.0f, hInset);
    hInset = (baseLandscape - (320.0f * 3 + 10.0f * 2)) / 2.0f;
    self.insetLandscape = UIEdgeInsetsMake(10.0f, hInset, 10.0f, hInset);
    
    // Set insets based on current orientation
    if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
    {
        UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
        layout.sectionInset = self.insetLandscape;
    }
    else
    {
        UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
        layout.sectionInset = self.insetPortrait;
    }
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
    cell.nameLabel.text = printer.name;
    cell.ipAddressLabel.text = printer.ip_address;
    cell.portLabel.text = [printer.port stringValue];

    cell.statusView.statusHelper = [[PrinterStatusHelper alloc] initWithPrinterIP:printer.ip_address];
    cell.statusView.statusHelper.delegate = cell.statusView;

    [cell.statusView setStatus:[printer.onlineStatus boolValue]]; //initial status
    [cell.statusView.statusHelper startPrinterStatusPolling];
    return cell;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [self.collectionView performBatchUpdates:^
     {
        if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
        {
            UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
            layout.sectionInset = self.insetLandscape;
        }
        else
        {
            UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
            layout.sectionInset = self.insetPortrait;
        }
     } completion:^(BOOL finished)
     {
     }];
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
- (IBAction)printerCellLongPressedAction:(id)sender
{
    NSIndexPath *selectedIndexPath = [self.collectionView indexPathForItemAtPoint:[sender locationInView:self.collectionView]];
    PrinterCollectionViewCell *cell = nil;
    if(self.toDeleteIndexPath != nil)
    {
        cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
    }
    
    cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:selectedIndexPath];
    self.toDeleteIndexPath = selectedIndexPath;
    [cell setCellToBeDeletedState:YES];

}

- (IBAction)collectionViewTappedAction:(id)sender
{
    
    if(self.toDeleteIndexPath != nil)
    {
        PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.toDeleteIndexPath];
        [cell setCellToBeDeletedState:NO];
        self.toDeleteIndexPath = nil;
    }
}

- (IBAction)printerDeleteButtonAction:(id)sender
{
    if ([self.printerManager deletePrinterAtIndex:self.toDeleteIndexPath.row])
    {
        //check if reference to default printer was also deleted
        if (![self.printerManager hasDefaultPrinter])
            self.defaultPrinterIndexPath = nil;
        
        //set the view of the cell to stop polling for printer status
        PrinterCollectionViewCell *cell = (PrinterCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.toDeleteIndexPath];
        [cell.statusView.statusHelper stopPrinterStatusPolling];
        
        cell.indexPath = nil;
        //set view to non default printer cell style
        [cell setAsDefaultPrinterCell:NO];
        [cell setCellToBeDeletedState:NO];
        
        //remove cell from view
        [self.collectionView deleteItemsAtIndexPaths:@[self.toDeleteIndexPath]];
        self.toDeleteIndexPath = nil;
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDefault
                        withTitle:kAlertTitlePrinters
                      withDetails:nil];
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
    
}

#pragma mark - Reload

- (void)reloadData
{
    [super reloadData];
    [self.collectionView reloadData];
}

@end
