//
//  AlertUtils.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AlertUtils.h"

@implementation AlertUtils

+ (void)displayResult:(RESULT_TYPE)result withTitle:(NSString*)title withDetails:(NSArray*)details
{
    //TODO: replace messages with localizable strings
    
    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:title
                                                        message:nil
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
    
    switch (result)
    {
        case INFO_PRINTER_ADDED:
            [alertView setMessage:@"The new printer was added successfully."];
            break;
            
        case ERR_NO_NETWORK:
            [alertView setMessage:@"The device is not connected to the network."];
            break;
            
        case ERR_INVALID_IP:
            [alertView setMessage:@"The IP address is invalid. The printer could not be found."];
            break;
        
        case ERR_MAX_PRINTERS:
            [alertView setMessage:@"The number of printers saved is already at maximum."];
            break;
            
        case ERR_PRINTER_NOT_FOUND:
            [alertView setMessage:@"The printer was not found on the network."];
            break;
            
        case ERR_ALREADY_ADDED:
            [alertView setMessage:  @"The printer has already been added."];
            break;
            
        case ERR_CANNOT_ADD:
            [alertView setMessage:@"The printer could not be added."];
            break;
            //TODO: it would be better to explain why the printer could not be added
            
        case ERR_DEFAULT:
        default:
            [alertView setMessage:@"The operation could not be compeleted."];
            break;
    }
    
    [alertView show];
}

@end
