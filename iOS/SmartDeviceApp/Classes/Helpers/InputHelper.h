//
//  InputHelper.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/21/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface InputHelper : NSObject

#pragma mark - IP Address

/**
 Removes leading zeroes.
 @param inputIP
        the UITextField contents
 @return the trimmed IP string
 */
+ (NSString*)trimIP:(NSString*)inputIP;

/**
 Checks if the input IP is a valid IP address.
 @return YES if valid, NO otherwise.
 */
+ (BOOL)isIPValid:(NSString*)inputIP;

@end
