//
//  PrintSettingsPrinterItemCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsPrinterItemCell.h"
#import "UIColor+Theme.h"

@implementation PrintSettingsPrinterItemCell

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

- (void)setPrinterName:(NSString*)name;
{
    if (name == nil || [name isEqualToString:@""])
    {
        self.printerNameLabel.text = NSLocalizedString(IDS_LBL_NO_NAME, @"");
    }
    else
    {
        self.printerNameLabel.text = name;
    }
}

@end
