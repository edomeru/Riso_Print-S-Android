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
    ALERT_TITLE_PRINTERS,
    ALERT_TITLE_PRINTERS_ADD,
    ALERT_TITLE_PRINTERS_SEARCH,
    
    ALERT_TITLE_DEFAULT
    
} ALERT_TITLE_TYPE;

typedef enum
{
    // success messages
    INFO_PRINTER_ADDED,
    
    // error with network
    ERR_NO_NETWORK,
    
    // error with user input
    ERR_INVALID_IP,
    
    // error when adding printers
    ERR_MAX_PRINTERS,
    ERR_PRINTER_NOT_FOUND,
    ERR_ALREADY_ADDED,
    ERR_CANNOT_ADD,
    
    // default error message
    ERR_DEFAULT
    
} ALERT_RESULT_TYPE;

@interface AlertHelper : NSObject

/**
 Displays an AlertView.
 
 @param result
        one of the defined ALERT_RESULT_TYPE values
        (use ERR_* for error messages, INFO_* for success messages)
 @param title
        one of the defined ALERT_TITLE_TYPE values
        (use ALERT_TITLE_* strings)
 @param details
        array of extra information optionally needed when displaying
        the alert (ex. specify the printer IP when adding the printer
        failed)
 */
+ (void)displayResult:(ALERT_RESULT_TYPE)result withTitle:(ALERT_TITLE_TYPE)title withDetails:(NSArray*)details;

@end
