//
//  PrintJob.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Printer;

/**
 * PrintJob is the entity that holds the information for the printed documents.
 * A printer can save a maximum of 100 print jobs.
 */
@interface PrintJob : NSManagedObject

/**
 * The date the print job was created.
 */
@property (nonatomic, retain) NSDate* date;


/**
 * The filename of the printed document.
 */
@property (nonatomic, retain) NSString* name;


/**
 * Flag if printing succeeded or failed.
 */
@property (nonatomic, retain) NSNumber* result;


/**
 * Reference to the printer that is used to print the document.
 */
@property (nonatomic, retain) Printer* printer;

@end
