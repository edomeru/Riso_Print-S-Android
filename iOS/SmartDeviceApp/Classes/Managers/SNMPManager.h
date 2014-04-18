//
//  SNMPManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <CoreData/CoreData.h>

@interface SNMPManager : NSObject

#pragma mark - Initialization

/**
 Gets access to the singleton SNMPManager object.
 If the object does not exist yet, then this method creates it.
 */
+ (SNMPManager*)sharedSNMPManager;

#pragma mark - Printer Search

/**
 Searches for the Printer in the network using its IP Address.
 Uses the Manual Search function of the SNMP Common Library.
 A notification observer should be prepared beforehand to listen
 for the printer found and search ended notifications.
 @param printerIP
        IP address of the printer to search
 */
- (void)searchForPrinter:(NSString*)printerIP;

/**
 Searches for all available and supported printers in the network.
 Uses the Device Discovery function of the SNMP Common Library.
 A notification observer should be prepared beforehand to listen
 for the printer found and search ended notifications.
 */
- (void)searchForAvailablePrinters;

/**
 Cancels an ongoing searchForPrinter or searchForAvailablePrinters
 operation. If a notification observer listening for printer found
 and/or search ended has been previously setup, they should be then
 be removed after canceling search.
 */
- (void)cancelSearch;

@end
