//
//  SettingsValidationHelper.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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

+ (kSettingsInputError)validateCardIDInput:(NSString *)inputString
{
    NSCharacterSet* validSet = [NSCharacterSet alphanumericCharacterSet];
    
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
