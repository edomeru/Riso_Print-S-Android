//
//  PListUtils.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PListUtils.h"

#define SDA_PROP_LIST   @"SmartDeviceApp-Settings"

@implementation PListUtils

+ (NSDictionary*)getDefaultPrintSettings
{
    NSString* pathToPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPlist];
    
    return [dict objectForKey:@"PrintSettings_Default"];
}

+ (NSUInteger)getMaxPrinters
{
    NSString* pathToPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPlist];
    
    return [[dict objectForKey:@"Printer_MaxCount"] unsignedIntegerValue];
}

+ (BOOL)useSNMPCommonLib
{
    NSString* pathToPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPlist];
    
    return [[dict objectForKey:@"Use_SNMPCommonLib"] boolValue];
}

+ (BOOL)useSNMPUnicastTimeout
{
    NSString* pathToPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:@"plist"];
    NSDictionary* dict = [[NSDictionary alloc] initWithContentsOfFile:pathToPlist];
    
    return [[dict objectForKey:@"Use_SNMPUnicastTimeout"] boolValue];
}

@end
