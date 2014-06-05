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
 for all groups cannot be used) and whether the group
 should be displayed as collapsed or expanded.
 @param numJobs
        number of print jobs
 @param collapsed
        YES if group is collapsed, NO if expanded
 @param indexPath
        the group's index path
 */
- (void)getNumJobs:(NSUInteger*)numJobs getCollapsed:(BOOL*)collapsed forGroupAtIndexPath:(NSIndexPath*)indexPath;

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

/**
 Notifies the layout to update the arrangement of the groups in their
 respective columns during the next update.
 */
- (void)invalidateColumnAssignments;

/**
 Notifies the layout that the specified item will be deleted.
 This allows the layout to take note of the deleted group's
 frame, which is necessary to recalculate the other groups' 
 positions after the delete operation.
 */
- (void)prepareForDelete:(NSIndexPath*)itemToDelete;

@end
