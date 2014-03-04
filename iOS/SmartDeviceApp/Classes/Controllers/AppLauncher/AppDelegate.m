//
//  AppDelegate.m
//  SmartDeviceApp
//
//  Created by Seph on 12/17/13.
//  Copyright (c) 2013 aLink. All rights reserved.
//

#import "AppDelegate.h"
#import "PDFFileManager.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    NSLog(@"Did Finish Launching");
    
    [self cleanUpPDF];

    /*check if open-in*/
    if([launchOptions objectForKey: UIApplicationLaunchOptionsURLKey] != nil)
    {
        NSURL *url = nil;
        url = (NSURL *)[launchOptions valueForKey: UIApplicationLaunchOptionsURLKey];
        
        if([url isFileURL] == true)
        {
            NSLog(@"Opened with URL:%@", [url path]);
            _PDFFileAvailable = true;
        
            // register main view controller for notification
            [[NSNotificationCenter defaultCenter] addObserver:self
                                              selector:@selector(didEndPPDFProcessing:)
                                              name:@"jp.alink-group.smartdeviceapp.endpdfprocessing"
                                              object: nil];
        
            [self processPDF:url];// make async if necessary
        }
        
    }
    
    return YES;
}

/*If from background and open-in, this method will be called not the didFinishLaunchingWithOptions*/
-(BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    NSLog(@"open URL:%@", [url path]);
    // register main view controller for notification
    [[NSNotificationCenter defaultCenter] addObserver:self
                                          selector:@selector(didEndPPDFProcessing:)
                                          name:@"jp.alink-group.smartdeviceapp.endpdfprocessing"
                                          object: nil];
    [self cleanUpPDF];
    [self processPDF:url]; //make async if necessary

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
    [self cleanUpPDF];
}


#pragma mark  - PDF Processing methods
- (void) processPDF:(NSURL *)pdfURL
{
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];

    int statusCode = [pdfFileManager setUpPDF:pdfURL];
    
    [[NSNotificationCenter defaultCenter]
        postNotificationName:@"jp.alink-group.smartdeviceapp.endpdfprocessing"
        object:[NSNumber numberWithInt:statusCode]];
}

- (void) cleanUpPDF
{
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    [pdfFileManager cleanUp];
}

-(void) didEndPPDFProcessing: (NSNotification *) _notification
{
    NSLog(@"PDF Processing ended");
    int statusCode = [(NSNumber *)[_notification object] integerValue];
    
    if(statusCode == PDF_ERROR_NONE)
    {
       //call rootview to reload screen
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
