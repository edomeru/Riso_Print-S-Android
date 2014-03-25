//
//  UIView+Localization.m
//  SmartDeviceApp
//
//  Created by Seph on 3/24/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "UIView+Localization.h"

@implementation UIView (Localization)


- (NSString *)localizationId
{
    return nil;
}

- (void)setLocalizationId:(NSString *)localizationId
{
    if (localizationId != nil)
    {
        if([self isKindOfClass:UILabel.class])
        {
            UILabel *label = (UILabel *)self;
            [label setText:NSLocalizedString(localizationId, @"")];
        }
        else if ([self isKindOfClass:UIButton.class])
        {
            UIButton *button = (UIButton *)self;
            [button setTitle:NSLocalizedString(localizationId, @"") forState:UIControlStateNormal];
        }
    }
}

@end
