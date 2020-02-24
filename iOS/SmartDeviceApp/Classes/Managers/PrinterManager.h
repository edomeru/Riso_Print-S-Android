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

/**
 * Classes that use PrinterManager's printer search methods should
 * conform to this protocol to be notified when either a printer is found
 * or when the search has already ended.
 */
@protocol PrinterSearchDelegate <NSObject>

@required

/**
 * Notifies the delegate that the search has ended.
 * @param printerFound YES if a printer was found, NO otherwise
 */
- (void)printerSearchEndedwithResult:(BOOL)printerFound;

/**
 * Notifies the delegate that a new printer has been found.
 * "New" here means that the found printer is not yet in {@link PrinterManager}'s
 * list of saved printers and is not yet in the database.\n
 * @param printerDetails name, IP address, port, and printer capabilities
 */
- (void)printerSearchDidFoundNewPrinter:(PrinterDetails*)printerDetails;

@optional

/**
 * Notifies the delegate that an old printer was found.
 * "Old" here means that the found printer is already in {@link PrinterManager}'s
 * list of saved printers and is already in the database.\n
 * @param printerIP IP address of the printer
 * @param printerName name of the printer
 */
- (void)printerSearchDidFoundOldPrinter:(NSString*)printerIP withName:(NSString*)printerName;

@end

/**
 * Handler for all operations that uses Printer objects.
 * This class provides the following methods:
 *  - adding, deleting, retrieving, and updating Printer objects
 *  - searching for printers from the network
 *  - checking the existence and the number of Printer objects
 *
 * In addition, this class keeps a list of references to all the Printers currently
 * saved in the database, which it automatically updates when add or delete
 * operations are performed.\n It also keeps track of which printer is set
 * as the default printer.\n\n
 * This class is designed to be used as a singleton, which makes its list of saved
 * printers consistent throughout the lifecycle of the application.
 */
@interface PrinterManager : NSObject

/** 
 * If the search methods are used, this delegate cannot be nil.
 */
@property (weak, nonatomic) id<PrinterSearchDelegate> searchDelegate;

/**
 * Returns the current number of Printers saved in the database.
 */
@property (readonly, assign, nonatomic) NSUInteger countSavedPrinters;

#pragma mark - Initialization

/**
 * Returns the singleton PrinterManager object.
 * If the manager does not exist yet, then this method creates it.\n
 * During the creation of PrinterManager, its list of saved printers
 * are also initialized, along with other information such as which
 * is the default printer and the maximum allowed number of Printers.
 *
 * @return the single instance of PrinterManager
 */
+ (PrinterManager*)sharedPrinterManager;

#pragma mark - Printers in DB

/**
 * Creates a Printer object and saves it to the database.
 * It first creates a PrintSetting object using the default print settings,
 * and attaches it to the Printer object.\n The printer's details are then set,
 * and if this is the first Printer, then it is also set as the default printer.\n
 * Finally, the list of saved Printers is updated.\n
 * All changes are saved in the database. If an error occurs, all changes are discarded.
 *
 * @param printerDetails name, IP address, port, and printer capabilities
 * @return YES if successful, NO otherwise
 */
- (BOOL)registerPrinter:(PrinterDetails*)printerDetails;

/**
 * Assigns the specified Printer object to the DefaultPrinter object.
 * If the DefaultPrinter object does not exist yet, it is first created.\n
 * All changes are saved in the database. If an error occurs, all changes are discarded.
 *
 * @param printer the Printer object to be set as the default printer
 * @return YES if successful, NO otherwise
 */
- (BOOL)registerDefaultPrinter:(Printer*)printer;

/**
 * Gets a Printer object from the list of saved printers.
 * This Printer object is a reference to the Printer saved in the database.
 *
 * @param index index from PrinterManager's list of saved printers
 * @return the Printer object or nil if an error occurs
 */
- (Printer*)getPrinterAtIndex:(NSUInteger)index;

/**
 * Removes a Printer object from the list of saved printers.
 * This effectively also removes the Printer saved in the database.\n
 * If the printer to delete is set as the default printer, the first
 * printer on the list is then set as the default printer.\n If the
 * printer to delete is the first printer, then the next one on the
 * list is used.\n If the printer to delete is already the last,
 * printer, then the DefaultPrinter object is simply removed.
 *
 * @param index index from PrinterManager's list of saved printers
 * @return YES if successful, NO otherwise
 */
- (BOOL)deletePrinterAtIndex:(NSUInteger)index;

/**
 * Checks if a default printer has been set.
 *
 * @return YES if a default printer is set, NO otherwise
 */
- (BOOL)hasDefaultPrinter;

/**
 * Checks if the specified Printer is set as the default printer.
 *
 * @param printer the Printer object to check
 * @return YES if the printer is the default printer, NO otherwise
 */
- (BOOL)isDefaultPrinter:(Printer*)printer;

/**
 * Returns the Printer object currently set as the default printer.
 *
 * @return the Printer object currently set as the default printer
 */
- (Printer*)getDefaultPrinter;

/**
 * Makes all the modifications done to all Printer objects permanent.
 * This includes all changes to attributes such as the printer capabilities.
 *
 * @return YES if successful, NO otherwise
 */
- (BOOL)savePrinterChanges;

#pragma mark - Printers in Network (SNMP)

/**
 * Searches the network for a printer with the specified IP address.
 * This method requires that the {@link searchDelegate} property is not
 * nil and that the calling class conforms to the PrinterSearchDelegate
 * protocol. \n
 * This method returns immediately and the result of the search should be
 * handled in the PrinterSearchDelegate methods.
 *
 * @param printerIP the IP address of the printer to search
 */
- (void)searchForPrinter:(NSString*)printerIP;

/**
 * Searches the network for all available printers.
 * This method requires that the {@link searchDelegate} property is not
 * nil and that the calling class conforms to the PrinterSearchDelegate
 * protocol. \n
 * This method returns immediately and the result of the search should be
 * handled in the PrinterSearchDelegate methods.
 */
- (void)searchForAllPrinters;

/**
 * Terminates an ongoing printer search operation.
 * If the search is cancelled, the search delegate methods will not be anymore called.
 * 
 * @param stopSessions indicate if will perform an immediate stop of the current SNMP Sessions
 */
- (void)stopSearching:(BOOL)stopSessions;

#pragma mark - Printer Utilities

/**
 * Checks if the contents of the list of saved printers has
 * already reached the maximum number of allowed printers.
 *
 * @return YES if already at maximum, NO otherwise
 */
- (BOOL)isAtMaximumPrinters;

/**
 * Checks if there is a printer on the list of saved printers
 * that has the same IP address.
 *
 * @param printerIP the IP address to check
 * @return YES if the specified printer IP is a duplicate, NO otherwise
 */
- (BOOL)isIPAlreadyRegistered:(NSString*)printerIP;

/**
 * Checks if the printer is supported (based on printer name).
 *
 * @param printerName the printer name to check
 * @return YES if the printer is supported, NO otherwise
 */
- (BOOL)isPrinterModelValid:(NSString*)printerName;

@end
