//
//  SettingsValidationHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SettingsValidationHelper.h"



@implementation SettingsValidationHelper

+ (kSettingsInputError)validateCardIDInput:(NSString *)inputString
{
    NSString *validCharacters = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    NSCharacterSet* validSet = [NSCharacterSet characterSetWithCharactersInString:validCharacters];
    
    if([[inputString stringByTrimmingCharactersInSet:validSet] length] > 0)
    {
        return kSettingsInputErrorInvalidCardID;
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
        case kSettingsInputErrorNone:
            message = @"";
        default:
            break;
    }
    return message;
}

@end
