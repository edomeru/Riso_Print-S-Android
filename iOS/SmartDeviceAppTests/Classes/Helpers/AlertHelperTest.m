//
//  AlertHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "AlertHelper.h"
#import "CXAlertView.h"

@interface AlertHelperTest : GHTestCase
{
}

@end

@implementation AlertHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    for (UIWindow* window in [[UIApplication sharedApplication] windows])
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                [alert cleanAllPenddingAlert];
                [alert dismiss];
                [self waitForCompletion:2];
            }
        }
    }
}

// Run before each test method
- (void)setUp
{
}

// Run after each test method
- (void)tearDown
{
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_DisplayResult
{
    GHTestLog(@"# CHECK: AlertHelper can display result/info. #");
    
    GHTestLog(@"-- setting different alert messages");
    [self displayAlertWithResult:kAlertResultInfoPrinterAdded];
    [self displayAlertWithResult:kAlertResultErrNoNetwork];
    [self displayAlertWithResult:kAlertResultErrInvalidIP];
    [self displayAlertWithResult:kAlertResultErrMaxPrinters];
    [self displayAlertWithResult:kAlertResultErrPrinterNotFound];
    [self displayAlertWithResult:kAlertResultErrPrinterDuplicate];
    [self displayAlertWithResult:kAlertResultErrPrinterCannotBeAdded];
    [self displayAlertWithResult:kAlertResultErrDefault];
    
    GHTestLog(@"-- setting different alert titles");
    [self displayAlertWithTitle:kAlertTitlePrinters];
    [self displayAlertWithTitle:kAlertTitlePrintersAdd];
    [self displayAlertWithTitle:kAlertTitlePrintersSearch];
    [self displayAlertWithTitle:kAlertTitlePrintJobHistory];
    [self displayAlertWithTitle:kAlertTitleDefault];
}

- (void)test002_DisplayConfirmation
{
    GHTestLog(@"# CHECK: AlertHelper can display confirmation.");
    
    GHTestLog(@"-- setting different confirmations");
    [self displayAlertWithConfirmation:kAlertConfirmationDeleteAllJobs];
}

#pragma mark - Utilities

- (void)displayAlertWithResult:(kAlertResult)result
{
    GHTestLog(@"-- result type=%d", result);
    [AlertHelper displayResult:result withTitle:kAlertTitleDefault withDetails:nil];
    [self checkAndDismissResultAlert];
}

- (void)displayAlertWithTitle:(kAlertTitle)title
{
    GHTestLog(@"-- title type=%d", title);
    [AlertHelper displayResult:kAlertResultErrDefault withTitle:title withDetails:nil];
    [self checkAndDismissResultAlert];
}

- (void)displayAlertWithConfirmation:(kAlertConfirmation)confirmation
{
    GHTestLog(@"-- confirmation type=%d", confirmation);
    
    void (^dismiss)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];
    };
    
    [AlertHelper displayConfirmation:confirmation withCancelHandler:dismiss withConfirmHandler:dismiss];
    [self checkAndDismissConfirmationAlert];
}

- (void)checkAndDismissResultAlert
{
    [self waitForCompletion:1.5];
    
    for (UIWindow* window in [[UIApplication sharedApplication] windows])
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                
                GHAssertNotNil(alert, @"");
                GHAssertNotNil(alert.title, @"");
                GHAssertNotNil(alert.contentView, @"");
                GHAssertTrue(![alert.title isEqualToString:@""], @"");
                GHAssertTrue([alert.buttons count] == 1, @"");
                
                [alert cleanAllPenddingAlert];
                [alert dismiss];
                [self waitForCompletion:1.5];
                alert = nil;
                
                return;
            }
        }
    }
    
    GHFail(@"alert was not displayed");
}

- (void)checkAndDismissConfirmationAlert
{
    [self waitForCompletion:1.5];
    
    for (UIWindow* window in [[UIApplication sharedApplication] windows])
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                
                GHAssertNotNil(alert, @"");
                GHAssertNotNil(alert.title, @"");
                GHAssertTrue(![alert.title isEqualToString:@""], @"");
                
                [alert cleanAllPenddingAlert];
                [alert dismiss];
                [self waitForCompletion:1.5];
                alert = nil;
                
                return;
            }
        }
    }
    
    GHFail(@"alert was not displayed");
}

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    return done;
}

@end
