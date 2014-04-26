//
//  PrintersIpadViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintersViewController.h"
#import "PrintersIpadViewController.h"

@interface PrintersIpadViewController (UnitTest)

// expose private properties
- (UIButton*)mainMenuButton;
- (UIButton*)addPrinterButton;
- (UIButton*)printerSearchButton;
- (UICollectionView*)collectionView;

@end

@interface PrintersIpadViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    PrintersIpadViewController* controller;
}

@end

@implementation PrintersIpadViewControllerTest

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
    
    NSString* controllerName = @"PrintersIpadViewController";
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
    
    GHAssertNotNil([controller mainMenuButton], @"");
    GHAssertNotNil([controller addPrinterButton], @"");
    GHAssertNotNil([controller printerSearchButton], @"");
    GHAssertNotNil([controller collectionView], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* mainMenuButton = [controller mainMenuButton];
    ibActions = [mainMenuButton actionsForTarget:controller
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"mainMenuAction:"], @"");
    
    UIButton* addPrinterButton = [controller addPrinterButton];
    ibActions = [addPrinterButton actionsForTarget:controller
                                   forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"addPrinterAction:"], @"");
    
    UIButton* printerSearchButton = [controller printerSearchButton];
    ibActions = [printerSearchButton actionsForTarget:controller
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"printerSearchAction:"], @"");
}

@end
