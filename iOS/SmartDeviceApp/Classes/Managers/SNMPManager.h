//
//  SNMPManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <CoreData/CoreData.h>

/**
 * Handler for all SNMP-related operations.
 * This class provides the interface to the SNMP common library. \n
 * It contains the methods for searching for printers on the network
 * as well as the means for canceling any ongoing search.\n\n
 * This class is designed to be used as a singleton.\n
 * It also requires its users to prepare observers for the following notifications:
 *  - NOTIF_SNMP_ADD
 *  - NOTIF_SNMP_END
 */
@interface SNMPManager : NSObject

#pragma mark - Initialization

/**
 * Returns the singleton SNMPManager object.
 * If the manager does not exist yet, then this method creates it.\n
 *
 * @return the single instance of SNMPManager
 */
+ (SNMPManager*)sharedSNMPManager;

#pragma mark - State

/**
 * Checks if the SNMPManager is currently performing a search operation.
 *
 * @return YES if there is no ongoing search, NO otherwise
 */
+ (BOOL)idle;

#pragma mark - Printer Search

/**
 * Starts the network search for a printer using its IP address.
 * This method uses the Manual Search function of the SNMP common library.\n
 * This method returns immediately. The calling class should prepare observers
 * for the following notifications beforehand to handle search end and printer
 * add notifications:
 *  - NOTIF_SNMP_END
 *  - NOTIF_SNMP_ADD
 *
 * @param printerIP IP address of the printer to search
 */
- (void)searchForPrinter:(NSString*)printerIP;

/**
 * Starts network search for all available printers.
 * This method uses the Device Discovery function of the SNMP common library.\n
 * This method returns immediately. The calling class should prepare observers
 * for the following notifications beforehand to handle search end and printer
 * add notifications:
 *  - NOTIF_SNMP_END
 *  - NOTIF_SNMP_ADD
 */
- (void)searchForAvailablePrinters;

/**
 * Stops an ongoing search operation started by either {@link searchForPrinter}
 * or {@link searchForAvailablePrinters}. This method waits until all the operations
 * in the SNMP common library have been terminated before returning. \n
 * Notification observers in the calling class will not anymore receive its notifications.
 */
- (void)cancelSearch;

@end
