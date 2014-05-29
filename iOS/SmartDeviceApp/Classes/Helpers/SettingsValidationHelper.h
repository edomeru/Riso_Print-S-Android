//
//  SettingsValidationHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

#define LOGIN_ID_MAX_INPUT 31

/**
 An enumeration to identify input error in settings screen
 */
typedef enum
{
    kSettingsInputErrorNone, /**<  No Error*/
    kSettingsInputErrorInvalidLoginID /**<  Invalid Login ID input error*/
} kSettingsInputError;

@interface SettingsValidationHelper : NSObject

/**
 Validates the current string in the Login ID field
 @param inputString The current string in the cardId textfield
 @return kSettingsInputError
 */
+ (kSettingsInputError)validateLoginIDInput:(NSString *)inputString;

@end
