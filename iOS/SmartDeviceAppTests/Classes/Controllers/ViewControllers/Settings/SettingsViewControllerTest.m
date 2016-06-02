//
//  SettingsViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "SettingsViewController.h"

@interface SettingsViewController (Testing)
- (UITextField *)loginId;
- (UIButton *) mainMenuButton;
- (UIAlertView *)errorAlert;
@end

@interface SettingsViewControllerTest : GHTestCase { }

@end
@implementation SettingsViewControllerTest
- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)test001_UIViewBinding
{
    GHTestLog(@"Testing SettingsViewController.UIViewBinding");
    
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
}

- (void)test002_UIViewLoading
{
    GHTestLog(@"Testing SettingsViewController.UIViewBinding");
    
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
}

- (void)test003_textFieldDidEndEditing
{
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
    
    NSString *validID = @"ValidID";
    [controller loginId].text = validID;
    [controller textFieldDidEndEditing:controller.loginId];
    
    NSString *savedSetting = [[NSUserDefaults standardUserDefaults] objectForKey:@"LoginID"];
    GHAssertEqualStrings(savedSetting, validID, @"");
}

- (void)performUIViewBindingTest:(SettingsViewController *)controller
{
    [controller view];
    GHAssertNotNil(controller, @"");
    GHAssertNotNil(controller.view, @"");
    GHAssertNotNil([controller loginId], @"");
    GHAssertNotNil([controller mainMenuButton], @"");
}
@end
