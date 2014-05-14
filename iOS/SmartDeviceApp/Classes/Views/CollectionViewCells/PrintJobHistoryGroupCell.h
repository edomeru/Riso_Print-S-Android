//
//  PrintJobHistoryItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintJobItemCell.h"
#import "DeleteButton.h"

#define GROUPCELL   @"PrintJobHistoryGroup"
#define TAG_FACTOR  1000

@class DeleteButton;

@protocol PrintJobHistoryGroupCellDelegate <NSObject>

@required
- (BOOL)shouldHighlightGroupHeader;
- (void)didTapGroupHeader:(NSUInteger)groupTag;
- (BOOL)shouldHighlightDeleteGroupButton;
- (void)didTapDeleteGroupButton:(DeleteButton*)button ofGroup:(NSUInteger)groupTag;
- (BOOL)shouldPutDeleteJobButton:(NSUInteger)groupTag;
- (void)didTapDeleteJobButton:(DeleteButton*)button ofJob:(NSUInteger)jobTag ofGroup:(NSUInteger)groupTag;

@end

@interface PrintJobHistoryGroupCell : UICollectionViewCell <UITableViewDataSource, UITableViewDelegate, DeleteButtonDelegate>

@property (weak, nonatomic) id<PrintJobHistoryGroupCellDelegate> delegate;

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
 Sets the IP address of the group to be displayed in the header.
 @param ip
        the group ip
 */
- (void)putGroupIP:(NSString*)ip;

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
 Removes an existing "DELETE" button from its items.
 */
- (void)removeDeleteButton;

/**
 Forces the cell to update its current layout and views.
 */
- (void)reloadContents;

/**
 Clears the highlight color of the group header.
 */
- (void)clearHeader;

@end
