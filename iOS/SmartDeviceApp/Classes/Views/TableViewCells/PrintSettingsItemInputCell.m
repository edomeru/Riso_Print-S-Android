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
    if (@available(iOS 13.0, *)) {
        self.contentView.backgroundColor = [UIColor colorNamed:@"color_gray2_gray5"];
        self.valueTextField.backgroundColor = [UIColor colorNamed:@"color_text_field"];
    } else {
        self.contentView.backgroundColor = [UIColor gray2ThemeColor];
    }
}

- (void)setEnabled:(BOOL)enabled
{
    [self setUserInteractionEnabled:enabled];
    self.settingLabel.enabled = enabled;
    self.valueTextField.enabled = enabled;
    
    if (enabled) {
        if (@available(iOS 13.0, *)) {
            self.valueTextField.textColor = [UIColor colorNamed:@"color_black_white"];
        } else {
            self.valueTextField.textColor = [UIColor blackColor];
        }
    }
    else {
        self.valueTextField.textColor = self.valueTextField.backgroundColor;
    }
}

@end
