//
//  PrintJobHistoryHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PrintJobHistoryHelper : NSObject

/**
 Returns a list of PrintJob objects from the database
 organized into PrintJobHistoryGroups (by Printer) and
 sorted according to timestamp (most recent first).
 @return array of PrintJobHistoryGroups
 */
+ (NSMutableArray*)preparePrintJobHistoryGroups;

@end
