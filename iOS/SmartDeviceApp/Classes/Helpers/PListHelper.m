//
//  PListHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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

+ (NSUInteger)readUint:(kPlistUintVal)type
{
    if (sharedSettingsDict == nil)
        [self setSharedSettingsDict];
    
    switch (type)
    {
        case kPlistUintValMaxPrinters:
            return [[sharedSettingsDict objectForKey:@"Printer_MaxCount"] unsignedIntegerValue];
            
        case kPlistUintValMaxPrintJobsPerPrinter:
            return [[sharedSettingsDict objectForKey:@"PrintJob_MaxCountPerPrinter"] unsignedIntegerValue];
            
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
        case kPlistBoolValUsePrintJobTestData:
            return [[sharedSettingsDict objectForKey:@"Use_PrintJobHistoryTestData"] boolValue];
            
        default:
            return NO;
    }
}

@end
