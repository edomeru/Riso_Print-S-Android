//
//  UIColor+Theme.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "UIColor+Theme.h"

@interface UIColor(Theme_Internal)

+ (UIColor *)colorWithR:(CGFloat)red G:(CGFloat)green B:(CGFloat)blue A:(CGFloat)alpha;

@end

@implementation UIColor(Theme_Internal)

+ (UIColor *)colorWithR:(CGFloat)red G:(CGFloat)green B:(CGFloat)blue A:(CGFloat)alpha
{
    return [UIColor colorWithRed:red/255.0f green:green/255.0f blue:blue/255.0f alpha:alpha];
}

@end

@implementation UIColor (Theme)

+ (UIColor *)blackThemeColor
{
    return [UIColor blackColor];
}

+ (UIColor *)whiteThemeColor
{
    return [UIColor whiteColor];
}

+ (UIColor *)gray1ThemeColor
{
    return [UIColor colorWithR:205.0f G:205.0f B:205.0f A:1.0f];
}

+ (UIColor *)gray2ThemeColor
{
    return [UIColor colorWithR:173.0f G:173.0f B:173.0f A:1.0f];
}

+ (UIColor *)gray3ThemeColor
{
    return [UIColor colorWithR:145.0f G:145.0f B:145.0f A:1.0f];
}

+ (UIColor *)gray4ThemeColor
{
    return [UIColor colorWithR:36.0f G:36.0f B:36.0f A:1.0f];
}

+ (UIColor *)purple1ThemeColor
{
    return [UIColor colorWithR:133.0f G:65.0f B:216.0f A:1.0f];
}

+ (UIColor *)purple2ThemeColor
{
    return [UIColor colorWithR:82.0f G:7.0f B:182.0f A:1.0f];
}

//+ (UIColor *)redThemeColor
//{
//    return [UIColor colorWithR:248.0f G:75.0f B:75.0f A:1.0f];
//}

@end
