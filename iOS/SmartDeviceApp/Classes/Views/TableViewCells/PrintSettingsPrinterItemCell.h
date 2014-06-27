//
//  PrintSettingsPrinterItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * PrintSettingsPrinterItemCell class is used to display the info on the currently selected printer on print settings screen.
 */
@interface PrintSettingsPrinterItemCell : UITableViewCell

/**
 * The name of the printer.
 */
@property (nonatomic, weak) IBOutlet UILabel *printerNameLabel;

/**
 * The IP address of the printer
 */
@property (nonatomic, weak) IBOutlet UILabel *printerIPLabel;

/**
 * Select printer label when there is no printer selected.
 */
@property (weak, nonatomic) IBOutlet UILabel *selectPrinterLabel;

/**
 Set the printerNameLabel to the name of the selected printer.
 If the printer has no name, it will set the printerNameLabel to "No Name".
 @param name Name of the printer
 */
- (void)setPrinterName:(NSString*)name;

@end
