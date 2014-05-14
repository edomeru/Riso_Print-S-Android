//
//  TestAppDelegate.m
//  SmartDeviceApp
//
//  Created by Seph on 5/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "TestAppDelegate.h"

extern void __gcov_flush();

@implementation TestAppDelegate

- (void)applicationWillTerminate:(UIApplication *)application
{
    __gcov_flush();
    [super applicationWillTerminate:application];
}

@end
