//
//  AlertHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CXAlertView.h"

/**
 * Constants indicating the title of the AlertView.
 */
typedef enum
{
    kAlertTitlePrinters, /**< Title for alerts displayed in Printers Screen. */
    kAlertTitlePrintersAdd, /**< Title for alerts displayed in Add Printer Screen. */
    kAlertTitlePrintersSearch, /**< Title for alerts displayed in Search Printers Screen. */
    kAlertTitlePrintJobHistory, /**< Title for alerts displayed in Print Job History Screen. */
    kAlertTitlePrintPreview, /**< Title for alerts displayed in Print Preview Screen. */
    
    kAlertTitleDefault /**< Sets alert title to RISO Smart Print */
    
} kAlertTitle;

/**
 * Constants indicating the message of the AlertView.
 */
typedef enum
{
    // success messages
    kAlertResultInfoPrinterAdded, /**< Message when successfully added a printer. */
    
    // error with network
    kAlertResultErrNoNetwork, /**< Message when there is no available network. */
    
    // error with user input
    kAlertResultErrInvalidIP, /**< Message when the input for printer IP address is invalid. */
    
    // error when adding printers
    kAlertResultErrMaxPrinters, /**< Message when adding more than 10 printers. */
    kAlertResultErrPrinterNotFound, /**< Message when adding a printer that is not found on the network. */
    kAlertResultErrPrinterDuplicate, /**< Message when adding a duplicate printer. */
    
    // error when opening PDF
    kAlertResultErrFileHasOpenPassword, /**< Message when opening a document that has password. */
    kAlertResultErrFileDoesNotAllowPrinting, /**< Message when opening a document that does not allow printing . */
    kAlertResultFileCannotBeOpened, /**< Message when opening an invalid file. */
    
    // printing
    kAlertResultErrNoPrinterSelected, /**< Message when successfully added a printer. */
    kAlertResultPrintFailed, /**< Message when printing failed. */
    kAlertResultPrintSuccessful, /**< Message when printing succeed. */
    
    // db-related errors
    kAlertResultErrDB /**< Message when a database related error occurs. */
    
} kAlertResult;

/**
 * Constants indicating the types of confirmation messages the AlertView displays.
 */
typedef enum
{
    kAlertConfirmationDeleteAllJobs, /**< Confirmation message when trying to delete all print job history items. */
    kAlertConfirmationDeleteJob, /**< Confirmation message when trying to delete a print job history item. */
    
    kAlertConfirmationDeletePrinter /**< Confirmation message when trying to delete a printer. */
    
} kAlertConfirmation;


/**
 * The AlertHelper is a helper class that provides methods to display an AlertView informing the user of a result or asking for a confirmation.
 */
@interface AlertHelper : NSObject

/**
 * Displays an AlertView informing the user of a result.
 * @param result
 *        one of the defined kAlertResult values
 *        (use kAlertResult* for error messages, kAlertInfo* for success messages)
 * @param title
 *        one of the defined kAlertTitle values
 *        (use kAlertTitle*)
 * @param details
 *        array of extra information optionally needed when displaying
 *        the alert (ex. specify the printer IP when adding the printer
 *        failed)
 */
+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details;

/**
 * Displays an AlertView informing the user of a result.
 * @param result
 *        one of the defined kAlertResult values
 *        (use kAlertResult* for error messages, kAlertInfo* for success messages)
 * @param title
 *        one of the defined kAlertTitle values
 *        (use kAlertTitle*)
 * @param details
 *        array of extra information optionally needed when displaying
 *        the alert (ex. specify the printer IP when adding the printer
 *        failed)
 * @param handler
 *        block to handle event when alert view is dismissed
 */
+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details withDismissHandler:(CXAlertViewHandler)handler;

/**
 * Displays an AlertView asking for user confirmation.
 * The following buttons are available as a response:
 * (1) cancel, (2) OK.
 * @param confirmation
 *        one of the defined kAlertConfirmation values
 *        (use kAlertConfirmation*)
 * @param cancelled
 *        block to execute when the user presses cancel
 * @param confirmed
 *        block to execute when the user presses OK
 */
+ (void)displayConfirmation:(kAlertConfirmation)confirmation withCancelHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))cancelled withConfirmHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))confirmed;

@end
