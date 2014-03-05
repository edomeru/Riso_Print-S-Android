//
//  InputUtils.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "InputUtils.h"

@interface InputUtils ()

@end

@implementation InputUtils

+ (BOOL)validateAndFormatIP:(NSString**)ip
{
    //TODO: check if IP address is valid
    //TODO: format the IP address (save in the same parameter)
    /**
     IP Address Format: xxx.xxx.xxx.xxx
     
     # of Characters: 7-15
     Type of Characters: Digits and Dots
     Numerical Values: 0-255
     
     Format:
     - 4 dot-separated numbers
     - if value is blank, then it is considered as zero
     - leading zeroes are disregarded
     - spaces are automatically trimmed
     - cannot input over 15 characters
     **/
    
    return YES;
}

@end
