//
//  AlertHelper.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AlertHelper.h"

@interface AlertHelper ()

@end

@implementation AlertHelper

+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details
{
    //TODO: replace messages with localizable strings
    
    // get the title
    NSString* alertTitle;
    switch (title)
    {
        case kAlertTitlePrinters:
            alertTitle = @"Printer Info";
            break;
        
        case kAlertTitlePrintersAdd:
            alertTitle = @"Printer Add Info";
            break;
            
        case kAlertTitlePrintersSearch:
            alertTitle = @"Printer Search Info";
            break;
            
        case kAlertTitlePrintJobHistory:
            alertTitle = @"Print Job History Info";
            break;
        
        case kAlertTitleDefault:
        default:
            alertTitle = @"SmartDeviceApp";
            break;
    }
    
    // get the message
    NSString* alertMsg;
    switch (result)
    {
        case kAlertResultInfoPrinterAdded:
            alertMsg = @"The new printer was added successfully.";
            break;
            
        case kAlertResultErrNoNetwork:
            alertMsg = @"The device is not connected to the network.";
            break;
            
        case kAlertResultErrInvalidIP:
            alertMsg = @"The IP address format is invalid.";
            break;
        
        case kAlertResultErrMaxPrinters:
            alertMsg = @"The number of printers saved is already at maximum.";
            break;
            
        case kAlertResultErrPrinterNotFound:
            alertMsg = @"The printer was not found on the network.";
            break;
            
        case kAlertResultErrPrinterDuplicate:
            alertMsg = @"The printer has already been added.";
            break;
            
        case kAlertResultErrPrinterCannotBeAdded:
            alertMsg = @"The printer could not be added.";
            break;
            //TODO: it would be better to explain why the printer could not be added
            
        case kAlertResultErrDefault:
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
