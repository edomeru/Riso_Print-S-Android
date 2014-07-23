//
//  MenuButton.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Custom UIButton with selected state and separator.
 * Used as a series of menu list.
 */
@interface MenuButton : UIButton

/**
 * Determines whether or not the border (separator) is hidden or not.
 * - YES: Separator is hidden.
 * - NO: Separator is visible.
 */
@property (nonatomic) BOOL borderHidden;

/**
 * Color to be used when the button is tapped (highlighted).
 */
@property (nonatomic) UIColor *highlightColor;

/**
 * Color to be used when the button is selected.
 */
@property (nonatomic) UIColor *selectedColor;

@end
