//
//  PrintSettingsOptionsItemCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsOptionsItemCell.h"
#import "UIColor+Theme.h"

@interface PrintSettingsOptionsItemCell()

@property (nonatomic, weak) IBOutlet UIImageView *radioImageView;

@end

@implementation PrintSettingsOptionsItemCell

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
    self.radioImageView.highlighted = selected;
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

@end
