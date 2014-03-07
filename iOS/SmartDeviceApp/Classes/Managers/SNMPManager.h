//
//  SNMPManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <CoreData/CoreData.h>

@class Printer;

@interface SNMPManager : NSManagedObject

/**
 Searches for the Printer in the network using its IP Address.
 If the Printer is accessible/available, its print capabilities
 are also retrieved and checked to determine if it is supported.
 If it is supported, its info and capabilities are stored in 
 an array and returned.
 
 @param printerIP
        IP address of the printer to search
 
 @return NSArray* containing printer info and capabilities, 'nil' otherwise.
 **/
+ (NSArray*)searchForPrinter:(NSString*)printerIP;

/**
 Get the Online/Offline status of the Printer
 
 @param printerIP
 IP address of the printer
 
 @return YES if Online; NO if Offline
 **/+ (BOOL) getPrinterStatus:(NSString *) ipAddress;
@end
