//
//  PrintSettingsPrinterHeaderCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsPrinterHeaderCell.h"
#import "UIColor+Theme.h"

@interface PrintSettingsPrinterHeaderCell ()

/**
 * Print icon of the button.
 */
@property (weak, nonatomic) IBOutlet UIImageView *printIcon;

/**
 * Print text of the button.
 */
@property (weak, nonatomic) IBOutlet UILabel *printText;

@end

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
    
    [self.printIcon setHighlighted:highlighted];
    if (self.highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple1ThemeColor];
        self.printText.textColor = [UIColor whiteThemeColor];
    }
    else
    {
        if (@available(iOS 13.0, *)) {
            self.contentView.backgroundColor = [UIColor colorNamed:@"color_white_gray2"];
            self.printText.textColor = [UIColor colorNamed:@"color_black_white"];
        } else {
            self.contentView.backgroundColor = [UIColor whiteThemeColor];
            self.printText.textColor = [UIColor blackThemeColor];
        }
    }
}

@end
