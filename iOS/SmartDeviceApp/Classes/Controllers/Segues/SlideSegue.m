//
//  SlidingSegue.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SlideSegue.h"
#import "RootViewController.h"
#import "SlidingViewController.h"

const float AnimationDuration = 0.3f;

@interface SlideSegue()

- (void)performSegue;
- (void)performUnwindSegue;

@end

@implementation SlideSegue

- (id)initWithIdentifier:(NSString *)identifier source:(UIViewController *)source destination:(UIViewController *)destination
{
    _isUnwinding = NO;
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
    SlidingViewController *slidingViewController = self.destinationViewController;
    RootViewController *container = (RootViewController *) mainViewController.parentViewController;
   
    UIView *slidingView;
    NSLayoutConstraint *slidingConstraint;
    
    if (slidingViewController.slideDirection == SlideLeft)
    {
        slidingView = container.leftSlidingView;
        slidingConstraint = container.leftSlidingConstraint;
        container.rightSlidingView.userInteractionEnabled = NO;
    }
    else
    {
        slidingView = container.rightSlidingView;
        slidingConstraint = container.rightSlidingConstraint;
        container.leftSlidingView.userInteractionEnabled = NO;
    }
    slidingView.userInteractionEnabled = YES;
    
    // Reset constraints
    slidingConstraint.constant = 0;
    container.leftMainConstraint.constant = 0;
    container.rightMainConstraint.constant = 0;
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
    if (slidingViewController.slideDirection == SlideLeft)
    {
        if (slidingViewController.isFixedSize == YES)
        {
            container.rightMainConstraint.constant = -slidingFrame.size.width;
        }
        container.leftMainConstraint.constant = slidingFrame.size.width;
    }
    else
    {
        if (slidingViewController.isFixedSize == YES)
        {
            container.leftMainConstraint.constant = -slidingFrame.size.width;
        }
        container.rightMainConstraint.constant = slidingFrame.size.width;
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
         
         // Add reference
         container.sideController = slidingViewController;
     }];
}

- (void)performUnwindSegue
{
    UIViewController *mainViewController = self.destinationViewController;
    SlidingViewController *slidingViewContoller = self.sourceViewController;
    RootViewController *container = (RootViewController *) mainViewController.parentViewController;
    CGRect slidingFrame = slidingViewContoller.view.frame;
    
    NSLayoutConstraint *slidingConstraint;
    // Prepare constraints
    if (slidingViewContoller.slideDirection == SlideLeft)
    {
        slidingConstraint = container.leftSlidingConstraint;
    }
    else
    {
        slidingConstraint = container.rightSlidingConstraint;
    }
    
    // Prepare animation
    slidingConstraint.constant = -slidingFrame.size.width;
    if (slidingViewContoller.slideDirection == SlideLeft)
    {
        container.leftMainConstraint.constant = 0;
        container.rightMainConstraint.constant = 0;
    } else
    {
        container.rightMainConstraint.constant = 0;
        container.leftMainConstraint.constant = 0;
    }
    
    [UIView animateWithDuration:AnimationDuration delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^
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
         
         // Remove reference
         container.sideController = nil;
     }];
}

@end
