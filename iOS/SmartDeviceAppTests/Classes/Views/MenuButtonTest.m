//
//  MenuButtonTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "MenuButton.h"

@interface MenuButtonTest : GHTestCase
{
}

@end

@implementation MenuButtonTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
}

// Run at end of all tests in the class
- (void)tearDownClass
{
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

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_SetSelected
{
    MenuButton* button = [[MenuButton alloc] init];
    GHAssertNotNil(button, @"");
    
    [button setSelected:YES];
    GHAssertEquals(button.backgroundColor, button.selectedColor, @"");
    
    [button setSelected:NO];
    GHAssertEquals(button.backgroundColor, [UIColor clearColor], @"");
}

- (void)test002_SetHighlighted
{
    MenuButton* button = [[MenuButton alloc] init];
    GHAssertNotNil(button, @"");
    
    [button setHighlighted:YES];
    GHAssertEquals(button.backgroundColor, button.highlightColor, @"");
    
    [button setHighlighted:NO];
    GHAssertEquals(button.backgroundColor, [UIColor clearColor], @"");
    
    [button setSelected:YES];
    [button setHighlighted:YES];
}

@end
