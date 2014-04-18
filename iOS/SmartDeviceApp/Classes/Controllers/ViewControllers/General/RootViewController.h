//
//  RootViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

@interface RootViewController : UIViewController

@property (nonatomic, weak) IBOutlet UIView *mainView;
@property (nonatomic, weak) IBOutlet UIView *leftSlidingView;
@property (nonatomic, weak) IBOutlet UIView *rightSlidingView;

@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftMainConstraint;
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *rightMainConstraint;
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftSlidingConstraint;
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *rightSlidingConstraint;

@property (nonatomic, weak) UIViewController *mainController;
@property (nonatomic, weak) SlidingViewController *sideController;

@end
