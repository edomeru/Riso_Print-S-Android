//
//  PrinterManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Printer;

@interface PrinterManager : NSObject

/**
 Creates a Printer object.
 This object is typed as a NSManagedObject and is created as
 a Core Data entity.
 **/
+ (Printer*)createPrinter;

/**
 Adds the Printer to the Database (Core Data).
 
 @return YES if successful, NO otherwise.
 **/
+ (BOOL)addPrinterToDB:(Printer*)printer;

/**
 Checks if a new Printer can be added to a list of Printer objects.
 
 @param printerIP
        IP Address of the new Printer object
 
 @param listSavedPrinters
        list where the new Printer is to be added
 
 @return YES if can add, NO otherwise
 **/
+ (BOOL)canAddPrinter:(NSString*)printerIP toList:(NSArray*)listSavedPrinters;

@end
