//
//  PListHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    kPlistUintValMaxPrinters,
    kPlistUintValMaxPrintJobsPerPrinter, // print jobs per printer
    
} kPlistUintVal;

@interface PListHelper : NSObject

/**
 Wrapper for reading an unsigned int value from the property list.
 @param kPlistUintVal key
 @return value for the specified key
 */
+ (NSUInteger)readUint:(kPlistUintVal)type;

@end
