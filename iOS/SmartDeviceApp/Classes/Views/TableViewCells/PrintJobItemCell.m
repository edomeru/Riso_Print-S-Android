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

- (void)setDeleteState:(BOOL)isDelete
{
    if (isDelete)
    {
        self.timestamp.hidden = YES;
        [self.name setTextColor:[UIColor whiteThemeColor]];
        [self.contentView setBackgroundColor:[UIColor purple2ThemeColor]];
    }
    else
    {
        self.timestamp.hidden = NO;
        [self.name setTextColor:[UIColor blackThemeColor]];
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
            [self.contentView setBackgroundColor:[UIColor gray2ThemeColor]]; //set to be darker than background
        else
            [self.contentView setBackgroundColor:[UIColor gray1ThemeColor]];
    }
}

@end
