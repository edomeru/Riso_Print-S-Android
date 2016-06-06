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
#import "DirectPrintManager.h"
#import "SNMPManager.h"
#import "AlertHelper.h"

#define PREVIEW_DEBUG_MODE 0
#define PDF_END_PROCESSING_NOTIFICATION @"jp.alink-group.smartdeviceapp.endpdfprocessing"

#define STORYBOARD_NAME @"Main"

@interface AppDelegate()

/**
 * Determines whether or not the the application is launched/activated
 * from background.
 * Used to determine open-in from background.
 */
@property (nonatomic) BOOL isOpenInHandled;
@property (nonatomic) UIBackgroundTaskIdentifier bgTask;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    
    self.isOpenInHandled = NO;
    NSURL *url = [launchOptions objectForKey:UIApplicationLaunchOptionsURLKey];
    if (url == nil)
    {
        [[PDFFileManager sharedManager] setFileAvailableForLoad:NO];
        [[PDFFileManager sharedManager] setFileURL:nil];
    }
    else
    {
#if PREVIEW_DEBUG_MODE
        NSLog(@"Open-In process in didFinishLaunchingWithOptions");
#endif
        [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
        [[PDFFileManager sharedManager] setFileURL:url];
        self.isOpenInHandled = YES;
    }
    return YES;
}

/*If Open-in, this method will be called */
-(BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    /*IOS-9-001 Support Open-in from Slide Over panel
      Solution for bug where in openURL is called again when performing open-in from slide over panel
      Bug reference: http://www.openradar.me/22896662
      During open-in the file to be opened is placed by the system in the App inbox and the url received is the path of the file in the App inbox. The file is then moved to the App Documents directory
      In second call of openURL the file does not exist in the App inbox anymore since it is already moved in Documents in the first call.
      To catch this, check if file does not exist in inbox and if url is the same as the url processed in the first call
      Do not process if meets condition to be able retain the current file in the app*/
    if(![[NSFileManager defaultManager] fileExistsAtPath:[url path]] && [[[[PDFFileManager sharedManager] fileURL] path] isEqual:[url path]])
    {
#if PREVIEW_DEBUG_MODE
        NSLog(@"Open-In openURL: url does not exist but is same as previous url");
#endif
        return YES;
    }
    
    /*IOS-9-001 Support Open-in from Slide Over panel
      To handle open-in fronm Slide-Over panel (available only in iPad models with iOS9), we must process file even if it's not from background since Slide-Over does not put the app to the background, but only to inactive state
       Instead check if the open-in is already previously handled by other part of the app such as from the launch (didFinishLaunchingWithOptions)*/
    if (!self.isOpenInHandled)
    {
#if PREVIEW_DEBUG_MODE
        NSLog(@"Open-In process in openURL");
#endif
        [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
        [[PDFFileManager sharedManager] setFileURL:url];
        
        // Reset view controllers when loading a new PDF
        UIStoryboard *mainStoryBoard = [UIStoryboard storyboardWithName:STORYBOARD_NAME bundle:[NSBundle mainBundle]];
        self.window.rootViewController = [mainStoryBoard instantiateInitialViewController];
        self.isOpenInHandled = YES;
    }
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    
    /*IOS-9-001 Support Open-in from Slide Over panel. Reset the is open-in handled flag when the app will become inactive because when app is inactive, there is a posibility that it will be activated via open-in*/
    self.isOpenInHandled = NO;
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    if ([DirectPrintManager idle])
    {
        return;
    }
    
    self.bgTask = [application beginBackgroundTaskWithExpirationHandler:^{
        [DirectPrintManager cancelAll];
        [self endBgTask];
        [AlertHelper displayResult:kAlertResultPrintFailed withTitle:kAlertTitleDefault withDetails:nil];
    }];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
}

- (void)endBgTask
{
    if (self.bgTask != UIBackgroundTaskInvalid)
    {
        [[UIApplication sharedApplication] endBackgroundTask:self.bgTask];
        self.bgTask = UIBackgroundTaskInvalid;
    }
}

@end
