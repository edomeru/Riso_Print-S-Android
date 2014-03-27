//
//  AppDelegate.m
//  SmartDeviceApp
//
//  Created by Seph on 12/17/13.
//  Copyright (c) 2013 aLink. All rights reserved.
//

#import "AppDelegate.h"
#import "PDFFileManager.h"
#import "RootViewController.h"

#define PREVIEW_DEBUG_MODE 0
#define PDF_END_PROCESSING_NOTIFICATION @"jp.alink-group.smartdeviceapp.endpdfprocessing"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
#if PREVIEW_DEBUG_MODE
    {
        //TODO: REMOVE! For testing only
        NSURL *fileURL = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"sample.pdf" ofType:nil]];
        NSLog(@"testfile url: %@", [fileURL path]);
        [self setUpPreview:fileURL];
        return YES;
    }
#else
    /*check if open-in*/
    if([launchOptions objectForKey: UIApplicationLaunchOptionsURLKey] == nil)
    {
        [self cleanUpPDF]; //Do clean-up if not open-In to clean-up if previous open-in session is not clean-up
    }
    return YES;
 #endif
}

/*If Open-in, this method will be called */
-(BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
#if DEBUG_LOG_PRINT_PREVIEW_SCREEN
    NSLog(@"Open URL:%@", [url path]);
#endif
    [self setUpPreview:url];
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

- (void) setUpPreview:(NSURL *) fileURL
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didEndPDFProcessing:)
                                                 name:PDF_END_PROCESSING_NOTIFICATION
                                               object: nil];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        [self cleanUpPDF];
        [self processPDF:fileURL]; //make async if necessary
    });
}


#pragma mark  - PDF Processing methods
- (void) processPDF:(NSURL *)pdfURL
{
#if DEBUG_LOG_PRINT_PREVIEW_SCREEN
    NSLog(@"Process PDF");
#endif
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];

    int statusCode = [pdfFileManager setUpPDF:pdfURL];
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter]
         postNotificationName:PDF_END_PROCESSING_NOTIFICATION
         object:[NSNumber numberWithInt:statusCode]];
    });
}

- (void) cleanUpPDF
{
#if DEBUG_LOG_PRINT_PREVIEW_SCREEN
    NSLog(@"Cleanup PDF");
#endif
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    [pdfFileManager cleanUp];
}

-(void) didEndPDFProcessing: (NSNotification *) notification
{
#if DEBUG_LOG_PRINT_PREVIEW_SCREEN
    NSLog(@"PDF Processing ended");
#endif
    int statusCode = [(NSNumber *)[notification object] intValue];
    
    if(statusCode == PDF_ERROR_NONE)
    {
        RootViewController *rootController = (RootViewController *)self.window.rootViewController;
        [rootController loadPDFView];
    }
    else
    {
        NSString *error_message = nil;
        switch(statusCode)
        {
            case PDF_ERROR_OPEN:
                error_message = @"Cannot open pdf"; //replace with localizable string
                break;
            case PDF_ERROR_LOCKED:
                error_message = @"Unsupported PDF: Password protected PDF"; //replace with localizable string
                break;
            case PDF_ERROR_PRINTING_NOT_ALLOWED:
                error_message = @"Unsupported PDF: PDF does not allow printing"; //replace with localizable string
                break;
            case PDF_ERROR_PROCESSING_FAILED:
                error_message = @"PDF Processing Failed. "; //replace with localizable string
                break;
        }
        UIAlertView *errorAlert = [[UIAlertView alloc] initWithTitle:@"Error"
                                                             message:error_message
                                                            delegate:nil
                                                   cancelButtonTitle:@"OK"
                                                   otherButtonTitles:nil];
        
        [errorAlert show];
    }
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
