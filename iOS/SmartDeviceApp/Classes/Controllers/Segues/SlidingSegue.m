//
//  SlidingSegue.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SlidingSegue.h"
#import "RootViewController.h"

const float AnimationDuration = 0.3f;

@interface SlidingSegue()

- (void)performSegue;
- (void)performUnwindSegue;

@end

@implementation SlidingSegue

- (id)initWithIdentifier:(NSString *)identifier source:(UIViewController *)source destination:(UIViewController *)destination
{
    _isUnwinding = NO;
    _slideDirection = SlideLeft;
    return [super initWithIdentifier:identifier source:source destination:destination];
}

- (void)perform
{
    if (self.isUnwinding == NO)
    {
        [self performSegue];
    }
    else
    {
        [self performUnwindSegue];
    }
}

- (void)performSegue
{
    UIViewController *mainViewController = self.sourceViewController;
    UIViewController *slidingViewController = self.destinationViewController;
    RootViewController *container = (RootViewController *) mainViewController.parentViewController;
   
    UIView *slidingView;
    NSLayoutConstraint *slidingConstraint;
    
    if (self.slideDirection == SlideLeft)
    {
        slidingView = container.leftSlidingView;
        slidingConstraint = container.leftSlidingConstraint;
    }
    else
    {
        slidingView = container.rightSlidingView;
        slidingConstraint = container.rightSlidingConstraint;
    }
    
    // Reset constraints
    slidingConstraint.constant = 0;
    container.leftMainConstraint.constant = 0;
    [container.view layoutIfNeeded];
    CGRect slidingFrame = CGRectMake(0, 0, slidingView.frame.size.width, slidingView.frame.size.height);

    // Add sliding container
    [container addChildViewController:slidingViewController];
    slidingViewController.view.frame = slidingFrame;
    [slidingView addSubview:slidingViewController.view];
    slidingConstraint.constant = -slidingFrame.size.width;
    [container.view layoutIfNeeded];
    
    // Prepare animation
    slidingConstraint.constant = 0;
    if (self.slideDirection == SlideLeft)
    {
        container.leftMainConstraint.constant = slidingFrame.size.width;
    }
    else
    {
        container.leftMainConstraint.constant = -slidingFrame.size.width;
    }
    
    [UIView animateWithDuration:AnimationDuration animations:^
     {
         [container.view layoutIfNeeded];
     } completion:^(BOOL finished)
     {
         // Disable interaction on main view
         [mainViewController.view setUserInteractionEnabled:NO];
         
         // Notify events
         [mainViewController didMoveToParentViewController:nil];
         [slidingViewController didMoveToParentViewController:container];
     }];
}

- (void)performUnwindSegue
{
    UIViewController *mainViewController = self.destinationViewController;
    UIViewController *slidingViewContoller = self.sourceViewController;
    RootViewController *container = (RootViewController *) mainViewController.parentViewController;
    CGRect slidingFrame = slidingViewContoller.view.frame;
    
    UIView *slidingView;
    NSLayoutConstraint *slidingConstraint;
    CGFloat mainStart;
    // Prepare constraints
    if (self.slideDirection == SlideLeft)
    {
        slidingView = container.leftSlidingView;
        slidingConstraint = container.leftSlidingConstraint;
        mainStart = slidingFrame.size.width;
    }
    else
    {
        slidingView = container.rightSlidingView;
        slidingConstraint = container.rightSlidingConstraint;
        mainStart = -slidingFrame.size.width;
    }
    
    // Reset constraints
    slidingConstraint.constant = 0;
    container.leftMainConstraint.constant = mainStart;
    [container.view layoutIfNeeded];
   
    // Prepare animation
    slidingConstraint.constant = -slidingFrame.size.width;
    container.leftMainConstraint.constant = 0;
    
    
    [UIView animateWithDuration:AnimationDuration animations:^
     {
         [container.view layoutIfNeeded];
     } completion:^(BOOL finished)
     {
         // Enable interaction on main view
         [mainViewController.view setUserInteractionEnabled:YES];
         
         // Remove sliding view
         [slidingViewContoller.view removeFromSuperview];
         [slidingViewContoller removeFromParentViewController];
         
         // Notify events
         [mainViewController didMoveToParentViewController:container];
         [slidingViewContoller didMoveToParentViewController:nil];
     }];
}

@end
