//
//  PrintJobHistoryManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PrintJob;

@interface PrintJobHistoryManager : NSObject

/**
 Returns a list of PrintJob objects from the database
 organized into PrintJobHistoryGroups (by Printer) and
 sorted according to timestamp (most recent first).
 @return array of PrintJobHistoryGroups
 */
+ (NSMutableArray*)retrievePrintJobHistoryGroups;

/**
 */
+ (void)deletePrintJob:(PrintJob*)printJob;

@end
