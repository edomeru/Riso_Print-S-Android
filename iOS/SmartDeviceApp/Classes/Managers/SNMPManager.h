//
//  SNMPManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <CoreData/CoreData.h>
#import "PrinterSearchDelegate.h"

@interface SNMPManager : NSManagedObject

/**
 Searches for the Printer in the network using its IP Address.
 Uses the Manual Search function of the SNMP Common Library.
 Posts a notification when the printer is found and when the search is over.
 
 @param printerIP
        IP address of the printer to search
 */
+ (void)searchForPrinter:(NSString*)printerIP;

/**
 Searches for all available and supported printers in the network.
 Uses the Device Discovery function of the SNMP Common Library.
 Posts a notification when a printer is found and when the search is over.
 */
+ (void)searchForAvailablePrinters;

/**
 Get the Online/Offline status of the Printer
 
 @param printerIP
 IP address of the printer
 
 @return YES if Online; NO if Offline
 */
+ (BOOL)getPrinterStatus:(NSString*)ipAddress;

@end
