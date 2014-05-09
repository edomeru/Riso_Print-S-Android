//
//  AppDelegate.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AppDelegate.h"
#import "PDFFileManager.h"
#import "RootViewController.h"

#define PREVIEW_DEBUG_MODE 0
#define PDF_END_PROCESSING_NOTIFICATION @"jp.alink-group.smartdeviceapp.endpdfprocessing"

#define STORYBOARD_NAME @"Main"

@interface AppDelegate()

@property (nonatomic) BOOL isFromBackground;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    self.isFromBackground = NO;
    
    NSURL *url = [launchOptions objectForKey:UIApplicationLaunchOptionsURLKey];
    if (url == nil)
    {
        [[PDFFileManager sharedManager] setFileAvailableForLoad:NO];
        [[PDFFileManager sharedManager] setFileURL:nil];
    }
    else
    {
        [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
        [[PDFFileManager sharedManager] setFileURL:url];
    }
    return YES;
}

/*If Open-in, this method will be called */
-(BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    if (self.isFromBackground)
    {
        [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
        [[PDFFileManager sharedManager] setFileURL:url];
        
        // Reset view controllers when loading a new PDF
        UIStoryboard *mainStoryBoard = [UIStoryboard storyboardWithName:STORYBOARD_NAME bundle:[NSBundle mainBundle]];
        self.window.rootViewController = [mainStoryBoard instantiateInitialViewController];
    }
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    __block UIBackgroundTaskIdentifier bgTask;
    bgTask = [application beginBackgroundTaskWithExpirationHandler:^{
        [application endBackgroundTask:bgTask];
        bgTask = UIBackgroundTaskInvalid;
    }];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        // Allow async task to finish first before deactivating
        [NSThread sleepForTimeInterval:20];
        
        [application endBackgroundTask:bgTask];
        bgTask = UIBackgroundTaskInvalid;
    });

}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    self.isFromBackground = YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
}

@end
