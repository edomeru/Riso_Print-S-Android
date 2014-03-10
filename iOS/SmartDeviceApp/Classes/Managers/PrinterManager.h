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

#pragma mark - Printers in DB

/**
 Creates a Printer object.
 It also creates the required PrintSetting object and attaches
 it to this Printer.
 
 @return Printer*
 **/
+ (Printer*)createPrinter;

/**
 Creates a DefaultPrinter object.
 It also attaches an existing Printer object to it.
 
 @param printer
        printer object of the default printer to be created
 
 @return DefaultPrinter*
 **/
+ (DefaultPrinter*)createDefaultPrinter:(Printer*)printer;

/**
 Gets the list of Printers from DB
 
 @return NSMutableArray*
 **/
+ (NSMutableArray*)getPrinters;

/**
 Gets the DefaultPrinter object from DB
 
 @return NSMutableArray*
 **/
+ (DefaultPrinter*)getDefaultPrinter;

#pragma mark - Printers in Network (SNMP)

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

#pragma mark - Printer Utilities

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
