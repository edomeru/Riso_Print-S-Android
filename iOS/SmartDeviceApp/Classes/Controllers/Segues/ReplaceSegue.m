//
//  ReplaceSegue.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "ReplaceSegue.h"
#import "RootViewController.h"
#import "SlidingViewController.h"

@interface ReplaceSegue()

- (void)replaceController:(UIViewController *)source withController:(UIViewController *)destination inContainer:(RootViewController *)container;

@end

@implementation ReplaceSegue

- (void)perform
{
    RootViewController *container = (RootViewController *)[self.sourceViewController parentViewController];
    
    if (container == nil)
    {
        container = self.sourceViewController;
    }
    else
    {
        container = [RootViewController container];
    }
   
    // Unwind if sliding controller is in view
    [container.sideController close];
    
    // Remove old
    [container.mainController.view removeFromSuperview];
    [container.mainController removeFromParentViewController];

    // Add new
    UIViewController *destination = self.destinationViewController;
    [container addChildViewController:destination];
    destination.view.frame = CGRectMake(0, 0, container.mainView.frame.size.width, container.mainView.frame.size.height);
    [container.mainView addSubview:destination.view];
    [destination didMoveToParentViewController:container];
    
    container.mainController = destination;
}

- (void)replaceController:(UIViewController *)source withController:(UIViewController *)destination inContainer:(RootViewController *)container
{
    [container addChildViewController:destination];
    destination.view.frame = source.view.frame;
    [source willMoveToParentViewController:nil];
    
    [container transitionFromViewController:source toViewController:destination duration:0.0f options:UIViewAnimationOptionTransitionNone animations:^
     {
     }completion:^(BOOL finished)
     {
         [source.view removeFromSuperview];
         [source removeFromParentViewController];
         [destination didMoveToParentViewController:container];
     }];
}

@end
