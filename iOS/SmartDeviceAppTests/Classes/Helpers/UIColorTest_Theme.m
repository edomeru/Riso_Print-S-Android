//
//  UIColorTest_Theme.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "UIColor+Theme.h"

@interface UIColorTest_Theme : GHTestCase

- (bool)isColor:(UIColor *)color1 equalToColor:(UIColor *)color2;

@end

@implementation UIColorTest_Theme

- (void)testBlackThemeColor
{
    UIColor *color = [UIColor blackThemeColor];
    GHAssertEqualObjects(color, [UIColor blackColor], @"[UIColor blackThemeColor] must be equal to [UIColor blackColor");
}

- (void)testWhiteThemeColor
{
    UIColor *color = [UIColor whiteThemeColor];
    GHAssertEqualObjects(color, [UIColor whiteColor], @"[UIColor whiteThemeColor] must be equal to [UIColor whiteColor");
}

- (void)testGray1ThemeColor
{
    UIColor *color = [UIColor gray1ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:205/255.0f green:205/255.0f blue:205/255.0f alpha:1]], @"[UIColor gray1ThemeColor] must be 205,205,205");
}

- (void)testGray2ThemeColor
{
    UIColor *color = [UIColor gray2ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:173.0f/255.0f green:173.0f/255.0f blue:173.0f/255.0f alpha:1.0f]], @"[UIColor gray2ThemeColor] must be 173,173,173");
}

- (void)testGray3ThemeColor
{
    UIColor *color = [UIColor gray3ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:145.0f/255.0f green:145.0f/255.0f blue:145.0f/255.0f alpha:1.0f]], @"[UIColor gray3ThemeColor] must be 145,145,145");
}

- (void)testGray4ThemeColor
{
    UIColor *color = [UIColor gray4ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:36.0f/255.0f green:36.0f/255.0f blue:36.0f/255.0f alpha:1.0f]], @"[UIColor gray4ThemeColor] must be 36,36,36");
}

- (void)testPurple1ThemeColor
{
    UIColor *color = [UIColor purple1ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:133.0f/255.0f green:65.0f/255.0f blue:216.0f/255.0f alpha:1.0f]], @"[UIColor purple1ThemeColor] must be 133,65,216");
}

- (void)testPurple2ThemeColor
{
    UIColor *color = [UIColor purple2ThemeColor];
    GHAssertTrue([self isColor:color equalToColor:[UIColor colorWithRed:82.0f/255.0f green:7.0f/255.0f blue:182.0f/255.0f alpha:1.0f]], @"[UIColor purple2ThemeColor] must be 82,7,182");
}

- (bool)isColor:(UIColor *)color1 equalToColor:(UIColor *)color2
{
    const CGFloat tolerance = 0.0001;
    
    CGColorRef color1CGColor = [color1 CGColor];
    CGColorRef color2CGColor = [color2 CGColor];
    
    unsigned long color1ComponentCount = CGColorGetNumberOfComponents(color1CGColor);
    unsigned long color2ComponentCount = CGColorGetNumberOfComponents(color2CGColor);
    
    if(color1ComponentCount != color2ComponentCount){
        return NO;
    }
    
    const CGFloat *color1Components = CGColorGetComponents(color1CGColor);
    const CGFloat *color2Components = CGColorGetComponents(color2CGColor);
    
    for(int i = 0; i < color1ComponentCount; i++){
        CGFloat difference = color1Components[i] - color2Components[i];
        
        if(fabs(difference) > tolerance){
            return NO;
        }
    }
    
    return YES;
}

@end
