//
//  PrintJobHistoryHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PrintDocument;

/**
 * PrintJobHistoryHelper is a helper class that provides methods for the creation of a print job.
 */
@interface PrintJobHistoryHelper : NSObject

/**
 Returns a list of PrintJob objects from the database
 organized into PrintJobHistoryGroups (by Printer) and
 sorted according to timestamp (most recent first).
 @return array of PrintJobHistoryGroups
 */
+ (NSMutableArray*)preparePrintJobHistoryGroups;


/**
 Creates a print job based on the printed document.
 @param printDocument The printed document.
 @param result non-zero value if printing succeeded, 0 otherwise.
 @return YES if successfully created a print job, NO otherwise.
 */
+ (BOOL)createPrintJobFromDocument:(PrintDocument *)printDocument withResult:(NSInteger)result;

@end
