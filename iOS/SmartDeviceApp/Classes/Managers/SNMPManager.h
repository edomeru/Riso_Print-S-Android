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
 If it is supported, its info and capabilities are saved to the
 same Printer object.
 
 @param printer
        Printer object containing the IP Address to access
 
 @return YES if printer was found, NO otherwise.
 **/
+ (BOOL)searchForPrinter:(Printer**)printer;

@end
