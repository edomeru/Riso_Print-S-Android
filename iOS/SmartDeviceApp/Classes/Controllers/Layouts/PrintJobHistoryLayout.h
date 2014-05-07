//
//  PrintJobHistoryLayout.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PrintJobHistoryLayoutDelegate <NSObject>

/** 
 Requests the data source for the number of print jobs
 to be displayed for the current group (a fixed size
 for all groups cannot be used).
 @param indexPath
 */
- (NSUInteger)numberOfJobsForGroupAtIndexPath:(NSIndexPath*)indexPath;

@end

@interface PrintJobHistoryLayout : UICollectionViewLayout

/** Reference to the ViewController containing the UICollectionView */
@property (weak, nonatomic) id<PrintJobHistoryLayoutDelegate> delegate;

/**
 Initializes the properties containing the constant layout measurements
 depending on the specified orientation and device type.
 @param orientation
        landscape or portrait
 @param device
        iPhone or iPad
 */
- (void)setupForOrientation:(UIInterfaceOrientation)orientation forDevice:(UIUserInterfaceIdiom)idiom;

@end
