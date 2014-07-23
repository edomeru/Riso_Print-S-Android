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
 * Returns the black theme color.
 * @return [UIColor blackColor]
 */
+ (UIColor *)blackThemeColor;

/**
 * Returns the white theme color.
 * @return [UIColor whiteColor]
 */
+ (UIColor *)whiteThemeColor;

/**
 * Returns the Gray 1 theme color.
 * @return UIColor with value red: 205, green: 205, blue: 205, alpha: 1
 */
+ (UIColor *)gray1ThemeColor;

/**
 * Returns the Gray 2 theme color.
 * @return UIColor with value red: 173, green: 173, blue: 173, alpha: 1
 */
+ (UIColor *)gray2ThemeColor;

/**
 * Returns the Gray 3 theme color.
 * @return UIColor with value red: 145, green: 145, blue: 145, alpha: 1
 */
+ (UIColor *)gray3ThemeColor;

/**
 * Returns the Gray 4 theme color.
 * @return UIColor with value red: 36, green: 36, blue: 36, alpha: 1
 */
+ (UIColor *)gray4ThemeColor;

/**
 * Returns the Purple 1 theme color.
 * @return UIColor with value red: 133, green: 65, blue: 216, alpha: 1
 */
+ (UIColor *)purple1ThemeColor;

/**
 * Returns the Purple 2 theme color.
 * @return UIColor with value red: 82, green: 7, blue: 182, alpha: 1
 */
+ (UIColor *)purple2ThemeColor;

//+ (UIColor *)redThemeColor;

@end
