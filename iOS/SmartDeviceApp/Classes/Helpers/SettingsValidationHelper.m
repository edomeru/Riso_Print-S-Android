//
//  SettingsValidationHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SettingsValidationHelper.h"

@implementation SettingsValidationHelper

+ (kSettingsInputError)validateLoginIDInput:(NSString *)inputString
{
    NSString *validCharacters = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    NSCharacterSet* validSet = [NSCharacterSet characterSetWithCharactersInString:validCharacters];
    
    if([[inputString stringByTrimmingCharactersInSet:validSet] length] > 0)
    {
        return kSettingsInputErrorInvalidLoginID;
    }
    
    return kSettingsInputErrorNone;
}

@end
