//
//  AlertUtils.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

#define ALERT_PRINTER           @"Printer Info"
#define ALERT_ADD_PRINTER       @"Add Printer Info"
#define ALERT_PRINTER_SEARCH    @"Printer Search Info"

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
    
} RESULT_TYPE;

@interface AlertUtils : NSObject

/**
 Displays an AlertView.
 
 @param RESULT_TYPE
        one of pre-defined enum values
 @param title
        title for the UIAlertView
 @param details
        extra information needed when displaying the alert
        (ex. specify printer IP when adding printer failed)
 */
+ (void)displayResult:(RESULT_TYPE)result withTitle:(NSString*)title withDetails:(NSArray*)details;

@end
