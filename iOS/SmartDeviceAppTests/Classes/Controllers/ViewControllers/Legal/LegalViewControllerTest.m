//
//  LegalViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "LegalViewController.h"

@interface LegalViewController (UnitTest)

// expose private properties
- (UIWebView*)webView;
- (UIButton*)mainMenuButton;

@end

@interface LegalViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    LegalViewController* controller;
}

@end

@implementation LegalViewControllerTest

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
    
    NSString* controllerName = @"LegalViewController";
    controller = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controller, @"unable to instantiate controller (%@)", controllerName);
    GHAssertNotNil(controller.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controller = nil;
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
    
    GHAssertNotNil([controller webView], @"");
    GHAssertNotNil([controller mainMenuButton], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    GHTestLog(@"-- check main menu button");
    UIButton* mainMenuButton = [controller mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controller
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

@end
