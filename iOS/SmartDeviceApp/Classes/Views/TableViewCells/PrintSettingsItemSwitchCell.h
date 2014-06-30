//
//  PrintSettingsItemSwitchCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * PrintSettingsItemSwitchCell class is used to display the print settings item with switch as input.
 */
@interface PrintSettingsItemSwitchCell : UITableViewCell

/**
 * The label of the item
 */
@property (nonatomic, weak) IBOutlet UILabel *settingLabel;

/**
 * The switch for user input. 
 */
@property (nonatomic, weak) IBOutlet UISwitch *valueSwitch;

/**
 * Line separator of the items.
 */
@property (nonatomic, weak) IBOutlet UIView *separator;

@end
