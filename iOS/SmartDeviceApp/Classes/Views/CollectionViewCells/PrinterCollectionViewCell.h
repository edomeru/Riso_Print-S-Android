//
//  PrinterCollectionViewCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PrinterStatusView;
@class DeleteButton;

/**
 * PrinterCollectionViewCell class is used to display the list of saved printers on iPad.
 */
@interface PrinterCollectionViewCell : UICollectionViewCell

/**
 * The name if the printer.
 */
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;

/**
 * The IP address of the printer.
 */
@property (weak, nonatomic) IBOutlet UILabel *ipAddressLabel;

/**
 * A UISegmentControl used to select the port to be used by the printer.
 */
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;

/**
 * A UISegmentControl to set the printer as default.
 */
@property (weak, nonatomic) IBOutlet UISegmentedControl *defaultPrinterSelection;

/**
 * Connectivity status of the printer.
 */
@property (weak, nonatomic) IBOutlet PrinterStatusView *statusView;

/**
 * Header of the CollectionViewCell.
 * Black header indicates that the printer is set as default.
 */
@property (weak, nonatomic) IBOutlet UIView *cellHeader;

/**
 * Button to open the printer settings.
 */
@property (weak, nonatomic) IBOutlet UIButton *defaultSettingsButton;

/**
 * Button to delete the printer
 */
@property (weak, nonatomic) IBOutlet DeleteButton *deleteButton;

/**
 * This view is highlighted when the user taps the defaultSettingsButton.
 */
@property (weak, nonatomic) IBOutlet UIView *defaultSettingsRow;

/**
 * Sets the cell as default printer.
 * Changes the cell header to black; Hide the default switch and show the deafult icon.
 * @param isDefaultPrinter flag if the printer is default or not.
 */
- (void)setAsDefaultPrinterCell:(BOOL)isDefaultPrinterCell;

/**
 * Marks the cell for deletion.
 * @param isCellForDelete flag if the printer is to be deleted.
 */
- (void)setCellToBeDeletedState:(BOOL)isCellForDelete;

/**
 * Highlights the default settings button when selected
 * @param isSelected flag if default settings button is pressed.
 */
- (void)setDefaultSettingsRowToSelected:(BOOL)isSelected;
@end
