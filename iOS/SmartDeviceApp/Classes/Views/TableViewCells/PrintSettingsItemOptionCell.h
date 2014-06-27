//
//  PrintSettingsItemOptionCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * PrintSettingsItemOptionCell class is used to display the print settings item that opens another view for the list of options.
 */
@interface PrintSettingsItemOptionCell : UITableViewCell

/**
 * The label of the item.
 */
@property (nonatomic, weak) IBOutlet UILabel *settingLabel;

/**
 * The value of the currently selected option for the item.
 */
@property (nonatomic, weak) IBOutlet UILabel *valueLabel;

/**
 * Line separator of the items.
 */
@property (nonatomic, weak) IBOutlet UIView *separator;

/**
 * The > sign indicating that there is a list of options for the item.
 */
@property (weak, nonatomic) IBOutlet UIImageView *subMenuImage;

/**
 Shows or hides the currently selected option for the item.
 @param isValueHidden flag wether to show or hide the currently selected option for the item.
 */
- (void) setHideValue:(BOOL)isValueHidden;
@end
