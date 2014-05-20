//
//  PrinterManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Printer;
@class PrinterDetails;

@protocol PrinterSearchDelegate <NSObject>

@required

/**
 Notifies the delegate that the search has ended.
 @param found
        YES if a printer was found, NO otherwise.
 */
- (void)printerSearchEndedwithResult:(BOOL)printerFound;

/**
 Notifies the delegate that a new printer has been found.
 Provides the printer info and capabilities as found by the search.
 @param printerDetails
        info and capabilities of the printer
 */
- (void)printerSearchDidFoundNewPrinter:(PrinterDetails*)printerDetails;

@optional

/**
 Notifies the delegate that an already saved printer was found.
 Provides some details about the printer.
 @param printerIP
        IP address of the printer
 @param printerName
        name of the printer
 */
- (void)printerSearchDidFoundOldPrinter:(NSString*)printerIP withName:(NSString*)printerName;

@end

@interface PrinterManager : NSObject

/** 
 UIViewController for the SNMP search operations.
 The delegate receives the update that a printer was found or when
 the search has ended.
 */
@property (weak, nonatomic) id<PrinterSearchDelegate> searchDelegate;

/**
 Number of saved printers held by the PrinterManager.
 */
@property (readonly, assign, nonatomic) NSUInteger countSavedPrinters;

#pragma mark - Initialization

/**
 Gets access to the singleton PrinterManager object.
 If the object does not exist yet, then this method creates it.
 */
+ (PrinterManager*)sharedPrinterManager;

#pragma mark - Printers in DB

/**
 Creates a Printer object. It first creates the required PrintSetting object 
 using the default print settings and attaches it to this Printer. All changes
 are then saved to the DB. Finally, the newly registered Printer is also added 
 to this PrinterManager's list of saved printers.
 If a DB error occurs, all changes are discarded.
 
 @return YES if successful, NO otherwise.
 */
- (BOOL)registerPrinter:(PrinterDetails*)printerDetails;

/**
 Assigns the Printer object to the DefaultPrinter object.
 If the DefaultPrinter object does not exist yet, it is
 first created. All changes are then saved to the DB.
 If a DB error occurs, all changes are discarded.
 
 @param printer
        Printer object to be set as the default printer
 
 @return YES if successful, NO otherwise.
 */
- (BOOL)registerDefaultPrinter:(Printer*)printer;

/**
 Gets a Printer object from the list of saved printers.
 @param index
        index from the list of saved printers
 @return Printer* object or nil if the index is not valid
 */
- (Printer*)getPrinterAtIndex:(NSUInteger)index;

/**
 Removes the defaultprinter record
 
 @return YES if successful, NO otherwise.
 */
- (BOOL) deleteDefaultPrinter;

/**
 Removes a Printer object from the DB. If this Printer object
 is set as the default printer, the DefaultPrinter object is 
 first removed. The PrinterManager's reference to both the
 Printer object and the DefaultPrinter objects are also removed.
 All changes are saved to the DB.
 
 @param index
        index from the list of saved printers
 @return YES if successful, NO otherwise.
 */
- (BOOL)deletePrinterAtIndex:(NSUInteger)index;

/**
 Checks if the PrinterManager holds a reference to a default printer.
 @return YES if a default printer is set, NO otherwise.
 */
- (BOOL)hasDefaultPrinter;

/**
 Checks if the specified printer is default printer.
 @return YES if the printer is the default printer, NO otherwise.
 */
- (BOOL)isDefaultPrinter:(Printer*)printer;

/**
 Returns the printer object of the default printer
 
 @return Printer object
 */
-(Printer*) getDefaultPrinter;

/**
 Call to save the changes done to printer objects to database.
 
 @return YES if successful; NO otherwise
 */
-(BOOL) savePrinterChanges;

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
