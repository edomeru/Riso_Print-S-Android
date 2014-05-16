//
//  AlertHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CXAlertView.h"

typedef enum
{
    kAlertTitlePrinters,
    kAlertTitlePrintersAdd,
    kAlertTitlePrintersSearch,
    kAlertTitlePrintJobHistory,
    
    kAlertTitleDefault
    
} kAlertTitle;

typedef enum
{
    // success messages
    kAlertResultInfoPrinterAdded,
    
    // error with network
    kAlertResultErrNoNetwork,
    
    // error with user input
    kAlertResultErrInvalidIP,
    
    // error when adding printers
    kAlertResultErrMaxPrinters,
    kAlertResultErrPrinterNotFound,
    kAlertResultErrPrinterDuplicate,
    kAlertResultErrPrinterCannotBeAdded,
    
    // error when deleting
    kAlertResultErrDelete,
    
    // error when opening invalid file
    kAlertResultFileCannotBeOpened,
    
    // printing
    kAlertResultPrintFailed,
    kAlertResultPrintSuccessful,
    
    // default error message
    kAlertResultErrDefault
    
} kAlertResult;

typedef enum
{
    kAlertConfirmationDeleteAllJobs,
    kAlertConfirmationDeleteJob,
    
    kAlertConfirmationDeletePrinter
    
} kAlertConfirmation;

@interface AlertHelper : NSObject

/**
 Displays an AlertView informing the user of a result.
 
 @param result
        one of the defined kAlertResult values
        (use kAlertResult* for error messages, kAlertInfo* for success messages)
 @param title
        one of the defined kAlertTitle values
        (use kAlertTitle*)
 @param details
        array of extra information optionally needed when displaying
        the alert (ex. specify the printer IP when adding the printer
        failed)
 */
+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details;

/**
 Displays an AlertView informing the user of a result.
 
 @param result
        one of the defined kAlertResult values
        (use kAlertResult* for error messages, kAlertInfo* for success messages)
 @param title
        one of the defined kAlertTitle values
        (use kAlertTitle*)
 @param details
        array of extra information optionally needed when displaying
        the alert (ex. specify the printer IP when adding the printer
        failed)
 @param handler
        block to handle event when alert view is dismissed
 */
+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details withDismissHandler:(CXAlertViewHandler)handler;

/**
 Displays an AlertView asking for user confirmation.
 The following buttons are available as a response:
 (1) cancel, (2) OK.
 
 @param confirmation
        one of the defined kAlertConfirmation values
        (use kAlertConfirmation*)
 @param cancelled
        block to execute when the user presses cancel
 @param confirmed
        block to execute when the user presses OK
 */
+ (void)displayConfirmation:(kAlertConfirmation)confirmation withCancelHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))cancelled withConfirmHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))confirmed;

@end
