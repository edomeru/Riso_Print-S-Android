//
//  PrinterSearchDelegate.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PrinterDetails;

@protocol PrinterSearchDelegate <NSObject>

@required

/**
 Notifies the delegate that the search has ended.
 */
- (void)searchEnded;

/**
 Notifies the delegate that a new printer has been found.
 Provides the printer info and capabilities as found by the search.
 @param printerDetails
        info and capabilities of the printer
 */
- (void)updateForNewPrinter:(PrinterDetails*)printerDetails;

/**
 Notifies the delegate that an already saved printer was found.
 Provides some details about the printer.
 @param printerIP
        IP address of the printer
 @param printerName
        name of the printer
 */
- (void)updateForOldPrinter:(NSString*)printerIP withName:(NSString*)printerName;

@end
