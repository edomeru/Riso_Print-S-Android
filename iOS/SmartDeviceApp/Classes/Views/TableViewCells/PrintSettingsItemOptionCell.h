//
//  PrintSettingsItemOptionCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsItemOptionCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *settingLabel;
@property (nonatomic, weak) IBOutlet UILabel *valueLabel;
@property (nonatomic, weak) IBOutlet UIView *separator;
@property (weak, nonatomic) IBOutlet UIImageView *subMenuImage;

- (void) setHideValue:(BOOL)isValueHidden;
@end
