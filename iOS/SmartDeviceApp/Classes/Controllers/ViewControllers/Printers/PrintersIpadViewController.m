//
//  PrintersIpadViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintersIpadViewController.h"
#import "PrinterManager.h"
#import "Printer.h"
#import "PrinterCollectionViewCell.h"
#import "PrinterDetails.h"
#import "PrinterStatusView.h"
#import "PrinterStatusHelper.h"

@interface PrintersIpadViewController ()

@property (strong, nonatomic) PrinterManager* printerManager;
@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;
@property (nonatomic) UIEdgeInsets insetPortrait;
@property (nonatomic) UIEdgeInsets insetLandscape;

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

    self.printerManager = [[PrinterManager alloc] init];
    [self.printerManager getListOfSavedPrinters];
    [self.printerManager getDefaultPrinter];
    
    // For TEST data creation only
    /*for (int i = 0; i < 5; i++)
    {
        PrinterDetails *printer = [[PrinterDetails alloc] init];
        printer.name = [NSString stringWithFormat:@"Riso Printer #%d", i];
        printer.ip = [NSString stringWithFormat:@"192.168.1.%d", 101 + i];
        printer.port = [NSNumber numberWithInt:515];
        [self.printerManager registerPrinter:printer];
    }*/
    
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
    return [self.printerManager.listSavedPrinters count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    PrinterCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell" forIndexPath:indexPath];
    Printer *printer = [self.printerManager.listSavedPrinters objectAtIndex:[indexPath item]];
    cell.nameLabel.text = printer.name;
    cell.ipAddressLabel.text = printer.ip_address;
    cell.portLabel.text = [printer.port stringValue];
    cell.defaultSwitch.on = NO;
    
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

#pragma mark -
#pragma mark IBActions
- (IBAction)addPrinterAction:(id)sender
{
    // TODO: Change button state
    [super addPrinterAction:sender];
}

- (IBAction)printerSearchAction:(id)sender
{
    // TODO: Change button state
    [super printerSearchAction:sender];
}

@end
