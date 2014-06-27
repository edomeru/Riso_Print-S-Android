//
//  PrinterCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusView.h"
#import "DeleteButton.h"

/**
 * PrinterCell class is used to display the list of saved printers on iPhone and iPod Touch.
 */
@interface PrinterCell : UITableViewCell

/**
 * The name if the printer.
 */
@property (weak, nonatomic) IBOutlet UILabel *printerName;

/**
 * Connectivity status of the printer.
 */
@property (weak, nonatomic) IBOutlet PrinterStatusView *printerStatus;

/**
 * Line separator of cells of the saved printers.
 */
@property (weak, nonatomic) IBOutlet UIView* separator;

/**
 * The IP address of the printer.
 */
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;

/**
 Sets the cell state for deletion.
 @param isCellForDelete flag if the cell is to be deleted or not.
 */
- (void)setCellToBeDeletedState:(BOOL)isCellForDelete;

/**
 * Sets the cell style for a default printer cell.
 */
- (void)setCellStyleForDefaultCell;

/**
 * Sets the cell style a normal cell.
 */
- (void)setCellStyleForNormalCell;

/**
 * Sets the cell style when deleting a printer.
 */
- (void)setCellStyleForToDeleteCell;

/**
 * Removes the delete button.
 */
- (void)cancelDeleteButton;

/**
 * Sets the delete button highlight, text and text color.
 */
- (void)setDeleteButtonLayout;

@end
