//
//  PrinterSearchViewControllerTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/8/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterSearchViewController.h"

@interface PrinterSearchViewController (UnitTest)

// expose private properties
- (UITableView*)tableView;

@end

@interface PrinterSearchViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrinterSearchViewController* controller;
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
    
    NSString* controllerName = @"PrinterSearchViewController";
    controller = [storyboard instantiateViewControllerWithIdentifier:controllerName];
    GHAssertNotNil(controller, @"unable to instantiate controller (%@)", controllerName);
    
    [controller loadView];
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
    
    GHAssertNotNil([controller tableView], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
}

@end
