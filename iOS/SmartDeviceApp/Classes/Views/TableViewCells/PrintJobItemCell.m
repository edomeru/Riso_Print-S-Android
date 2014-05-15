//
//  PrintJobItemCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobItemCell.h"
#import "UIColor+Theme.h"

@implementation PrintJobItemCell

#pragma mark - Lifecycle

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
    }
    return self;
}

#pragma mark - UI Properties

- (void)setBackgroundColors
{
    UIView* normalBackground = [[UIView alloc] init];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        normalBackground.backgroundColor = [UIColor gray2ThemeColor];
    else
        normalBackground.backgroundColor = [UIColor gray1ThemeColor];
    self.backgroundView = normalBackground;
    
    UIView* highlightedBackground = [[UIView alloc] init];
    highlightedBackground.backgroundColor = [UIColor purple2ThemeColor];
    self.selectedBackgroundView = highlightedBackground;
}

- (void)markForDeletion:(BOOL)marked
{
    if (marked)
    {
        self.timestamp.hidden = YES;
        [self.name setTextColor:[UIColor whiteThemeColor]];
        [self.backgroundView setBackgroundColor:[UIColor purple2ThemeColor]];
    }
    else
    {
        self.timestamp.hidden = NO;
        [self.name setTextColor:[UIColor blackColor]];
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
            [self.backgroundView setBackgroundColor:[UIColor gray2ThemeColor]];
        else
            [self.backgroundView setBackgroundColor:[UIColor gray1ThemeColor]];
    }
}

@end
