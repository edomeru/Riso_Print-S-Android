//
//  DirectPrintManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol DirectPrintManagerDelegate;

/**
 * Handler for all DirectPrint-related operations.
 * This class provides the interface to the DirectPrint common library.\n
 * It contains the methods for sending the print job to the printer.
 */
@interface DirectPrintManager : NSObject

/**
 * If the print methods are used, this delegate cannot be nil.
 */
@property (nonatomic, weak) id<DirectPrintManagerDelegate> delegate;

/**
 * Checks if the DirectPrintManager is currently sending a print job.
 *
 * @return YES if there is no ongoing print operation, NO otherwise
 */
+ (BOOL)idle;

/**
 * Stops all ongoing print operations started by either {@link printDocumentViaLPR}
 * or {@link printDocumentViaRaw}. This method waits until all the operations
 * in the Direct Print common library have been terminated before returning. \n
 */
+ (void)cancelAll;

/**
 * Sends the print job to the printer via the LPR port.
 * This method uses the Direct Print - LPR function of the Direct Print common library.\n
 * It creates the print job, displays a printing progress popup, then sends the print job
 * to the printer. The printing progress is auto-updated by DirectPrintManager.\n The final
 * result can be retrieved from the {@link documentDidFinishPrinting} delegate method.
 */
- (void)printDocumentViaLPR;

/**
 * Sends the print job to the printer via the Raw port.
 * This method uses the Direct Print - Raw function of the Direct Print common library.\n
 * It creates the print job, displays a printing progress popup, then sends the print job
 * to the printer. The printing progress is auto-updated by DirectPrintManager.\n The final
 * result can be retrieved from the {@link documentDidFinishPrinting} delegate method.
 */
- (void)printDocumentViaRaw;

@end

/**
 * Classes that use DirectPrinterManager's print methods should conform
 * to this protocol to be notified when a printing operation is finished.
 */
@protocol DirectPrintManagerDelegate <NSObject>

@required

/**
 * Notifies the delegate that printing has finished.
 * Note that the printing progress only refers to the transmission
 * of the print job to the printer. It does not indicate whether
 * the printer has actually finished printing the document.
 *
 * @param successful YES if printing was successful, NO otherwise.
 */
- (void)documentDidFinishPrinting:(BOOL)successful;

@end
