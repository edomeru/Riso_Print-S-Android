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
#if DEBUG_LOG_PLIST_HELPER
        NSLog(@"[ERROR][PListUtils] plist file (%@) not found", SDA_PROP_LIST);
#endif
        //TODO: to prevent possible crashes, set sharedSettingsDict to an empty dictionary?
    }

    sharedSettingsDict = [[NSDictionary alloc] initWithContentsOfFile:pathToSettingsPlist];
    if (sharedSettingsDict == nil)
    {
#if DEBUG_LOG_PLIST_HELPER
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

+ (NSUInteger)readUint:(kPlistUintVal)type
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    switch (type)
    {
        case kPlistUintValMaxPrinters:
            return [[sharedSettingsDict objectForKey:@"Printer_MaxCount"] unsignedIntegerValue];
            
        default:
            return 0;
    }
}

+ (BOOL)readBool:(kPlistBoolVal)type;
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    switch (type)
    {
        case kPlistBoolValUseSNMP:
            return [[sharedSettingsDict objectForKey:@"Use_SNMPCommonLib"] boolValue];
            
        case kPlistBoolValUseSNMPTimeout:
            return [[sharedSettingsDict objectForKey:@"Use_SNMPUnicastTimeout"] boolValue];
            
        case kPlistBoolValUsePrintJobTestData:
            return [[sharedSettingsDict objectForKey:@"Use_PrintJobHistoryTestData"] boolValue];
            
        default:
            return NO;
    }
}

@end
