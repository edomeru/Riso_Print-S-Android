//
//  AlertHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AlertHelper.h"
#import "CXAlertView.h"

@interface AlertHelper ()

@end

@implementation AlertHelper

+ (void)displayResult:(kAlertResult)result withTitle:(kAlertTitle)title withDetails:(NSArray*)details
{
    // get the title
    NSString* alertTitle;
    switch (title)
    {
        case kAlertTitlePrinters:
            alertTitle = NSLocalizedString(IDS_LBL_PRINTER_INFO, @"");
            break;
        
        case kAlertTitlePrintersAdd:
            alertTitle = NSLocalizedString(IDS_LBL_ADD_PRINTER, @"");
            break;
            
        case kAlertTitlePrintersSearch:
            alertTitle = NSLocalizedString(IDS_LBL_SEARCH_PRINTERS, @"");
            break;
            
        case kAlertTitlePrintJobHistory:
            alertTitle = NSLocalizedString(IDS_LBL_PRINT_JOB_HISTORY, @"");
            break;
        
        case kAlertTitleDefault:
        default:
            alertTitle = NSLocalizedString(IDS_APP_NAME, @"");
            break;
    }
    
    // get the message
    NSString* alertMsg;
    switch (result)
    {
        case kAlertResultInfoPrinterAdded:
            alertMsg = NSLocalizedString(IDS_LBL_ADD_SUCCESSFUL, @"");
            break;
            
        case kAlertResultErrNoNetwork:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_NETWORK_ERROR, @"");
            break;
            
        case kAlertResultErrInvalidIP:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_INVALID_IP_ADDRESS, @"");
            break;
        
        case kAlertResultErrMaxPrinters:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_MAX_PRINTER_COUNT, @"");
            break;
            
        case kAlertResultErrPrinterNotFound:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_WARNING_CANNOT_FIND_PRINTER, @"");
            break;
            
        case kAlertResultErrPrinterDuplicate:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_CANNOT_ADD_PRINTER, @"");
            break;
            
        case kAlertResultErrPrinterCannotBeAdded:
            alertMsg = @"The printer could not be added.";
            break;
            //TODO: only cause is DB error (registerPrinter: failed)
            
        case kAlertResultFileCannotBeOpened:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_OPEN_FAILED, @"");
            break;
            
        case kAlertResultErrDefault:
        default:
            alertMsg = @"The operation could not be compeleted.";
            break;
            //TODO: replace with localized string or remove if will not be used
    }
    
    //TODO: if using a custom AlertView, implement it here
    /*UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];*/
    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:alertTitle message:alertMsg cancelButtonTitle:NSLocalizedString(IDS_LBL_OK, @"")];
    [alertView show];
}

+ (void)displayConfirmation:(kAlertConfirmation)confirmation forScreen:(id)screen withDetails:(NSArray*)details
{
    //TODO: replace title and message with localized strings
    //TODO: replace NO and YES with localized strings
    
    NSInteger tag = 0;
    
    // get the title, message, and choices
    NSString* alertTitle;
    NSString* alertMsg;
    NSString* cancelButtonTitle;
    NSString* confirmButtonTitle;
    switch (confirmation)
    {
        case kAlertConfirmationDeleteAllJobs:
            tag = [[details objectAtIndex:0] integerValue];
            alertTitle = @"Print Job History Info";
            alertMsg = [NSString stringWithFormat:
                        @"Are you sure you want to delete all print jobs for %@?", [details objectAtIndex:1]];
            cancelButtonTitle = @"NO";
            confirmButtonTitle = @"YES";
            break;
    }
    
    //TODO: if using a custom AlertView, implement it here
    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                                       delegate:screen
                                              cancelButtonTitle:cancelButtonTitle
                                              otherButtonTitles:confirmButtonTitle, nil];
    alertView.tag = tag;
    [alertView show];
}

@end
