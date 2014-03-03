//
//  RootViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RootViewController : UIViewController

@property (nonatomic, weak) IBOutlet UIView *mainView;
@property (nonatomic, weak) IBOutlet UIView *slidingView;

@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftMainConstraint;
@property (nonatomic, weak) IBOutlet NSLayoutConstraint *leftSlidingConstraint;

@end
