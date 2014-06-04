//
//  PrintSettingsItemInputCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsItemInputCell.h"
#import "UIColor+Theme.h"

@implementation PrintSettingsItemInputCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
    self.contentView.backgroundColor = [UIColor gray2ThemeColor];
}

- (void)setEnabled:(BOOL)enabled
{
    self.settingLabel.enabled = enabled;
    self.valueTextField.enabled = enabled;
    
    if (enabled)
        self.valueTextField.textColor = [UIColor blackColor];
    else
        self.valueTextField.textColor = self.valueTextField.backgroundColor;
}

@end
