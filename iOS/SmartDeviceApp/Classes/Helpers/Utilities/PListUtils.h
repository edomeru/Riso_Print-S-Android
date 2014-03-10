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
 Gets the maximum number of printers defined in the Property List.
 @return NSUInteger
 **/
+ (NSUInteger)getMaxPrinters;

/**
 Gets the list of default print settings defined in the Property List.
 @return NSDictionary*
 **/
+ (NSDictionary*)getDefaultPrintSettings;

@end
