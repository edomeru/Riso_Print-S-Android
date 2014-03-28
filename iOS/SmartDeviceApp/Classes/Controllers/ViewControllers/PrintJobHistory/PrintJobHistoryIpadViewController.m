//
//  PrintJobHistoryIpadViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryIpadViewController.h"

//const float CELL_SIZE_WIDTH     = 320.0f;
//const float CELL_SPACING_PORT   = 20.0f;
//const float CELL_SPACING_LAND   = 10.0f;
//const float CELL_MARGIN_TOP     = 10.0f;
//const float CELL_MARGIN_BOTTOM  = 10.0f;
//const float COLUMNS_PORT        = 2;
//const float COLUMNS_LAND        = 3;

@interface PrintJobHistoryIpadViewController ()

//@property (nonatomic) UIEdgeInsets insetPortrait;
//@property (nonatomic) UIEdgeInsets insetLandscape;

@end

@implementation PrintJobHistoryIpadViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
       
//    // Create insets for landscape and portrait orientations
//    CGRect frame = [[UIScreen mainScreen] bounds];
//    CGFloat basePortrait = MIN(frame.size.width, frame.size.height);
//    CGFloat baseLandscape = MAX(frame.size.width, frame.size.height);
//    int hInset;
//    hInset = (basePortrait  - (CELL_SIZE_WIDTH * COLUMNS_PORT + CELL_SPACING_PORT * COLUMNS_PORT-1)) / 2.0f;
//    self.insetPortrait = UIEdgeInsetsMake(CELL_MARGIN_TOP, hInset, CELL_MARGIN_BOTTOM, hInset);
//    hInset = (baseLandscape - (CELL_SIZE_WIDTH * COLUMNS_LAND + CELL_SPACING_LAND * COLUMNS_LAND-1)) / 2.0f;
//    self.insetLandscape = UIEdgeInsetsMake(CELL_MARGIN_TOP, hInset, CELL_MARGIN_BOTTOM, hInset);
//    
//    // Set insets based on current orientation
//    if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
//    {
//        UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
//        layout.sectionInset = self.insetLandscape;
//    }
//    else
//    {
//        UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
//        layout.sectionInset = self.insetPortrait;
//    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Rotation

//- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
//{
//    [self.collectionView performBatchUpdates:^
//     {
//         if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
//         {
//             UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
//             layout.sectionInset = self.insetLandscape;
//         }
//         else
//         {
//             UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
//             layout.sectionInset = self.insetPortrait;
//         }
//     } completion:^(BOOL finished)
//     {
//     }];
//}

#pragma mark - CollectionView

@end
