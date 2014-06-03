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
    char formattedIP[32];
    bool isValid = util_validate_ip([*inputIP UTF8String], formattedIP, 32);
    
    *inputIP = [*inputIP initWithUTF8String:formattedIP];
    
    return isValid;
}

@end
