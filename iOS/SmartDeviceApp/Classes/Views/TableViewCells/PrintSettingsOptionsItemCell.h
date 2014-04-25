//
//  PrintSettingsOptionsItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PrinterStatusView;

@interface PrintSettingsOptionsItemCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *optionLabel;
@property (nonatomic, weak) IBOutlet UIView *separator;
@property (weak, nonatomic) IBOutlet PrinterStatusView *statusView;

@end
