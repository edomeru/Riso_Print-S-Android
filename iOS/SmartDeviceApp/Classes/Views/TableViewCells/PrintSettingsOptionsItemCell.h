//
//  PrintSettingsOptionsItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PrinterStatusView;

/**
 * PrintSettingsOptionsItemCell class is used to display the options of the settings or list of printers for the printer screen on iPhone and iPod Touch.
 */
@interface PrintSettingsOptionsItemCell : UITableViewCell

/**
 * The value of the item for settings option or the Printer name for printers screen on iPhone and iPod Touch.
 */
@property (nonatomic, weak) IBOutlet UILabel *optionLabel;

/**
 * Line separator of the items.
 */
@property (nonatomic, weak) IBOutlet UIView *separator;

/**
 * Connectivity status of the printer for printers screen on iPhone and iPod Touch.
 */
@property (weak, nonatomic) IBOutlet PrinterStatusView *statusView;

/**
 * The IP address of the printer for printers screen on iPhone and iPod Touch.
 */
@property (weak, nonatomic) IBOutlet UILabel *subLabel;

@end
