//
//  PrinterSearchViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterSearchViewController.h"
#import "SearchResultCell.h"
#import "PrinterDetails.h"

@interface PrinterSearchViewController (UnitTest)

// expose private properties
- (UITableView*)tableView;
- (UIRefreshControl*)refreshControl;

// expose private methods
- (void)refresh;

@end

@interface SearchResultCell (UnitTest)

// expose private properties
- (UILabel*)printerIP;
- (UILabel*)printerName;
- (UIView*)separator;
- (UIButton*)oldIcon;
- (UIButton*)addIcon;

@end

@interface PrinterSearchViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrinterSearchViewController* controllerIphone;
    PrinterSearchViewController* controllerIpad;
}

@end

@implementation PrinterSearchViewControllerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* msg;
    const float SEARCH_TIMEOUT = 10.0f;
    
    NSString* controllerIphoneName = @"PrinterSearchIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    msg = [NSString stringWithFormat:@"wait for %.2f secs while the iPhone controller is loading", SEARCH_TIMEOUT];
    [self waitForCompletion:SEARCH_TIMEOUT withMessage:msg];
    
    NSString* controllerIpadName = @"PrinterSearchIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
    msg = [NSString stringWithFormat:@"wait for %.2f secs while the iPad controller is loading", SEARCH_TIMEOUT];
    [self waitForCompletion:SEARCH_TIMEOUT withMessage:msg];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
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

- (void)test001_IBOutletsBinding
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIphone tableView], @"");
    GHAssertNotNil([controllerIpad tableView], @"");
}

- (void)test002_UITableViewCell
{
    GHTestLog(@"# CHECK: UITableViewCell Outlets.");
    
    GHTestLog(@"-- get SearchResultCell (iPhone)");
    UITableView* tableViewIphone = [controllerIphone tableView];
    SearchResultCell* cellIphone = [tableViewIphone dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cellIphone, @"");
    GHAssertNotNil([cellIphone printerIP], @"");
    GHAssertNotNil([cellIphone printerName], @"");
    GHAssertNotNil([cellIphone separator], @"");
    GHAssertNotNil([cellIphone oldIcon], @"");
    GHAssertNotNil([cellIphone addIcon], @"");
    
    GHTestLog(@"-- get SearchResultCell (iPad)");
    UITableView* tableViewIpad = [controllerIpad tableView];
    SearchResultCell* cellIPad = [tableViewIpad dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    GHTestLog(@"-- check cell bindings");
    GHAssertNotNil(cellIPad, @"");
    GHAssertNotNil([cellIPad printerIP], @"");
    GHAssertNotNil([cellIPad printerName], @"");
    GHAssertNotNil([cellIPad separator], @"");
    GHAssertNotNil([cellIPad oldIcon], @"");
    GHAssertNotNil([cellIPad addIcon], @"");
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Printer Search Test"
                                                    message:msg
                                                   delegate:self
                                          cancelButtonTitle:@"HIDE"
                                          otherButtonTitles:nil];
    [alert show];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    [alert dismissWithClickedButtonIndex:0 animated:YES];
    
    return done;
}

@end
