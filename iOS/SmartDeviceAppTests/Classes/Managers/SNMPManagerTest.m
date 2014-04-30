//
//  SNMPManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "SNMPManager.h"

@interface SNMPManagerTest : GHTestCase

@end

@implementation SNMPManagerTest

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

- (void)test001_SearchForPrinter
{
    GHTestLog(@"# CHECK: SNMPM can initiate Manual Search. #");
}

- (void)test002_SearchForAvailablePrinters
{
    GHTestLog(@"# CHECK: SNMPM can initiate Device Discovery. #");
}

@end
