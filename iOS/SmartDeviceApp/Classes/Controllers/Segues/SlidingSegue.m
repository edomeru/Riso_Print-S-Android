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
    
    // Reset constraints
    container.leftSlidingConstraint.constant = 0;
    container.leftMainConstraint.constant = 0;
    [container.view layoutIfNeeded];
    
    // Add sliding container
    [container addChildViewController:slidingViewController];
    CGRect slidingFrame = container.slidingView.frame;
    CGRect mainFrame = container.mainView.frame;
    slidingViewController.view.frame = slidingFrame;
    [container.slidingView addSubview:slidingViewController.view];
    if (self.slideDirection == SlideLeft)
    {
        container.leftSlidingConstraint.constant = -slidingFrame.size.width;
    } else
    {
        container.leftSlidingConstraint.constant = mainFrame.size.width;
    }
    [container.view layoutIfNeeded];
   
    // Prepare animation
    if (self.slideDirection == SlideLeft)
    {
        container.leftSlidingConstraint.constant = 0;
        container.leftMainConstraint.constant = slidingFrame.size.width;
    }
    else
    {
        container.leftSlidingConstraint.constant = mainFrame.size.width - slidingFrame.size.width;
        container.leftMainConstraint.constant = -slidingFrame.size.width;
    }
    
    // Start animation
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
    
    // Reset Constraints
    CGRect slidingFrame = slidingViewContoller.view.frame;
    container.leftSlidingConstraint.constant = 0;
    container.leftMainConstraint.constant = slidingFrame.size.width;
    [container.view layoutIfNeeded];
    
    // Prepare animation
    container.leftSlidingConstraint.constant = -slidingFrame.size.width;
    container.leftMainConstraint.constant = 0;
    
    // Start animation
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
