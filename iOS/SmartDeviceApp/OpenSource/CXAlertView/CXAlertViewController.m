//
//  CXAlertViewController.m
//  CXAlertViewDemo
//
//  Created by ChrisXu on 13/9/12.
//  Copyright (c) 2013年 ChrisXu. All rights reserved.
//

#import "CXAlertViewController.h"

@interface CXAlertView ()

- (void)setup;
- (void)resetTransition;
- (void)invalidateLayout;

@end

@interface CXAlertViewController ()

@end

@implementation CXAlertViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

#pragma mark - View life cycle

- (void)loadView
{
    self.view = self.alertView;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.alertView setup];
    
    [UIApplication sharedApplication].statusBarHidden = _rootViewControllerPrefersStatusBarHidden;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [UIApplication sharedApplication].statusBarHidden = _rootViewControllerPrefersStatusBarHidden;
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
    [UIApplication sharedApplication].statusBarHidden = _rootViewControllerPrefersStatusBarHidden;
}

//SDA Fix:Replace deprecated method to remove warnings
//- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
-(void) viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [self.alertView resetTransition];
    [self.alertView invalidateLayout];
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}

//SDA Fix: Comment out deprecated method to remove static warnings
/*
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return YES;
}
*/

- (BOOL)shouldAutorotate
{
    return YES;
}
//SDA Fix: Comment out so visibility of status bar is not altered by alert during rotation
/*
 - (BOOL)prefersStatusBarHidden
{
    return _rootViewControllerPrefersStatusBarHidden;
}
 */

//SDA Fix: Added method override to keep status bar style when alert is shown
- (UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

@end
