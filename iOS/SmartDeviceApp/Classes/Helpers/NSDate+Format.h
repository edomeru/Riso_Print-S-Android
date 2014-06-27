//
//  NSDate+Format.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * Format category creates a formatted string of date based on current locale.
 */
@interface NSDate (Format)

/**
 * Creates formatted NSString dependent on current locale.
 * @return NSString of date.
 */
- (NSString*)formattedString;

@end
