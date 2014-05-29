//
//  SettingsValidationHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "SettingsValidationHelper.h"

#define TEST_DATA_ALL_ENGLISH_ALPHANUMERIC @"1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

@interface SettingsValidationHelperTest : GHTestCase{ }
@end
@implementation SettingsValidationHelperTest
{
    
}

- (void)test001_validateCardIDInput_ValidChars
{
    NSMutableString *testString = [NSMutableString stringWithString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];
    kSettingsInputError result = [SettingsValidationHelper validateLoginIDInput:testString];
    GHAssertTrue((result == kSettingsInputErrorNone) , nil);
}

- (void)test002_validateCardIDInput_InvalidChars
{
    NSString *invalidChars =@" +";//other characters ruled out by keyboard
    for(int i = 0; i < invalidChars.length; i++)
    {
        unichar invalidChar = [invalidChars characterAtIndex:i];
        NSMutableString *testString = [NSMutableString stringWithFormat:@"%@%c", TEST_DATA_ALL_ENGLISH_ALPHANUMERIC, invalidChar];
        kSettingsInputError result = [SettingsValidationHelper validateLoginIDInput:testString];
        GHAssertTrue((result == kSettingsInputErrorInvalidLoginID) , [NSString stringWithFormat:@"Input with invalid char:%c", invalidChar]);
    }
}
@end
