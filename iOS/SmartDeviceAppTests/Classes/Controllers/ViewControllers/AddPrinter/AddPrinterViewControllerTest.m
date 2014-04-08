//
//  AddPrinterViewControllerTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/8/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AddPrinterViewController.h"

@interface AddPrinterViewController (UnitTest)

// expose private properties
- (UITextField*)textIP;
- (UITextField*)textUsername;
- (UITextField*)textPassword;
- (UIButton*)saveButton;
- (UIActivityIndicatorView*)progressIndicator;

@end

@interface AddPrinterViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    AddPrinterViewController* controller;
}

@end

@implementation AddPrinterViewControllerTest

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
    
    NSString* controllerName = @"AddPrinterViewController";
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
    
    GHAssertNotNil([controller textIP], @"");
    GHAssertNotNil([controller textUsername], @"");
    GHAssertNotNil([controller textPassword], @"");
    GHAssertNotNil([controller saveButton], @"");
    GHAssertNotNil([controller progressIndicator], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    UIButton* saveButton = [controller saveButton];
    NSArray* ibActions = [saveButton actionsForTarget:controller
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"onSave:"], @"");
}

- (void)test003_TextFieldInput
{
    GHTestLog(@"# CHECK: IP TextField does not accept spaces. #");
    
    GHTestLog(@"-- entering a space");
    BOOL willAcceptSpace = [controller textField:[controller textIP]
                   shouldChangeCharactersInRange:NSMakeRange(0, 1)
                               replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");
}

@end
