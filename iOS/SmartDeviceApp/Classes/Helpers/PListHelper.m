//
//  PListHelper.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/10/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PListHelper.h"

#define SDA_PROP_LIST   @"SmartDeviceApp-Settings"

static NSDictionary* sharedSettingsDict = nil;

@interface PListHelper ()

/**
 Sets the shared NSDictionary for the SmartDeviceApp Settings property list.
 This method should be called only once during the lifecycle of this class.
 */
+ (void)setSharedSettingsDict;

@end

@implementation PListHelper

#pragma mark - Initializer

+ (void)setSharedSettingsDict
{
    NSString* pathToSettingsPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:@"plist"];
    if (pathToSettingsPlist == nil)
    {
#if DEBUG_LOG_PLIST_UTILS
        NSLog(@"[ERROR][PListUtils] plist file (%@) not found", SDA_PROP_LIST);
#endif
        //TODO: to prevent possible crashes, set sharedSettingsDict to an empty dictionary?
    }

    sharedSettingsDict = [[NSDictionary alloc] initWithContentsOfFile:pathToSettingsPlist];
    if (sharedSettingsDict == nil)
    {
#if DEBUG_LOG_PLIST_UTILS
        NSLog(@"[ERROR][PListUtils] plist file error or invalid contents for dictionary (%@)", SDA_PROP_LIST);
#endif
        //TODO: to prevent possible crashes, set sharedSettingsDict to an empty dictionary?
    }
}

#pragma mark - Readers

+ (NSDictionary*)readDefaultPrintSettings
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    return [sharedSettingsDict objectForKey:@"PrintSettings_Default"];
}

+ (NSUInteger)readUint:(PL_UINT_TYPE)type
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    switch (type)
    {
        case PL_UINT_MAX_PRINTERS:
            return [[sharedSettingsDict objectForKey:@"Printer_MaxCount"] unsignedIntegerValue];
            
        default:
            return 0;
    }
}

+ (BOOL)readBool:(PL_BOOL_TYPE)type;
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    switch (type)
    {
        case PL_BOOL_USE_SNMP:
            return [[sharedSettingsDict objectForKey:@"Use_SNMPCommonLib"] boolValue];
            
        case PL_BOOL_USE_SNMP_TIMEOUT:
            return [[sharedSettingsDict objectForKey:@"Use_SNMPUnicastTimeout"] boolValue];
            
        default:
            return NO;
    }
}

@end
