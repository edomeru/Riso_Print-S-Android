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
