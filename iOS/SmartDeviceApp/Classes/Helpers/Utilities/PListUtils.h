//
//  PListUtils.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    PL_UINT_MAX_PRINTERS,
    
} PL_UINT_TYPE;

typedef enum
{
    PL_BOOL_USE_SNMP,
    PL_BOOL_USE_SNMP_TIMEOUT,
    
} PL_BOOL_TYPE;

@interface PListUtils : NSObject

/**
 Wrapper for reading the default print settings for a new printer.
 @return NSDictionary*
 */
+ (NSDictionary*)readDefaultPrintSettings;

/**
 Wrapper for reading an unsigned int value from the property list.
 @param PL_UINT_TYPE key
 @return value for the specified key
 */
+ (NSUInteger)readUint:(PL_UINT_TYPE)type;

/**
 Wrapper for reading a boolean value from the property list.
 @param PL_BOOL_TYPE key
 @return value for the specified key
 */
+ (BOOL)readBool:(PL_BOOL_TYPE)type;

@end
