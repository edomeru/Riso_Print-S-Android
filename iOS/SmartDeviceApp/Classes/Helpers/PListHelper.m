//
//  PListHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PListHelper.h"

#define SDA_PROP_LIST       @"SmartDeviceApp-Settings"
#define PROP_LIST_TYPE      @"plist"

#define KEY_MAX_PRINTERS    @"Printer_MaxCount"
#define KEY_MAX_PRINT_JOBS  @"PrintJob_MaxCountPerPrinter"
#define KEY_MAX_JOB_NO      @"PrintJob_MaxNum"
#define KEY_NEXT_JOB_NO     @"PrintJob_Num"

static NSDictionary* sharedSettingsDict = nil;

@interface PListHelper ()

/**
 * Sets the shared NSDictionary for the SmartDeviceApp Settings property list.
 * This method should be called only once during the lifecycle of this class.
 */
+ (void)setSharedSettingsDict;

/**
 * Gets the file path of the SmartDeviceApp Settings property list.
 * This method is called when reading from and writing to the file.
*/
+ (NSString*)path:(BOOL)shouldBeLocal;

@end

@implementation PListHelper

#pragma mark - Initializer

+ (void)setSharedSettingsDict
{
    if (sharedSettingsDict == nil)
    {
        sharedSettingsDict = [[NSMutableDictionary alloc] initWithContentsOfFile:[self path: NO]];
        if (sharedSettingsDict == nil)
        {
#if DEBUG_LOG_PLIST_HELPER
            NSLog(@"[ERROR][PListUtils] plist file error or invalid contents for dictionary (%@)", SDA_PROP_LIST);
#endif
        }
    }
}

+ (NSString*)path:(BOOL)shouldBeLocal
{
    NSArray* availablePaths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    NSString* localFilePath = [[[availablePaths objectAtIndex:0] stringByAppendingPathComponent:SDA_PROP_LIST] stringByAppendingPathExtension:PROP_LIST_TYPE];
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:localFilePath];
    if (shouldBeLocal || fileExists)
    {
        return localFilePath;
    }

    NSString* pathToSettingsPlist = [[NSBundle mainBundle] pathForResource:SDA_PROP_LIST ofType:PROP_LIST_TYPE];
    if (pathToSettingsPlist == nil)
    {
#if DEBUG_LOG_PLIST_HELPER
        NSLog(@"[ERROR][PListUtils] plist file (%@) not found", SDA_PROP_LIST);
#endif
        // To prevent possible crashes, set pathToSettingsPlist to an empty string
        pathToSettingsPlist = @"";
    }
    return pathToSettingsPlist;
}

#pragma mark - Readers

+ (NSUInteger)readUint:(kPlistUintVal)type
{
    [self setSharedSettingsDict];
    
    switch (type)
    {
        case kPlistUintValMaxPrinters:
            return [[sharedSettingsDict objectForKey:KEY_MAX_PRINTERS] unsignedIntegerValue];
            
        case kPlistUintValMaxPrintJobsPerPrinter:
            return [[sharedSettingsDict objectForKey:KEY_MAX_PRINT_JOBS] unsignedIntegerValue];
        
        case kPlistUintValMaxJobNum:
            return [[sharedSettingsDict objectForKey:KEY_MAX_JOB_NO] unsignedIntegerValue];

        case kPlistUintValNextJobNum:
            return [[sharedSettingsDict objectForKey:KEY_NEXT_JOB_NO] unsignedIntegerValue];

        default:
            return 0;
    }
}

+ (void)setUint:(NSUInteger)value forType:(kPlistUintVal)type
{
    [self setSharedSettingsDict];
    
    switch (type)
    {
        case kPlistUintValNextJobNum:
            [sharedSettingsDict setValue:[NSNumber numberWithInteger: value] forKey:KEY_NEXT_JOB_NO];
            [sharedSettingsDict writeToFile:[self path: YES] atomically:YES];
            break;

        default:
            /* Do nothing */
            break;
    }
}

@end
