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
    GHAssertEqualObjects(color, [UIColor colorWithRed:205/255.0f green:205/255.0f blue:205/255.0f alpha:1], @"[UIColor whiteThemeColor] must be 205,205,205");
}

- (void)testGray2ThemeColor
{
    UIColor *color = [UIColor gray2ThemeColor];
    GHAssertEqualObjects(color, [UIColor colorWithRed:173.0f/255.0f green:173.0f/255.0f blue:173.0f/255.0f alpha:1.0f], @"[UIColor whiteThemeColor] must be 173,173,173");
}

- (void)testGray3ThemeColor
{
    UIColor *color = [UIColor gray3ThemeColor];
    GHAssertEqualObjects(color, [UIColor colorWithRed:145.0f/255.0f green:145.0f/255.0f blue:145.0f/255.0f alpha:1.0f], @"[UIColor whiteThemeColor] must be 145,145,145");
}

- (void)testGray4ThemeColor
{
    UIColor *color = [UIColor gray4ThemeColor];
    GHAssertEqualObjects(color, [UIColor colorWithRed:36.0f/255.0f green:36.0f/255.0f blue:36.0f/255.0f alpha:1.0f], @"[UIColor whiteThemeColor] must be 36,36,36");
}

- (void)testPurple1ThemeColor
{
    UIColor *color = [UIColor purple1ThemeColor];
    GHAssertEqualObjects(color, [UIColor colorWithRed:133.0f/255.0f green:65.0f/255.0f blue:216.0f/255.0f alpha:1.0f], @"[UIColor whiteThemeColor] must be 133,65,216");
}

- (void)testPurple2ThemeColor
{
    UIColor *color = [UIColor purple2ThemeColor];
    GHAssertEqualObjects(color, [UIColor colorWithRed:82.0f/255.0f green:7.0f/255.0f blue:182.0f/255.0f alpha:1.0f], @"[UIColor whiteThemeColor] must be 82,7,182");
}

@end
