//
//  PrinterStatusHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SimplePing.h"

@protocol PrinterStatusHelperDelegate;

/**
 * PrinterStatusHelper is a helper class that provides methods for polling the printer's status and informs the delegate if there is a change in the status.
 */
@interface PrinterStatusHelper : NSObject <SimplePingDelegate>

/**
 * Reference to the ViewController that contains the list of printers.
 */
@property (weak, nonatomic) id <PrinterStatusHelperDelegate> delegate;

/**
 * IP address of the printer.
 */
@property (strong, nonatomic) NSString *ipAddress;

/**
 * Initialize class with printer's IP address.
 * @param ipAddress The IP address of the printer.
 */
- (id)initWithPrinterIP:(NSString *)ipAddress;

/**
 * Stops the polling of printer status.
 */
- (void)stopPrinterStatusPolling;

/**
 * Starts the polling of printer status.
 */
- (void)startPrinterStatusPolling;

/**
 * Check if polling for printer status.
 * @return YES if polling for printer status, NO otherwise.
 */
- (BOOL)isPolling;

@end

/**
 * PrinterStatusHelperDelegate protocol declares method for delegates to know that the printer status has changed.
 */
@protocol PrinterStatusHelperDelegate

/**
 * Notifies the delegate that the printer status changed.
 * @param isOnline Indicates the status of the printer.
 */
- (void)printerStatusHelper:(PrinterStatusHelper *)statusHelper statusDidChange:(BOOL)isOnline;

@end
