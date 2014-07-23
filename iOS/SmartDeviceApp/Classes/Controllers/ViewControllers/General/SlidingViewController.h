//
//  SlidingViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Slide Direction of the SlidingViewController
 */
typedef enum _SlideDirection
{
    SlideLeft = 0, /**< Slide from left. */
    SlideRight = 1, /**< Slide from right. */
} SlideDirection;

/**
 * Custom UIViewController that supports slide animation 
 * from either side of the screen.
 */
@interface SlidingViewController : UIViewController

/**
 * Determines the slide direction of the view when animated.
 * - SlideLeft: Slides from the left of the screen.
 * - SlideRight: Slides from the right of the screen.
 */
@property (nonatomic) SlideDirection slideDirection;

/**
 * Determines whether or not the sliding view will adjust the size of the
 * center view.
 * - YES: Center view will retain its size and will slide along the sliding view.
 * - NO: Center view will adjust its size accordingly to accommodate the width of
 * the sliding view.
 */
@property (nonatomic) BOOL isFixedSize;

/**
 * Performs the unwind action of the sliding view.
 */
- (void)close;

@end
