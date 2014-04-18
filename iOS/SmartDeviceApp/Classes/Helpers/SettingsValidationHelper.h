//
//  SettingsValidationHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 An enumeration to identify input error in settings screen
 */
typedef enum
{
    kSettingsInputErrorNone, /**<  No Error*/
    kSettingsInputErrorInvalidCardID, /**<  Invalid Card ID input error*/
    kSettingsInputErrorCommunityNameNoLength, /**<  No Community Name input error */
    kSettingsInputErrorCommunityNameInvalidChars /**<  Invalid characters in Community Name input error*/
} kSettingsInputError;

@interface SettingsValidationHelper : NSObject
/**
 Checks if the keyboard input for the Card ID field should be accepted
 @param inputString The input string with the last keyboard input
 @return YES if accepted; NO otherwise
 */
+ (BOOL)shouldAcceptCardIDInput:(NSString *)inputString;

/**
 Checks if the keyboard input for the Community Name field should be accepted
 @param inputString The input string with the last keyboard input
 @return YES if accepted; NO otherwise
 */
+ (BOOL)shouldAcceptCommunityNameInput:(NSString *)inputString;
/**
 Validates the current string in the Card ID field
 @param inputString The current string in the cardId textfield
 @return kSettingsInputError
 */
+ (kSettingsInputError)validateCardIDInput: (NSString *) inputString;

/**
 Validates the current string in the Community Name field
 @param inputString The current string in the Community Name field
 @return kSettingsInputError
 */
+ (kSettingsInputError)validateCommunityNameInput:(NSString *)inputString;

/**
 Returns the error message based on the Settings input error value
 @param error The Settings input error value
 @return error message
 */
+(NSString *)errorMessageForSettingsInputError:(kSettingsInputError) error;
@end
