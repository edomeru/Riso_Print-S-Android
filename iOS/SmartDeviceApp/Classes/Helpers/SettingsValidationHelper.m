//
//  SettingsValidationHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SettingsValidationHelper.h"

@implementation SettingsValidationHelper
+ (BOOL)shouldAcceptCardIDInput: (NSString *)inputString
{
    if(inputString.length > 128)
    {
        return NO;
    }
    
    return YES;
}

+ (BOOL) shouldAcceptCommunityNameInput:(NSString *)inputString
{
    if(inputString.length > 15)
    {
        return NO;
    }
    
    return YES;
}

+ (kSettingsInputError)validateCardIDInput:(NSString *)inputString
{
    NSCharacterSet* validSet = [NSCharacterSet alphanumericCharacterSet];
    
    if([[inputString stringByTrimmingCharactersInSet:validSet] length] > 0)
    {
        return kSettingsInputErrorInvalidCardID;
    }
    
    return kSettingsInputErrorNone;
}

+ (kSettingsInputError)validateCommunityNameInput:(NSString *)inputString
{
    if(inputString.length == 0)
    {
        return kSettingsInputErrorCommunityNameNoLength;
    }
    //Check for prohibitted characters
    NSCharacterSet* invalidSet = [NSCharacterSet characterSetWithCharactersInString:@" \\'\"#"];
    NSRange range = [inputString rangeOfCharacterFromSet:invalidSet];
    
    if(range.length > 0)
    {
        return kSettingsInputErrorCommunityNameInvalidChars;
    }
    
    return kSettingsInputErrorNone;
}

+ (NSString *)errorMessageForSettingsInputError:(kSettingsInputError) error
{
    NSString *message = @"Invalid Input";
    switch(error)
    {
        case kSettingsInputErrorInvalidCardID:
            message = @"Card ID should be alphanumeric only";
            break;
        case kSettingsInputErrorCommunityNameInvalidChars:
            message = @"Community Name should not contain: \\'\"#";
            break;
        case kSettingsInputErrorCommunityNameNoLength:
            message = @"Input required for community name";
            break;
        case kSettingsInputErrorNone:
            message = @"";
        default:
            break;
    }
    return message;
}

@end
