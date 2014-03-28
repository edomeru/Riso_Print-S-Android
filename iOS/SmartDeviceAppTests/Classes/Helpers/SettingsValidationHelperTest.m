//
//  SettingsValidationHelperTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "SettingsValidationHelper.h"

#define TEST_DATA_ALL_ENGLISH_ALPHANUMERIC @"1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
#define TEST_DATA_SYMBOLS @"!@#$%^&*()_-+={}[]|\\?/>.<,\"':;~`"
@interface SettingsValidationHelperTest : GHTestCase{ }
@end
@implementation SettingsValidationHelperTest
{
    
}
- (void) testShouldAcceptCardIDInput_LessThan128Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];
    BOOL result = [SettingsValidationHelper shouldAcceptCardIDInput:testString];
    GHAssertTrue(result, nil);
}

-(void) testShouldAcceptCardIDInput_MoreThan128Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];//62Chars
    [testString appendString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];//+62Chars
    [testString appendString: [testString substringToIndex:5]]; //+5Chars
    BOOL result = [SettingsValidationHelper shouldAcceptCardIDInput:testString];
    GHAssertFalse(result, nil);
}

-(void) testShouldAcceptCardIDInput_128Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];//62Chars
    [testString appendString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];//+62Chars
    [testString appendString: [testString substringToIndex:4]]; //+4Chars
    BOOL result = [SettingsValidationHelper shouldAcceptCardIDInput:testString];
    GHAssertTrue(result, nil);
}

-(void) testShouldAcceptCardIDInput_0Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:@""];
    BOOL result = [SettingsValidationHelper shouldAcceptCardIDInput:testString];
    GHAssertTrue(result, nil);
}

- (void) testShouldAcceptCommunityNameInput_LessThan15Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:@"abcdefghijklmn"];
    BOOL result = [SettingsValidationHelper shouldAcceptCommunityNameInput:testString];
    GHAssertTrue(result, nil);
}

-(void) testShouldAcceptCommunityNameInput_MoreThan15Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:@"ABCDEFGHIJKLMNOP"];
    BOOL result = [SettingsValidationHelper shouldAcceptCommunityNameInput:testString];
    GHAssertFalse(result, nil);
}

-(void) testShouldAcceptCommunityNameInput_15Chars
{
    NSMutableString *testString = [NSMutableString stringWithString:@"0123456789ABCDE"];
    BOOL result = [SettingsValidationHelper shouldAcceptCommunityNameInput:testString];
    GHAssertTrue(result, nil);
}

-(void) testValidateCardIDInput_ValidChars
{
    NSMutableString *testString = [NSMutableString stringWithString:TEST_DATA_ALL_ENGLISH_ALPHANUMERIC];
    kSettingsInputError result = [SettingsValidationHelper validateCardIDInput:testString];
    GHAssertTrue((result == kSettingsInputErrorNone) , nil);
}

-(void) testValidateCardIDInput_InvalidChars
{
    
    NSString *invalidChars =@" +";//other characters ruled out by keyboard
    for(int i = 0; i < invalidChars.length; i++)
    {
        unichar invalidChar = [invalidChars characterAtIndex:i];
        NSMutableString *testString = [NSMutableString stringWithFormat:@"%@%c", TEST_DATA_ALL_ENGLISH_ALPHANUMERIC, invalidChar];
        kSettingsInputError result = [SettingsValidationHelper validateCardIDInput:testString];
        GHAssertTrue((result == kSettingsInputErrorInvalidCardID) , [NSString stringWithFormat:@"Input with invalid char:%c", invalidChar]);
    }
}

-(void) testValidateCommunityNameInput_ValidChars
{
    NSMutableString *testString = [NSMutableString stringWithFormat:@"%@%@",TEST_DATA_ALL_ENGLISH_ALPHANUMERIC, TEST_DATA_SYMBOLS];
    NSString *testDataString =[[testString stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@" \\'\"#"]] mutableCopy];
    kSettingsInputError result = [SettingsValidationHelper validateCommunityNameInput:testDataString];
    GHAssertTrue((result == kSettingsInputErrorNone) , [NSString stringWithFormat:@"Test data: %@", testDataString]);
}

-(void) testValidateCommunityNameInput_InvalidChars
{
    NSString *invalidChars =@" \\'\"#";
    for(int i = 0; i < invalidChars.length; i++)
    {
        unichar invalidChar = [invalidChars characterAtIndex:i];
        NSMutableString *testString = [NSMutableString stringWithFormat:@"%@%c", TEST_DATA_ALL_ENGLISH_ALPHANUMERIC, invalidChar];
        kSettingsInputError result = [SettingsValidationHelper validateCommunityNameInput:testString];
        GHAssertTrue((result == kSettingsInputErrorCommunityNameInvalidChars) , [NSString stringWithFormat:@"Input with invalid char:%c", invalidChar]);
    }
}
@end
