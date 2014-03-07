//
//  SlideOverSegue.m
//  SmartDeviceApp
//
//  Created by Seph on 3/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SlideOverSegue.h"

@implementation SlideOverSegue

- (id)initWithIdentifier:(NSString *)identifier source:(UIViewController *)source destination:(UIViewController *)destination
{
    self = [super initWithIdentifier:identifier source:source destination:destination];
    if (self)
    {
        _isUnwinding = NO;
    }
    return self;
}

- (void)perform
{
    UIViewController *source = self.sourceViewController;
    UIViewController *destination = self.destinationViewController;
    
    if (self.isUnwinding == NO)
    {
        [source addChildViewController:destination];
        CGRect viewFrame = source.view.frame;
        viewFrame.origin.x = viewFrame.size.width;
        destination.view.frame = viewFrame;
        [source.view addSubview:destination.view];
        
        viewFrame.origin.x = 0;
        [UIView animateWithDuration:0.3f animations:^
         {
             destination.view.frame = viewFrame;
         }completion:^(BOOL finished)
         {
         }];
    }
    else
    {
        CGRect viewFrame = source.view.frame;
        viewFrame.origin.x = viewFrame.size.width;
        [UIView animateWithDuration:0.3f animations:^
         {
             source.view.frame = viewFrame;
         }completion:^(BOOL finished)
         {
             [source.view removeFromSuperview];
             [source removeFromParentViewController];
         }];
    }
}

@end
