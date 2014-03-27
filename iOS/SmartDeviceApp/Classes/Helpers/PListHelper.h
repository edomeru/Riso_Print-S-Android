//
//  PListHelper.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    kPlistUintValMaxPrinters,
    
} kPlistUintVal;

typedef enum
{
    kPlistBoolValUseSNMP,
    kPlistBoolValUseSNMPTimeout,
    
} kPlistBoolVal;

@interface PListHelper : NSObject

/**
 Wrapper for reading the default print settings for a new printer.
 @return NSDictionary*
 */
+ (NSDictionary*)readDefaultPrintSettings;

/**
 Wrapper for reading an unsigned int value from the property list.
 @param kPlistUintVal key
 @return value for the specified key
 */
+ (NSUInteger)readUint:(kPlistUintVal)type;

/**
 Wrapper for reading a boolean value from the property list.
 @param kPlistBoolVal key
 @return value for the specified key
 */
+ (BOOL)readBool:(kPlistBoolVal)type;

@end
