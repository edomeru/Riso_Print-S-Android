//
//  PrintJobHistoryLayout.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/1/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PrintJobHistoryLayoutDelegate <NSObject>

/** 
 Requests the data source for the size of the current group
 to be displayed. A fixed size cannot be used since each
 group has a different number of print jobs to be displayed.
 @param indexPath
 */
- (CGSize)sizeForGroupAtIndexPath:(NSIndexPath*)indexPath;

@end

@interface PrintJobHistoryLayout : UICollectionViewLayout

/** Reference to the ViewController containing the UICollectionView */
@property (weak, nonatomic) id<PrintJobHistoryLayoutDelegate> delegate;

/**
 Initializes the properties containing the constant layout measurements
 depending on the specified orientation and device type.
 @param orientation
 @param device
 */
- (void)setupForOrientation:(UIInterfaceOrientation)orientation forDevice:(UIUserInterfaceIdiom)idiom;

@end
