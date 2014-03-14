//
//  ErrorNames.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#ifndef SmartDeviceApp_ErrorNames_h
#define SmartDeviceApp_ErrorNames_h

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

#endif
