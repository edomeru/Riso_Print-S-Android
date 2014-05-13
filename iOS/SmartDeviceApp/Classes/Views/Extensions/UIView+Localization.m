//
//  UIView+Localization.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "UIView+Localization.h"

@implementation UIView (Localization)

- (NSString *)localizationId
{
    return nil;
}

- (NSString *)uppercaseLocalizationId
{
    return nil;
}

- (void)setLocalizationId:(NSString *)localizationId
{
    if (localizationId != nil)
    {
        NSString *upperCased = [localizationId uppercaseString];
        if([self isKindOfClass:UILabel.class])
        {
            UILabel *label = (UILabel *)self;
            [label setText:NSLocalizedString(upperCased, @"")];
        }
        else if ([self isKindOfClass:UIButton.class])
        {
            UIButton *button = (UIButton *)self;
            [button setTitle:NSLocalizedString(upperCased, @"") forState:UIControlStateNormal];
        }
    }
}

- (void)setUppercaseLocalizationId:(NSString *)localizationId
{
    if (localizationId != nil)
    {
        NSString *upperCased = [localizationId uppercaseString];
        if([self isKindOfClass:UILabel.class])
        {
            UILabel *label = (UILabel *)self;
            [label setText:[NSLocalizedString(upperCased, @"") uppercaseString]];
        }
        else if ([self isKindOfClass:UIButton.class])
        {
            UIButton *button = (UIButton *)self;
            [button setTitle:[NSLocalizedString(upperCased, @"") uppercaseString] forState:UIControlStateNormal];
        }
    }
}

@end
