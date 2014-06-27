//
//  InputHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * The InputHelper is a helper class that provides validation for user inputs.
 */
@interface InputHelper : NSObject

#pragma mark - IP Address

/**
 Checks if the input IP is a valid IP address.
 @param inputIP The IP address the user want to validate.
 @return YES if valid, NO otherwise.
 */
+ (BOOL)isIPValid:(NSString**)inputIP;

@end
