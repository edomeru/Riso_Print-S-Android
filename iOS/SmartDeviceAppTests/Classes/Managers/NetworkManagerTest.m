//
//  NetworkManagerTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/8/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "NetworkManager.h"

@interface NetworkManagerTest : GHTestCase
{
}

@end

@implementation NetworkManagerTest

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

- (void)test001_ConnectionToLocalWifi
{
    GHTestLog(@"# CHECK: NM can check local wifi status. #");
    
    GHAssertTrue([NetworkManager isConnectedToLocalWifi], @"must be connected to local wifi");
}

@end
