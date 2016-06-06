//
//  DefaultPrinter.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Printer;

/**
 * Default printer is the entity that holds the information about the default printer.
 */
@interface DefaultPrinter : NSManagedObject

/**
 * Reference to the printer that is set as default.
 */
@property (nonatomic, retain) Printer* printer;

@end
