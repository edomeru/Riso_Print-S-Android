//
//  util.c
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#include <stdio.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "common.h"

int util_validate_ip(const char *input_ip, char *formatted_ip, size_t max_len)
{
    // Check if valid IPv4 address
    struct in_addr ipv4;
    int result = inet_pton(AF_INET, input_ip, &ipv4);
    if (result != 1)
    {
        // Check if valid IPv6 address
        struct in6_addr ipv6;
        result = inet_pton(AF_INET6, input_ip, &ipv6);
        if (result == 1)
        {
            const char *ip_string = inet_ntop(AF_INET6, &ipv6, formatted_ip, (socklen_t)max_len);
            if (ip_string == 0)
            {
                strcpy(formatted_ip, "");
            }
        }
    }
    else
    {
        const char * ip_string = inet_ntop(AF_INET, &ipv4, formatted_ip, (socklen_t)max_len);
        if (ip_string == 0)
        {
            strcpy(formatted_ip, "");
        }
    }
    
    return (result == 1);
}