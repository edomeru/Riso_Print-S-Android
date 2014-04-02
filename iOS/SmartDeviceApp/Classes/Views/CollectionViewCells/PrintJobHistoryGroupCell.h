//
//  PrintJobHistoryItemCell.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

#define GROUPCELL       @"PrintJobHistoryGroup"
#define PRINTJOBCELL    @"PrintJobHistoryItem"

#define TAG_FACTOR  1000

@protocol PrintJobHistoryGroupCellDelegate <NSObject>

@end

@interface PrintJobHistoryGroupCell : UICollectionViewCell <UITableViewDataSource, UITableViewDelegate>

/**
 Sets-up the internal properties of the cell.
 @param tag
        unique identifier used to determine which cell will respond
        to specific actions (i.e. collapse/expand, delete all)
 */
- (void)initWithTag:(NSInteger)tag;

/**
 Sets the unique group name to be displayed in the header.
 @param name
        the group name
 */
- (void)putGroupName:(NSString*)name;

/**
 Toggles the collapsed/expanded indicator.
 @param isCollapsed
        YES if the group is collapsed, NO otherwise
 */
- (void)putIndicator:(BOOL)isCollapsed;

/**
 Adds a print job to the list.
 @param name
        the print job name
 @param result
        YES if OK, NO if NG
 @param timestamp
        the date and time
 */
- (void)putPrintJob:(NSString*)name withResult:(BOOL)result withTimestamp:(NSDate*)timestamp;

/**
 Adds a "DELETE" button to an item.
 @param gesture
        the swipe gesture where the user expects the delete button to appear
 @param receiver
        receiver for the "DELETE" button's tap action
 @param actionOnTap
        method of the receiver that will be triggered when the "DELETE" button is tapped
 */
- (void)putDeleteButton:(UIGestureRecognizer*)gesture handledBy:(id<PrintJobHistoryGroupCellDelegate>)receiver usingActionOnTap:(SEL)actionOnTap;

/**
 Removes an existing "DELETE" button from an item.
 */
- (void)removeDeleteButton;

/**
 Removes the print job item at the specified index.
 @param indexPath
        indexPath for the row to be deleted
 */
- (void)removePrintJob:(NSIndexPath*)indexPath;

/**
 Forces the cell to update its current view.
 */
- (void)reloadContents;

@end
