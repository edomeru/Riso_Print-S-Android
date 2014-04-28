//
//  PrintJobHistoryViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryLayout.h"

@interface PrintJobHistoryViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UICollectionView*)groupsView;
- (PrintJobHistoryLayout*)groupsViewLayout;

@end

@interface PrintJobHistoryViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrintJobHistoryViewController* controllerIphone;
    PrintJobHistoryViewController* controllerIpad;
}

@end

@implementation PrintJobHistoryViewControllerTest

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
    
    NSString* controllerIphoneName = @"PrintJobHistoryIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    
    [controllerIphone loadView];
    GHAssertNotNil(controllerIphone.view, @"");
    
    NSString* controllerIpadName = @"PrintJobHistoryIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    
    [controllerIpad loadView];
    GHAssertNotNil(controllerIpad.view, @"");
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

- (void)test001_IBOutletsBindingIphone
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIphone mainMenuButton], @"");
    GHAssertNotNil([controllerIphone groupsView], @"");
    GHAssertNotNil([controllerIphone groupsViewLayout], @"");
}

- (void)test002_IBOutletsBindingIpad
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIpad mainMenuButton], @"");
    GHAssertNotNil([controllerIpad groupsView], @"");
    GHAssertNotNil([controllerIpad groupsViewLayout], @"");
}

- (void)test003_IBActionsBindingIphone
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controllerIphone mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controllerIphone
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

- (void)test004_IBActionsBindingIpad
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controllerIpad mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controllerIpad
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
}

@end
