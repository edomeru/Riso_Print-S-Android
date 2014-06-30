//
//  UIView+Localization.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Adds localization support to UILabels and UIButtons
 */
@interface UIView (Localization)

/**
 * ID of the localization string.
 * Uses the default case used in the strings file.
 *
 * @see uppercaseLocalizationId
 */
@property (nonatomic, strong) NSString *localizationId;

/**
 * ID of the localization string.
 * Forces the strings to be in uppercase.
 *
 * @see localizationId
 */
@property (nonatomic, strong) NSString *uppercaseLocalizationId;

@end
