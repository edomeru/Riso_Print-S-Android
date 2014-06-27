//
//  UIColor+Theme.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Theme category provides methods to return the theme colors of the app.
 */
@interface UIColor (Theme)

/**
 Returns the black theme color.
 @return [UIColor blackColor]
 */
+ (UIColor *)blackThemeColor;

/**
 Returns the white theme color.
 @return [UIColor whiteColor]
 */
+ (UIColor *)whiteThemeColor;

/**
 Returns the Gray 1 theme color.
 @return UIColor(205, 205, 205, 1)
 */
+ (UIColor *)gray1ThemeColor;

/**
 Returns the Gray 2 theme color.
 @return UIColor(173, 173, 173, 1)
 */
+ (UIColor *)gray2ThemeColor;

/**
 Returns the Gray 3 theme color.
 @return UIColor(145, 145, 145, 1)
 */
+ (UIColor *)gray3ThemeColor;

/**
 Returns the Gray 4 theme color.
 @return UIColor(36, 36, 36, 1)
 */
+ (UIColor *)gray4ThemeColor;

/**
 Returns the Purple 1 theme color.
 @return UIColor(133, 65, 216, 1)
 */
+ (UIColor *)purple1ThemeColor;

/**
 Returns the Purple 2 theme color.
 @return UIColor(82, 7, 182, 1)
 */
+ (UIColor *)purple2ThemeColor;

//+ (UIColor *)redThemeColor;

@end
