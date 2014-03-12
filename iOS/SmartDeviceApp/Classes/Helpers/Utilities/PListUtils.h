//
//  PListUtils.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PListUtils : NSObject

/**
 Default Print Settings for New Printers.
 @return NSDictionary*
 */
+ (NSDictionary*)getDefaultPrintSettings;

/**
 Maximum Number of Allowed Printers
 @return NSUInteger
 */
+ (NSUInteger)getMaxPrinters;

/**
 "Use SNMP Common Library"
 @return BOOL
 */
+ (BOOL)useSNMPCommonLib;

/**
 "Use SNMP Unicast Timeout"
 @return BOOL
 */
+ (BOOL)useSNMPUnicastTimeout;

@end
