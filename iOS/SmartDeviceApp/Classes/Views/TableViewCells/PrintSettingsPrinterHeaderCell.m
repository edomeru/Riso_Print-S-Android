//
//  PrintSettingsPrinterHeaderCell.m
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsPrinterHeaderCell.h"
#import "UIColor+Theme.h"

@implementation PrintSettingsPrinterHeaderCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated
{
    [super setHighlighted:highlighted animated:animated];
    
    if (self.highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple1ThemeColor];
    }
    else
    {
        self.contentView.backgroundColor = [UIColor blackThemeColor];
    }
}

@end
