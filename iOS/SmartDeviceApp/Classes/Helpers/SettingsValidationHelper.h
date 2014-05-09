//
//  SettingsValidationHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#define CARD_ID_MAX_INPUT 31
/**
 An enumeration to identify input error in settings screen
 */
typedef enum
{
    kSettingsInputErrorNone, /**<  No Error*/
    kSettingsInputErrorInvalidCardID /**<  Invalid Card ID input error*/
} kSettingsInputError;

@interface SettingsValidationHelper : NSObject
/**
 Validates the current string in the Card ID field
 @param inputString The current string in the cardId textfield
 @return kSettingsInputError
 */
+ (kSettingsInputError)validateCardIDInput: (NSString *) inputString;

/**
 Returns the error message based on the Settings input error value
 @param error The Settings input error value
 @return error message
 */
+(NSString *)errorMessageForSettingsInputError:(kSettingsInputError) error;
@end
