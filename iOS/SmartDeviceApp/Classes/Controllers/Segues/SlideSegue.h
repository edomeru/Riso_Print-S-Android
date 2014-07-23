//
//  SlidingSegue.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Custom segue that animates a View Controller from the side
 * of the screen towards the center.
 * This segue is used for SlidingViewController(s) that are
 * intented to partially cover the screen.
 *
 * @see SlideOverSegue
 * @see SlidingViewController
 */

@interface SlideSegue : UIStoryboardSegue

/**
 * Determines whether or not the segue should perform a forward
 * or backward animation.
 * - YES: The SlidingViewController will animate towards the side of the screen.
 * - NO: The SlidingViewController will animate towards the center of the screen.
 */
@property (nonatomic) BOOL isUnwinding;

@end
