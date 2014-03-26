//
//  AlertUtils.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AlertUtils.h"

@interface AlertUtils ()

@end

@implementation AlertUtils

+ (void)displayResult:(ALERT_RESULT_TYPE)result withTitle:(ALERT_TITLE_TYPE)title withDetails:(NSArray*)details
{
    //TODO: replace messages with localizable strings
    
    // get the title
    NSString* alertTitle;
    switch (title)
    {
        case ALERT_TITLE_PRINTERS:
            alertTitle = @"Printer Info";
            break;
        
        case ALERT_TITLE_PRINTERS_ADD:
            alertTitle = @"Printer Add Info";
            break;
            
        case ALERT_TITLE_PRINTERS_SEARCH:
            alertTitle = @"Printer Search Info";
            break;
        
        case ALERT_TITLE_DEFAULT:
        default:
            alertTitle = @"SmartDeviceApp";
            break;
    }
    
    // get the message
    NSString* alertMsg;
    switch (result)
    {
        case INFO_PRINTER_ADDED:
            alertMsg = @"The new printer was added successfully.";
            break;
            
        case ERR_NO_NETWORK:
            alertMsg = @"The device is not connected to the network.";
            break;
            
        case ERR_INVALID_IP:
            alertMsg = @"The IP address format is invalid.";
            break;
        
        case ERR_MAX_PRINTERS:
            alertMsg = @"The number of printers saved is already at maximum.";
            break;
            
        case ERR_PRINTER_NOT_FOUND:
            alertMsg = @"The printer was not found on the network.";
            break;
            
        case ERR_ALREADY_ADDED:
            alertMsg = @"The printer has already been added.";
            break;
            
        case ERR_CANNOT_ADD:
            alertMsg = @"The printer could not be added.";
            break;
            //TODO: it would be better to explain why the printer could not be added
            
        case ERR_DEFAULT:
        default:
            alertMsg = @"The operation could not be compeleted.";
            break;
    }
    
    //TODO: if using a custom AlertView, implement it here
    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
    [alertView show];
}

@end
