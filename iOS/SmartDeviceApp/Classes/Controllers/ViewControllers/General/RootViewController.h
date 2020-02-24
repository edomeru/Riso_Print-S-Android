//
//  RootViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

/**
 * Parent ViewController of the Sliding Design style.
 * Supports left and right sliding views.
 */

@interface RootViewController : UIViewController

/**
 * Container for the center view.
 */
@property (nonatomic, weak) IBOutlet UIView *mainView;

/**
 * Container for the left sliding view.
 */
@property (nonatomic, weak) IBOutlet UIView *leftSlidingView;

/**
 * Containter for the right sliding view.
 */
@property (nonatomic, weak) IBOutlet UIView *rightSlidingView;

/**
 * The view displayed on the left side
 * to prevent device elements such as sensor housing from obstructing the view.
 * This is only visible when running on an iPhone X in landscape left mode.
 */
@property (weak, nonatomic) IBOutlet UIView *leftNotchMaskView;

/**
 * The view displayed on the right side
 * to prevent device elements such as sensor housing from obstructing the view.
 * This is only visible when running on an iPhone X in landscape right mode.
 */
@property (weak, nonatomic) IBOutlet UIView *rightNotchMaskView;

/**
 * Constraint for adjusting the position of the center view from the left side.
 */
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftMainConstraint;

/**
 * Constraint for adjusting the position of the center view from the right side.
 */
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *rightMainConstraint;

/**
 * Constraint for adjusting the position of the left sliding view.
 */
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftSlidingConstraint;

/**
 * Constraint for adjusting the position of the right sliding view.
 */
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *rightSlidingConstraint;

/**
 * Constraint for setting the width of the notch mask view
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftNotchMaskWidthConstraint;

/**
 * Constraint for setting the width of the notch mask view
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightNotchMaskWidthConstraint;

/**
 * Current Center ViewController.
 */
@property (nonatomic, weak) UIViewController *mainController;

/**
 * Current SlidingViewController (left or right)
 */
@property (nonatomic, weak) SlidingViewController *sideController;

/**
 * Gets the instance of this class.
 * Note: Only one RootViewController should exist.
 *
 * @return RootViewController instance.
 */
+ (RootViewController *)container;

@end
