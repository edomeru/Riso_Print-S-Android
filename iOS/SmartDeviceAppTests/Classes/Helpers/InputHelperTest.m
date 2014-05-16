//
//  InputHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "InputHelper.h"

@interface InputHelperTest : GHTestCase
{
    NSString* expectedTrimmedIP;
    NSArray* listUntrimmedIP;
    NSArray* listValidIP;
    NSArray* listInvalidIP;
}

@end

@implementation InputHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    expectedTrimmedIP = @"127.0.0.1";
    listUntrimmedIP = @[
                        @"127.0.0.1",
                        @"000127.0.0.1",
                        @"00000000000127.0.0.1",
                        ];
    // testing for input with extra spaces is not needed
    // since the UI prevents the user from entering spaces
    
    listValidIP = @[
                    @"127.0.0.1",
                    @"192.168.1.1",
                    @"192.168.100.104",
                    @"132.254.111.10",
                    @"10.10.1.1",
                    @"26.10.2.10",
                    @"1.1.1.1",
                    @"255.255.255.255",
                    ];
    
    listInvalidIP = @[
                      @"10.10.10",
                      @"10.10",
                      @"10",
                      @"a.a.a.a",
                      @"192.168.b.1",
                      @"192.168.1.c",
                      @"abc.168.1.1",
                      @"192.abc.1.1",
                      @"10.10.10.256",
                      @"192.168.2.999",
                      @"999.0.0.1",
                      @"2222.168.1.1",
                      @"192.1688.1.1",
                      @"192.168.1234.1",
                      ];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    expectedTrimmedIP = nil;
    listUntrimmedIP = nil;
    listValidIP = nil;
    listInvalidIP = nil;
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

- (void)test001_TrimIP
{
    GHTestLog(@"# CHECK: InputHelper can trim leading zeroes. #");
    
    NSString* actualTrimmedIP;
    NSString* untrimmedIP;
    for (NSUInteger i = 0; i < [listUntrimmedIP count]; i++)
    {
        untrimmedIP = [listUntrimmedIP objectAtIndex:i];
        GHTestLog(@"-- trimming [%@]..", untrimmedIP);
        actualTrimmedIP = [InputHelper trimIP:untrimmedIP];
        GHAssertEqualStrings(actualTrimmedIP, expectedTrimmedIP, @"trimmed IP should be @%@", expectedTrimmedIP);
    }
}

- (void)test002_ValidateIP
{
    GHTestLog(@"# CHECK: InputHelper can check if an IP is valid. #");
    
    GHTestLog(@"-- INVALID IPs");
    NSString* invalidIP;
    for (NSUInteger i = 0; i < [listInvalidIP count]; i++)
    {
        invalidIP = [listInvalidIP objectAtIndex:i];
        GHTestLog(@"-- validating [%@]..", invalidIP);
        GHAssertFalse([InputHelper isIPValid:invalidIP], @"IP=%@ should be invalid", invalidIP);
    }

    GHTestLog(@"-- VALID IPs");
    NSString* validIP;
    for (NSUInteger i = 0; i < [listValidIP count]; i++)
    {
        validIP = [listValidIP objectAtIndex:i];
        GHTestLog(@"-- validating [%@]..", validIP);
        GHAssertTrue([InputHelper isIPValid:validIP], @"IP=%@ should be valid", validIP);
    }
}

@end
