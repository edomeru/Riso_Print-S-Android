//
//  PrintSettingsPrinterItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsPrinterItemCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *printerNameLabel;
@property (nonatomic, weak) IBOutlet UILabel *printerIPLabel;
@property (weak, nonatomic) IBOutlet UILabel *selectPrinterLabel;

- (void)setPrinterName:(NSString*)name;

@end
