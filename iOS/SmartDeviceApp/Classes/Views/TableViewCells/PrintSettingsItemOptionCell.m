//
//  PrintSettingsItemOptionCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsItemOptionCell.h"
#import "UIColor+Theme.h"

@implementation PrintSettingsItemOptionCell

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

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated
{
    [super setHighlighted:highlighted animated:animated];
    
    if (highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple1ThemeColor];
    }
    else
    {
        self.contentView.backgroundColor = [UIColor gray2ThemeColor];
    }
}


- (void) setUserInteractionEnabled:(BOOL)userInteractionEnabled
{
    [super setUserInteractionEnabled:userInteractionEnabled];
    if(userInteractionEnabled == NO)
    {
        self.settingLabel.enabled = NO;
        self.valueLabel.enabled = NO;
        self.subMenuImage.image = [UIImage imageNamed:@"img_btn_submenu_disabled"];
        
    }
    else
    {
        self.settingLabel.enabled = YES;
        self.valueLabel.enabled = YES;
        self.subMenuImage.image = [UIImage imageNamed:@"img_btn_submenu"];
    }
}

- (void) setHideValue:(BOOL)isValueHidden
{
    self.valueLabel.hidden = isValueHidden;
    self.subMenuImage.hidden = isValueHidden;
}

@end
