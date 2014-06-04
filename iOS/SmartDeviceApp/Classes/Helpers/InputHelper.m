//
//  InputHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "InputHelper.h"
#include <arpa/inet.h>
#import "common.h"

@implementation InputHelper

#pragma mark - IP Address

+ (BOOL)isIPValid:(NSString**)inputIP;
{
    //create a buffer that can hold IPv4 and IPv6
    char formattedIP[64];
    bool isValid = util_validate_ip([*inputIP UTF8String], formattedIP, 64);
    
    if(isValid)
    {
        *inputIP = [*inputIP initWithUTF8String:formattedIP];
    }
    
    return (isValid == 1);
}

@end
