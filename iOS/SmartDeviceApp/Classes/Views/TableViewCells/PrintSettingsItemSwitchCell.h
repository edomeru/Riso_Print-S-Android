//
//  PrintSettingsItemSwitchCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsItemSwitchCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *settingLabel;
@property (nonatomic, weak) IBOutlet UISwitch *valueSwitch;
@property (nonatomic, weak) IBOutlet UIView *separator;

@end