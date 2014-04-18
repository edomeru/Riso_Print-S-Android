//
//  PrintSettingsPrinterItemCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsPrinterItemCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *printerNameLabel;
@property (nonatomic, weak) IBOutlet UILabel *printerIPLabel;
@property (weak, nonatomic) IBOutlet UILabel *selectPrinterLabel;
@end
