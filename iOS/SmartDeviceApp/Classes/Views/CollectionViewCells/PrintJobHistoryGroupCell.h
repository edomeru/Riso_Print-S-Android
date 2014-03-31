//
//  PrintJobHistoryItemCell.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

#define GROUPCELL   @"PrintJobHistoryGroup"
#define ITEMCELL    @"PrintJobHistoryItem"

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
 Forces the cell to update its current view.
 */
- (void)reloadContents;

@end
