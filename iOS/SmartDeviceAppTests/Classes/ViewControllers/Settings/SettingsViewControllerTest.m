//
//  SettingsViewControllerTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/31/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//
#import <GHUnitIOS/GHUnit.h>
#import "SettingsViewController.h"

@interface SettingsViewController (Testing)
- (UITextField *) cardId;
- (UIButton *) mainMenuButton;
- (UIAlertView *)errorAlert;

- (void) cardIDDidEndEditing:(NSString *)inputString;
@end

@interface SettingsViewControllerTest : GHTestCase { }

@end
@implementation SettingsViewControllerTest
- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)testUIViewBinding
{
    GHTestLog(@"Testing SettingsViewController.UIViewBinding");
    
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
}

- (void)testUIViewLoading
{
    GHTestLog(@"Testing SettingsViewController.UIViewBinding");
    
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
}

- (void) testCardIDDidEndEditing
{
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
    
    NSString *validID = @"ValidID";
    [controller cardId].text = validID;
    [controller cardIDDidEndEditing:validID];
    
    NSString *savedSetting = [[NSUserDefaults standardUserDefaults] objectForKey:@"SDA_CardReaderID"];
    GHAssertEqualStrings(savedSetting, validID, @"");
    
     NSString *invalidID = @"invalid ID$";
    [controller cardId].text = invalidID;
    
    [controller cardIDDidEndEditing:invalidID];
    savedSetting = [[NSUserDefaults standardUserDefaults] objectForKey:@"SDA_CardReaderID"];
    GHAssertEqualStrings(savedSetting, validID, @"");
    
    GHAssertEqualStrings([controller cardId].text, validID, @"");
    [[controller errorAlert] dismissWithClickedButtonIndex:0 animated:NO];
}

- (void)performUIViewBindingTest:(SettingsViewController *)controller
{
    [controller loadView];
    GHAssertNotNil(controller, @"");
    GHAssertNotNil(controller.view, @"");
    GHAssertNotNil([controller cardId], @"");
    GHAssertNotNil([controller mainMenuButton], @"");
}
@end