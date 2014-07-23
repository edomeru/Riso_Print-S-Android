//
//  HomeViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "HomeViewController.h"

@interface HomeViewController (UnitTest)

// expose private properties
- (UIButton*)homeButton;
- (UIButton*)printersButton;
- (UIButton*)printJobHistoryButton;
- (UIButton*)settingsButton;
- (UIButton*)helpButton;
- (UIButton*)legaButton;

// expose private methods
- (BOOL)selectButton:(UIButton*)item;

@end

@interface HomeViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    HomeViewController* controller;
}

@end

@implementation HomeViewControllerTest

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
    
    NSString* controllerName = @"HomeViewController";
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

    GHAssertNotNil([controller homeButton], @"");
    GHAssertNotNil([controller printersButton], @"");
    GHAssertNotNil([controller printJobHistoryButton], @"");
    GHAssertNotNil([controller settingsButton], @"");
    GHAssertNotNil([controller helpButton], @"");
    GHAssertNotNil([controller legaButton], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    GHTestLog(@"-- check home button");
    ibActions = [[controller homeButton] actionsForTarget:controller
                                          forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"homeAction:"], @"");
    
    GHTestLog(@"-- check printers button");
    ibActions = [[controller printersButton] actionsForTarget:controller
                                              forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printersAction:"], @"");
    
    GHTestLog(@"-- check print job history button");
    ibActions = [[controller printJobHistoryButton] actionsForTarget:controller
                                                     forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printJobHistoryAction:"], @"");
    
    GHTestLog(@"-- check settings button");
    ibActions = [[controller settingsButton] actionsForTarget:controller
                                              forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"settingsAction:"], @"");
    
    GHTestLog(@"-- check help button");
    ibActions = [[controller helpButton] actionsForTarget:controller
                                          forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"helpAction:"], @"");
    
    GHTestLog(@"-- check legal button");
    ibActions = [[controller legaButton] actionsForTarget:controller
                                          forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"legalAction:"], @"");
}

- (void)test003_SelectButton
{
    GHTestLog(@"# CHECK: Button Selection. #");
    
    UIButton* item;
    
    GHTestLog(@"-- selecting home button");
    item = [controller homeButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
    
    GHTestLog(@"-- selecting printers button");
    item = [controller printersButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
    
    GHTestLog(@"-- selecting print job history button");
    item = [controller printJobHistoryButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
    
    GHTestLog(@"-- selecting settings button");
    item = [controller settingsButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
    
    GHTestLog(@"-- selecting help button");
    item = [controller helpButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
    
    GHTestLog(@"-- selecting legal button");
    item = [controller legaButton];
    GHAssertTrue([controller selectButton:item], @"");
    GHAssertTrue(item.selected, @"");
}

@end
