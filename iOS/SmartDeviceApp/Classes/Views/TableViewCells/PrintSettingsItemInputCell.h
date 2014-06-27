//
//  PrintSettingsItemInputCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * PrintSettingsItemInputCell class is used to display the print settings item with text field as input.
 */
@interface PrintSettingsItemInputCell : UITableViewCell

/**
 * The label of the item
 */
@property (nonatomic, weak) IBOutlet UILabel *settingLabel;

/**
 * The input text field of the item
 */
@property (nonatomic, weak) IBOutlet UITextField *valueTextField;

/**
 * Line separator of the items.
 */
@property (nonatomic, weak) IBOutlet UIView *separator;

/**
 * Flag if the option is available or not.
 */
- (void)setEnabled:(BOOL)enabled;

@end
