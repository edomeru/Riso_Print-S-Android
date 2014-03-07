//
//  PrinterManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Printer;
@class DefaultPrinter;
@class PrintSetting;

@interface PrinterManager : NSObject

/**
 Creates a Printer object.
 This object is typed as a NSManagedObject and is created as
 a Core Data entity.
 
 @return Printer*
 **/
+ (Printer*)createPrinter;

/**
 Calls the SNMP library to search for the printer in the network.
 If the printer is found and is supported, its info and capabilities
 are stored in the passed Printer object.
 
 @param printer
        reference to the Printer object, will contain the info
        and capabilities if the printer is found
 
 @return YES if printer found, NO otherwise.
 **/
+ (BOOL)searchForPrinter:(Printer**)printer;

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


/**
 Gets the list of Printers from DB
 
 @return NSMutableArray*
 **/
+(NSMutableArray *) getPrinters;

/**
 Creates a PrintSetting object.
 This object is typed as a NSManagedObject and is created as
 a Core Data entity.
 
 @return PrintSetting*
 **/
+ (PrintSetting*)createPrintSetting;

/**
 Gets the DefaultPrinter object from DB
 
 @return NSMutableArray*
 **/
+(DefaultPrinter *) getDefaultPrinter;

/**
 Creates a DefaultPrinter object.
 This object is typed as a NSManagedObject and is created as
 a Core Data entity.
 
 @param printer
        printer object of the default printer to be created
 
 @return DefaultPrinter*
 **/
+(DefaultPrinter*)createDefaultPrinter :(Printer *) printer;



@end
