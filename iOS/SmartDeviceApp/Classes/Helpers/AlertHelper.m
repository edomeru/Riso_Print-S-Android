//
//  AlertHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AlertHelper.h"

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
            alertMsg = NSLocalizedString(IDS_INFO_MSG_PRINTER_ADD_SUCCESSFUL, @"");
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
            alertMsg = NSLocalizedString(IDS_INFO_MSG_WARNING_CANNOT_FIND_PRINTER, @"");
            break;
            
        case kAlertResultErrPrinterDuplicate:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_CANNOT_ADD_PRINTER, @"");
            break;
            
        case kAlertResultErrPrinterCannotBeAdded:
            alertMsg = @"The printer could not be added.";
            break;
            //TODO: only cause is DB error (registerPrinter: failed)
            
        case kAlertResultErrDelete:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_DELETE_FAILED, @"");
            break;
            
        case kAlertResultFileCannotBeOpened:
            alertMsg = NSLocalizedString(IDS_ERR_MSG_OPEN_FAILED, @"");
            break;
            
        case kAlertResultErrDefault:
        default:
            alertMsg = @"The operation could not be compeleted.";
            break;
            //TODO: replace with localized string or remove if will not be used
    }
    
    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                              cancelButtonTitle:NSLocalizedString(IDS_LBL_OK, @"")];
    [alertView show];
}

+ (void)displayConfirmation:(kAlertConfirmation)confirmation withCancelHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))cancelled withConfirmHandler:(void (^)(CXAlertView*, CXAlertButtonItem*))confirmed;
{
    NSString* alertTitle;
    NSString* alertMsg;
    switch (confirmation)
    {
        case kAlertConfirmationDeleteAllJobs:
            alertTitle = NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS_TITLE, @"");
            alertMsg = NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @"");
            break;
            
        case kAlertConfirmationDeleteJob:
            alertTitle = NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS_TITLE, @"");
            alertMsg = NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @"");
            break;
            
        case kAlertConfirmationDeletePrinter:
            alertTitle = NSLocalizedString(IDS_LBL_PRINTERS, @"");
            alertMsg = NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @"");
            break;
    }
    
    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:alertTitle
                                                        message:alertMsg
                                              cancelButtonTitle:nil];
    
    [alertView addButtonWithTitle:NSLocalizedString(IDS_LBL_CANCEL, @"")
                             type:CXAlertViewButtonTypeDefault
                          handler:cancelled];
    
    [alertView addButtonWithTitle:NSLocalizedString(IDS_LBL_OK, @"")
                             type:CXAlertViewButtonTypeDefault
                          handler:confirmed];
    
    [alertView show];
}

@end
