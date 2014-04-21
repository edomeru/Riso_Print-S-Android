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
- (UITextField *) communityName;
- (UIButton *) mainMenuButton;
- (NSDictionary *) settings;
- (UIAlertView *)errorAlert;

- (void) cardIDDidEndEditing:(NSString *)inputString;
- (void) communityNameDidEndEditing:(NSString *)inputString;
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
    
    [controller viewDidLoad];
    GHAssertNotNil([controller settings], @"");
}

- (void) testCardIDDidEndEditing
{
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
    
    [controller viewDidLoad];
    GHAssertNotNil([controller settings], @"");
    
    NSString *validID = @"ValidID";
    [controller cardId].text = validID;
    [controller cardIDDidEndEditing:validID];
    
    NSString *savedSetting = [[controller settings] objectForKey:@"CardReaderID"];
    GHAssertEqualStrings(savedSetting, validID, @"");
    
     NSString *invalidID = @"invalid ID$";
    [controller cardId].text = invalidID;
    
    [controller cardIDDidEndEditing:invalidID];
    savedSetting = [[controller settings] objectForKey:@"CardReaderID"];
    GHAssertEqualStrings(savedSetting, validID, @"");
    
    GHAssertEqualStrings([controller cardId].text, validID, @"");
    [[controller errorAlert] dismissWithClickedButtonIndex:0 animated:NO];
}

- (void) testCommunityNameDidEndEditing
{
    UIStoryboard *storyboard;
    SettingsViewController *controller;
    
    storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    controller = [storyboard instantiateViewControllerWithIdentifier:@"SettingsViewController"];
    [self performUIViewBindingTest:controller];
    
    [controller viewDidLoad];
    GHAssertNotNil([controller settings], @"");
    
    NSString *validCommunityName= @"ValidName";
    [controller communityName].text = validCommunityName;
    [controller communityNameDidEndEditing:validCommunityName];
    
    NSString *savedSetting = [[controller settings] objectForKey:@"CommunityName"];
    GHAssertEqualStrings(savedSetting, validCommunityName, @"");
    
    NSString *invalidCommunityName= @"invalid Name";
    [controller communityName].text = invalidCommunityName;
    
    [controller communityNameDidEndEditing:invalidCommunityName];
    savedSetting = [[controller settings] objectForKey:@"CommunityName"];
    GHAssertEqualStrings(savedSetting, validCommunityName, @"");
    
    GHAssertEqualStrings([controller communityName].text, validCommunityName, @"");
    [[controller errorAlert] dismissWithClickedButtonIndex:0 animated:NO];
}

- (void)performUIViewBindingTest:(SettingsViewController *)controller
{
    [controller loadView];
    GHAssertNotNil(controller, @"");
    GHAssertNotNil(controller.view, @"");
    GHAssertNotNil([controller cardId], @"");
    GHAssertNotNil([controller communityName], @"");
    GHAssertNotNil([controller mainMenuButton], @"");
}
@end
