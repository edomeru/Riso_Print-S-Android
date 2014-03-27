//
//  AlertHelper.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    kAlertTitlePrinters,
    kAlertTitlePrintersAdd,
    kAlertTitlePrintersSearch,
    
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
    
    // default error message
    kAlertResultErrDefault
    
} kAlertResult;

@interface AlertHelper : NSObject

/**
 Displays an AlertView.
 
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

@end
