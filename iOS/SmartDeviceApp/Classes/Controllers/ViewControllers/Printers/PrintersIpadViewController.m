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

@interface PrintersIpadViewController ()

@property (strong, nonatomic) PrinterManager* printerManager;
@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;
@property (nonatomic, strong) UICollectionViewFlowLayout *layoutPortrait;
@property (nonatomic, strong) UICollectionViewFlowLayout *layoutLandscape;

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
    [self.printerManager getPrinters];
    [self.printerManager getDefaultPrinter];
    
    self.collectionView.delegate = self;
    self.collectionView.dataSource = self;
   
    CGRect frame = [[UIScreen mainScreen] bounds];
    CGFloat basePortrait = MIN(frame.size.width, frame.size.height);
    CGFloat baseLandscape = MAX(frame.size.width, frame.size.height);
    int hInset;
    // Prepare layout for portrait
    self.layoutPortrait = [[UICollectionViewFlowLayout alloc] init];
    hInset = (basePortrait  - (320.0f * 2 + 10.0f * 1)) / 2.0f;
    self.layoutPortrait.sectionInset = UIEdgeInsetsMake(10.0f, hInset, 10.0f, hInset);
    
    // Prepare layout for landscape
    self.layoutLandscape = [[UICollectionViewFlowLayout alloc] init];
    hInset = (baseLandscape - (320.0f * 3 + 10.0f * 2)) / 2.0f;
    self.layoutLandscape.sectionInset = UIEdgeInsetsMake(10.0f, hInset, 10.0f, hInset);
    
    // Set layout based on orientation
    if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
    {
        self.collectionView.collectionViewLayout = self.layoutLandscape;
    }
    else
    {
        self.collectionView.collectionViewLayout = self.layoutPortrait;
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
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    // TODO: Add to constant
    return CGSizeMake(320, 270);
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
    {
        [self.collectionView setCollectionViewLayout:self.layoutLandscape animated:YES];
    }
    else
    {
        [self.collectionView setCollectionViewLayout:self.layoutPortrait animated:YES];
    }
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
