//
//  ReplaceSegue.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "ReplaceSegue.h"
#import "RootViewController.h"

@implementation ReplaceSegue

- (void)perform
{
    RootViewController *source = self.sourceViewController;
    UIViewController *destination = self.destinationViewController;
    //UIViewController *container = source.parentViewController;
    
    // Replace
    [source addChildViewController:destination];
    destination.view.frame = source.mainView.frame;
    [source.mainView addSubview:destination.view];
}

@end
