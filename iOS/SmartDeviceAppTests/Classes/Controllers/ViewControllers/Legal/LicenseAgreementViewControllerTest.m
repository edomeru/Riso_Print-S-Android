//
//  LegalViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "LicenseAgreementViewController.h"

@interface LicenseAgreementViewController (UnitTest)

// expose private properties

-(UIWebView *)contentWebView;
-(UIButton *)cancelBtn;
-(UIButton *)okBtn;


@end

@interface LicenseAgreementViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    LicenseAgreementViewController* controller;
}

@end

@implementation LicenseAgreementViewControllerTest

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
    
    NSString* controllerName = @"LicenseAgreementViewController";
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
    
    GHAssertNotNil([controller contentWebView], @"");
    GHAssertNotNil([controller cancelBtn], @"");
    GHAssertNotNil([controller okBtn], @"");
}


- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    GHTestLog(@"-- check main menu button");
    UIButton* okButton = [controller okBtn];
    ibActions = [okButton actionsForTarget:controller
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"okAction:"], @"");
    
    UIButton* cancelButton = [controller okBtn];
    ibActions = [cancelButton actionsForTarget:controller
                           forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count]  == 1, @"");
    GHAssertTrue([ibActions containsObject:@"cancelAction:"], @"");
}

#pragma clang diagnostic ignored "-Wundeclared-selector"
//Suppress undeclared selector warning because setLicenseAgreement is meant to be private but needs to be tested in UT
//http://stackoverflow.com/questions/6224976/how-to-get-rid-of-the-undeclared-selector-warning

- (void)test003_LicenseAgreementState
{
    
    GHTestLog(@"# CHECK: LicenseAgreement state. #");
    
    [controller performSelector:@selector(setLicenseAgreement:) withObject:[NSNumber numberWithBool:NO]];
    GHAssertTrue([LicenseAgreementViewController hasConfirmedToLicenseAgreement]  == NO, @"");
    
    [controller performSelector:@selector(setLicenseAgreement:) withObject:[NSNumber numberWithBool:YES]];
    GHAssertTrue([LicenseAgreementViewController hasConfirmedToLicenseAgreement] , @"");
}

#pragma clang diagnostic pop
@end
