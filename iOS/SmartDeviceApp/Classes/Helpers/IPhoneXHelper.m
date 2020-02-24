//
//  IPhoneXHelper.m
//  RISOSmartPrint
//
//  Created by SDA on 19/10/2017.
//  Copyright © 2017 aLink. All rights reserved.
//

#import "IPhoneXHelper.h"

#import <sys/utsname.h>

@implementation IPhoneXHelper

+ (BOOL)isDeviceIPhoneX {
    
    NSString *machineString = @"";
#if defined(__i386__) || defined(__x86_64__)
    machineString = [NSProcessInfo processInfo].environment[@"SIMULATOR_MODEL_IDENTIFIER"];
#else
    struct utsname systemInfo;
    uname(&systemInfo);
    machineString = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
#endif
    
    return ([machineString isEqualToString:@"iPhone10,3"] || [machineString isEqualToString:@"iPhone10,6"] ||   //iPhone X
            [machineString isEqualToString:@"iPhone11,2"] || [machineString isEqualToString:@"iPhone11,8"] ||   //iPhone XS || iPhone XR
            [machineString isEqualToString:@"iPhone11,4"] || [machineString isEqualToString:@"iPhone11,6"] ||   //iPhone XS Max
            [machineString isEqualToString:@"iPhone12,1"] || [machineString isEqualToString:@"iPhone12,3"] ||   //iPhone 11 || iPhone 11 Pro
            [machineString isEqualToString:@"iPhone12,5"]); // iPhone 11 Pro Max
}

+ (CGFloat)sensorHousingHeight
{
    return 44.0f;   // Safe area layout padding size
}

@end
