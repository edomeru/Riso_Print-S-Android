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

/**
 * PrintJobHistoryGroupCellDelegate protocol provides methods to inform the delegate about user actions.
 * These actions include tapping the header, deleting a group of print jobs or just a single print job.
 */
@protocol PrintJobHistoryGroupCellDelegate <NSObject>

@required

/**
 Checks if the header needs to be highlighted.
 When the user taps the group header, it should be highlighted.
 @return YES if header needs to be highlighted, NO otherwise.
 */
- (BOOL)shouldHighlightGroupHeader;

/**
 Called when the user taps the header
 @param groupTag The tag of the group being tapped.
 */
- (void)didTapGroupHeader:(NSUInteger)groupTag;

/**
 Checks if the delete group button needs to be highlighted.
 @return YES if delete group button needs to be highlighted, NO otherwise.
*/
- (BOOL)shouldHighlightDeleteGroupButton;

/**
 Called when the user taps the delete group button
 @param groupTag The tag of the group.
 */
- (void)didTapDeleteGroupButton:(DeleteButton*)button ofGroup:(NSUInteger)groupTag;

/**
 Checks if the delete button for a print job needs to be displayed.
 When the user swiped left on the printjob item, delete button should be displayed.
 @param groupTag The tag of the group.
 */
- (BOOL)shouldPutDeleteJobButton:(NSUInteger)groupTag;

/**
 Called when the user taps the delete job button
 @param jobtag The tag of the job to be deleted.
 @param groupTag The tag of the group being tapped.
 */
- (void)didTapDeleteJobButton:(DeleteButton*)button ofJob:(NSUInteger)jobTag ofGroup:(NSUInteger)groupTag;

/**
 Checks if the print job item needs to be highlighted.
 When the user swiped left to show the delete button, the cell should be highlighted.
 @return YES if the print job item needs to be highlighted, NO otherwise.
 */
- (BOOL)shouldHighlightJob;

@end

/**
 * PrintJobHistoryGroupCell class is used to display the list of saved print jobs.
 */
@interface PrintJobHistoryGroupCell : UICollectionViewCell <UITableViewDataSource, UITableViewDelegate, DeleteButtonDelegate>

/**
 * The delegate receives notification that the user performs an action on the print job history group.
 */
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
