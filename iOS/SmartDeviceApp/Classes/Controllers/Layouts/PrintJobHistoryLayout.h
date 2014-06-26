//
//  PrintJobHistoryLayout.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/** 
 * Allows the PrintJobHistoryLayout to coordinate with the Print Job History screen controller.
 * The Print Job History screen controller is required to conform to this protocol in order
 * for the layout to request information on print job history groups, such as the number of
 * print job history items each group contains and whether groups are collapsed or expanded.
 */
@protocol PrintJobHistoryLayoutDelegate <NSObject>

@required

/** 
 * Requests the Print Job History screen controller for the number
 * of print jobs for a group and its collapsed/expanded state.
 *
 * @param numJobs output parameter for setting the number of print jobs for the group
 * @param collapsed output parameter that is set to YES if group is collapsed, NO if expanded
 * @param indexPath the group's index path in the UICollectionView
 */
- (void)getNumJobs:(NSUInteger*)numJobs getCollapsed:(BOOL*)collapsed forGroupAtIndexPath:(NSIndexPath*)indexPath;

@end

/**
 * Custom UICollectionView layout class for organizing the content of the Print Job History screen.
 * The print job history groups are organized according to the following rules:
 *  - for phones, the groups are listed in a single continuous column
 *  - for tablets in portrait, the groups are listed in two columns
 *  - for tablets in landscape, the groups are listed in three columns
 *  - the groups are populated based on which column is the shortest so far
 *  - the groups' expanded heights are used to determine shortest column
 *  - the collapsed/expanded state is retained even during rotation
 *  - the group positions are retained even during rotation
 *  - the group positions are retained even during collapse/expand
 *  - when a group is deleted, the groups below it move up the column (others are unaffected)
 *  - when a group is deleted and the column becomes empty, the collection view should relayout
 *  - during rotation and a column becomes empty, the collection view should relayout
 */
@interface PrintJobHistoryLayout : UICollectionViewLayout

/** 
 * Reference to the Print Job History screen controller.
 */
@property (weak, nonatomic) id<PrintJobHistoryLayoutDelegate> delegate;

/** 
 * Reference to the bottom constraint of the UICollectionView.
 * This is used for adjusting the height of the UICollectionView (work-around).
 */
@property (weak, nonatomic) NSLayoutConstraint *bottomConstraint;

/**
 * Initializes the PrintJobHistoryLayout.
 * This method should be called when the Print Job History screen is displayed
 * and when the device is rotated.
 *
 * @param orientation landscape or portrait
 * @param device iPhone or iPad
 */
- (void)setupForOrientation:(UIInterfaceOrientation)orientation forDevice:(UIUserInterfaceIdiom)idiom;

/**
 * Resets the group positions in their respective columns.
 * The group positions are retained when the device is rotated, when a group is deleted, and
 * when a group is collapsed/expanded. Calling this method will invalidate those positions and
 * trigger a relayout during the next screen update.
 */
- (void)invalidateColumnAssignments;

/**
 * Notifies the PrintJobHistoryLayout that the specified group will be deleted.
 * This allows the layout to take note of the deleted group's frame, which is 
 * necessary to calculate the other groups' positions after the delete operation,
 * and to update its flags and trackers.
 *
 * @param itemToDelete index path of the group to delete in the UICollectionView
 */
- (void)prepareForDelete:(NSIndexPath*)itemToDelete;

@end
