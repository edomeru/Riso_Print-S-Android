//
//  AddPrinterViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AddPrinterViewController.h"

@interface AddPrinterViewController (UnitTest)

// expose private properties
- (UITextField*)textIP;
- (UIButton*)saveButton;
- (UIActivityIndicatorView*)progressIndicator;

@end

@interface AddPrinterViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    AddPrinterViewController* controllerIphone;
    AddPrinterViewController* controllerIpad;
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
    
    NSString* controllerIphoneName = @"AddPrinterIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    
    [controllerIphone loadView];
    GHAssertNotNil(controllerIphone.view, @"");
    
    NSString* controllerIpadName = @"AddPrinterIphoneViewController";
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
    
    GHAssertNotNil([controllerIphone textIP], @"");
    GHAssertNotNil([controllerIphone saveButton], @"");
    GHAssertNotNil([controllerIphone progressIndicator], @"");
}

- (void)test002_IBOutletsBindingIpad
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIpad textIP], @"");
    GHAssertNotNil([controllerIpad saveButton], @"");
    GHAssertNotNil([controllerIpad progressIndicator], @"");
}

- (void)test003_IBActionsBindingIphone
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    UIButton* saveButton = [controllerIphone saveButton];
    NSArray* ibActions = [saveButton actionsForTarget:controllerIphone
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"onSave:"], @"");
}

- (void)test004_IBActionsBindingIpad
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    UIButton* saveButton = [controllerIpad saveButton];
    NSArray* ibActions = [saveButton actionsForTarget:controllerIpad
                                      forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"onSave:"], @"");
}

- (void)test005_TextFieldInputIphone
{
    GHTestLog(@"# CHECK: IP TextField does not accept spaces. #");
    
    GHTestLog(@"-- entering a space");
    BOOL willAcceptSpace = [controllerIphone textField:[controllerIphone textIP]
                         shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                     replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");
}

- (void)test006_TextFieldInputIpad
{
    GHTestLog(@"# CHECK: IP TextField does not accept spaces. #");
    
    GHTestLog(@"-- entering a space");
    BOOL willAcceptSpace = [controllerIpad textField:[controllerIpad textIP]
                         shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                     replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");
}

@end
