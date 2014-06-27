//
//  PrintJobItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

#define PRINTJOBCELL    @"PrintJobItem"

@class DeleteButton;

/**
 * PrintJobItemCell class is used to display the list of saved print jobs.
 */
@interface PrintJobItemCell : UITableViewCell

/**
 * Line separator of cells of the saved printers.
 */
@property (weak, nonatomic) IBOutlet UIView* separator;

/**
 * Indicator wether the printing succeeded or failed.
 */
@property (weak, nonatomic) IBOutlet UIImageView* result;

/**
 * The filename of the document.
 */
@property (weak, nonatomic) IBOutlet UILabel* name;

/**
 * The date and time the document was printed.
 */
@property (weak, nonatomic) IBOutlet UILabel* timestamp;

/**
 * Delete button
 */
@property (weak, nonatomic) IBOutlet DeleteButton* deleteButton;

/**
 * Sets the background color of the cell depending on the cell state.
 */
- (void)setBackgroundColors;

/**
 * Sets the delete button highlight, text and text color.
 */
- (void)setDeleteButtonLayout;

/**
 Shows or hide the delete button.
 @param marked flag if the delete button should be shown or hid.
 */
- (void)markForDeletion:(BOOL)marked;

@end
