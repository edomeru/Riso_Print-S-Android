//
//  InputHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "InputHelper.h"
#include <arpa/inet.h>

@implementation InputHelper

#pragma mark - IP Address

+ (NSString*)trimIP:(NSString*)inputIP
{
    //leading zeroes are disregarded
    NSString* pattern = @"^0+";
    
    NSError* error = nil;
    NSRegularExpression* regex = [NSRegularExpression regularExpressionWithPattern:pattern
                                                                           options:0
                                                                             error:&error];
    NSMutableString* trimmedIP = [NSMutableString stringWithString:inputIP];
    [regex replaceMatchesInString:trimmedIP
                          options:0
                            range:NSMakeRange(0, [inputIP length])
                     withTemplate:@""];
    
    return trimmedIP;
}

+ (BOOL)isIPValid:(NSString*)inputIP;
{
    const char *cString = [inputIP UTF8String];
    struct in_addr ipv4;
    
    // Check if valid IPv4 address
    int result = inet_pton(AF_INET, cString, &ipv4);
    if (result != 1)
    {
        // Check if valid IPv6 address
        struct in6_addr ipv6;
        result = inet_pton(AF_INET6, cString, &ipv6);
    }
    
    return (result == 1);
}

@end
