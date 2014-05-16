//
//  PrintSettingsViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintSettingsViewController.h"

@interface PrintSettingsViewController (Test)

- (void)initialize;
@property (weak, nonatomic) IBOutlet UILabel *printSettingsScreenTitle;

@end

@interface PrintSettingsViewControllerTest : GHTestCase

@end

@implementation PrintSettingsViewControllerTest
{
    NSString* storyboardId;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PrintSettingsViewController";
}

- (void)test001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertNotNil(viewController.printSettingsScreenTitle, @"");
}

- (void)test002_UIViewLoading_NoPrinterIndex
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertNotNil(viewController.printSettingsScreenTitle, @"");
    GHAssertEqualStrings(viewController.printSettingsScreenTitle.text, NSLocalizedString(@"IDS_LBL_PRINT_SETTINGS", @"Print Settings") ,@"");
}

- (void)test003_UIViewLoading_WithPrinterIndex
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.printerIndex = [NSNumber numberWithInt:0];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertNotNil(viewController.printSettingsScreenTitle, @"");
    GHAssertEqualStrings(viewController.printSettingsScreenTitle.text, NSLocalizedString(@"IDS_LBL_DEFAULT_PRINT_SETTINGS", @"Default Print Settings") ,@"");
}
@end
