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
    kSettingsInputError result = [SettingsValidationHelper validateCardIDInput:testString];
    GHAssertTrue((result == kSettingsInputErrorNone) , nil);
}

- (void)test002_validateCardIDInput_InvalidChars
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

- (void)test003_errorMessages
{
    NSArray *errorMessages = [NSArray arrayWithObjects: @"",
                                                        @"Card ID should be alphanumeric only",
                                                        nil];
    
    for(int i = 0; i < errorMessages.count; i++)
    {
    
        NSString *expectedMsg = (NSString *)[errorMessages objectAtIndex:i];
        NSString *actualMsg = [SettingsValidationHelper errorMessageForSettingsInputError:i];
        NSString *desc = [NSString stringWithFormat:@"Error message for kSettingsInputError[%d] = %@\n Expected:%@", i, actualMsg, expectedMsg];
        GHAssertTrue([expectedMsg isEqualToString:actualMsg], desc);
    }
}
@end
