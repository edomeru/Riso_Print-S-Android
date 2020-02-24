//
//  PListHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * Constants indicating the properties set in the Plist file.
 */
typedef enum
{
    kPlistUintValMaxPrinters, /**< Maximum number of saved printers. */
    kPlistUintValMaxPrintJobsPerPrinter, /**< Maximum number of saved print jobs per printer. */
    kPlistUintValMaxJobNum, /**<Maximum job number value>*/
    kPlistUintValNextJobNum, /**<The next job number to be assigned to a print job> */
} kPlistUintVal;


/**
 * PListHelper is a helper class that provides methods to read and return the value of a property in the Plist file.
 */
@interface PListHelper : NSObject

/**
 * Wrapper for reading an unsigned int value from the property list.
 * @param kPlistUintVal key
 * @return value for the specified key
 */
+ (NSUInteger)readUint:(kPlistUintVal)type;

/**
* Wrapper for writing an unsigned int value to the property list.
* @param NSUInteger value
* @param kPlistUintVal key
*/
+ (void)setUint:(NSUInteger)value forType:(kPlistUintVal)type;

@end
