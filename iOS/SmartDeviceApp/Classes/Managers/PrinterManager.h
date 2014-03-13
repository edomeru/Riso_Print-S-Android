//
//  PrinterManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PrinterSearchDelegate.h"

@class Printer;
@class DefaultPrinter;
@class PrintSetting;

@interface PrinterManager : NSObject

@property (weak, nonatomic) id<PrinterSearchDelegate> delegate;
@property (strong, nonatomic) NSMutableArray* listSavedPrinters;
@property (strong, nonatomic) DefaultPrinter* defaultPrinter;

#pragma mark - Printers in DB

/**
 Creates a Printer object and saves it to DB.
 It also creates a PrintSetting object using the default print settings
 and attaches it to this Printer.
 
 @return YES if successful, NO otherwise.
 */
- (BOOL)registerPrinter:(PrinterDetails*)printerDetails;

/**
 Creates a DefaultPrinter object and saves it to DB.
 It also attaches the passed Printer object to it.
 
 @param printer
        Printer object to be set as the default printer
 
 @return DefaultPrinter*
 */
- (BOOL)registerDefaultPrinter:(Printer*)printer;

/**
 Gets the list of Printers from the DB.
 */
- (void)getPrinters;

/**
 Gets the DefaultPrinter object from the DB.
 */
- (void)getDefaultPrinter;

/**
 Deletes a Printer object from the DB.
 */
- (void)deletePrinter:(Printer*)printer;

/**
 Deletes the DefaultPrinter object from the DB.
 */
- (void)deleteDefaultPrinter;

#pragma mark - Printers in Network (SNMP)

/**
 Calls the SNMP library to search for the printer in the network,
 which will be found if it is available and supported. This method
 also sets-up the notification observers for the callbacks from the
 SNMP library.
 
 @param printerIP
        the IP address of the printer
 
 @return YES if the printer was found, NO otherwise.
 */
- (void)searchForPrinter:(NSString*)printerIP;

/**
 Calls the SNMP library to search for all available and supported
 printers in the network. This method also sets-up the notification
 observers for the callbacks from the SNMP library.
 */
- (void)searchForAllPrinters;

/**
 Terminates the searching operation.
 Interrupts it if it already ongoing.
 */
- (void)stopSearching;

#pragma mark - Printer Utilities

/**
 Checks if the list of saved printers is already at the
 maximum number of allowed printers.
 
 @return YES if already at maximum, NO otherwise.
 */
- (BOOL)isAtMaximumPrinters;

/**
 Checks if there is a printer on the list of saved printers
 with the same printer IP.
 
 @param printerIP
        the IP address of the printer to be added
 
 @return YES if the specified printer IP is a duplicate, NO otherwise.
 */
- (BOOL)isIPAlreadyRegistered:(NSString*)printerIP;

@end
