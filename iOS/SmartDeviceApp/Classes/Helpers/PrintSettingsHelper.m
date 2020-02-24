//
//  PrintSettingsHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsHelper.h"
#import "XMLParser.h"
#import "PreviewSetting.h"
#import "PrintSetting.h"
#import "PrintPreviewHelper.h"

#include "common.h"

@implementation PrintSettingsHelper

+ (NSDictionary *)sharedPrintSettingsTree;
{
    static dispatch_once_t once;
    static NSDictionary *printSettingsTree;
    dispatch_once(&once, ^
    {
        // Load print settings tree
        NSString *printSettingsFilePath = [[NSBundle mainBundle] pathForResource:@"printsettings" ofType:@"xml"];
        NSDictionary *dictionary = [XMLParser dictionaryFromXMLFile:printSettingsFilePath];
        printSettingsTree = [dictionary objectForKey:@"printsettings"];
    });
    return printSettingsTree;
}

// for ORPHIS FW start
/*
+ (NSDictionary *)sharedPrintSettingsTreeFW:(NSString *)printerName
{
    // printerName = @"ORPHIS FW5230"; // debug
    
    
    static dispatch_once_t once;
    static NSDictionary *printSettingsTree;
    dispatch_once(&once, ^
                  {
                      // Load print settings tree
                       
                      NSString *printSettingsFilePath;

                      if ([printerName length] == 0) {
                          printSettingsTree = NULL;
                          printSettingsFilePath = NULL;
                      }
                      
                      if ([self isISSeries:printerName])
                      {
                          printSettingsFilePath = [[NSBundle mainBundle] pathForResource:@"printsettings" ofType:@"xml"];
                      }
                      else if ([printerName isEqualToString:@"ORPHIS FW5230"] ||
                               [printerName isEqualToString:@"ORPHIS FW5230A"] ||
                               [printerName isEqualToString:@"ORPHIS FW5231"] ||
                               [printerName isEqualToString:@"ORPHIS FW2230"] ||
                               [printerName isEqualToString:@"ORPHIS FW1230"] ||
                               [printerName isEqualToString:@"ComColor FW5230"] ||
                               [printerName isEqualToString:@"ComColor FW5230R"] ||
                               [printerName isEqualToString:@"ComColor FW5231"] ||
                               [printerName isEqualToString:@"ComColor FW5231R"] ||
                               [printerName isEqualToString:@"ComColor FW5000"] ||
                               [printerName isEqualToString:@"ComColor FW5000R"] ||
                               [printerName isEqualToString:@"ComColor FW2230"] ||
                               [printerName isEqualToString:@"ComColor black FW1230"] ||
                               [printerName isEqualToString:@"ComColor black FW1230R"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang FW5230"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang FW5230R"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang FW5231"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang FW2230 Wenjianhong"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang FW2230 Lan"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang black FW1230"] ||
                               [printerName isEqualToString:@"Shan Cai Yin Wang black FW1230R"]
                          )
                      {
                          printSettingsFilePath = [[NSBundle mainBundle] pathForResource:@"printsettings_fw" ofType:@"xml"];
                      }
                      else
                      {
                          printSettingsFilePath = [[NSBundle mainBundle] pathForResource:@"printsettings_gd" ofType:@"xml"];
                      }
  
                      NSDictionary *dictionary = [XMLParser dictionaryFromXMLFile:printSettingsFilePath];
                      printSettingsTree = [dictionary objectForKey:@"printsettings"];
                      
                  });
    return printSettingsTree;
}
 */
// for ORPHIS FW end

+ (PreviewSetting *)defaultPreviewSetting
{
    PreviewSetting *defaultPreviewSetting = [[PreviewSetting alloc] init];
   
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            NSString *type = [setting objectForKey:@"type"];
            NSString *defaultValue = [setting objectForKey:@"default"];
            
            if ([defaultPreviewSetting respondsToSelector:NSSelectorFromString(key)] == NO)
            {
                continue;
            }
            
            if ([type isEqualToString:@"list"] || [type isEqualToString:@"numeric"])
            {
                [defaultPreviewSetting setValue:[NSNumber numberWithInteger:[defaultValue integerValue]] forKey:key];
            }
            else
            {
                [defaultPreviewSetting setValue:[NSNumber numberWithBool:[defaultValue boolValue]] forKey:key];
            }
        }
    }
    
    return defaultPreviewSetting;
}

+ (void)copyDefaultPreviewSetting:(PreviewSetting **)previewSetting
{
    if(previewSetting == nil || *previewSetting == nil)
    {
        return;
    }

    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            NSString *type = [setting objectForKey:@"type"];
            NSString *defaultValue = [setting objectForKey:@"default"];

#if GET_ORIENTATION_FROM_PDF_ENABLED
            //the default orientation is the orientation of the first page of the PDF. do not override with value from the xml
            if ([key isEqualToString:KEY_ORIENTATION])
            {
                continue;
            }
#endif
            if ([(*previewSetting) respondsToSelector:NSSelectorFromString(key)] == NO)
            {
                continue;
            }
            
            if ([type isEqualToString:@"list"] || [type isEqualToString:@"numeric"])
            {
                [(*previewSetting) setValue:[NSNumber numberWithInteger:[defaultValue integerValue]] forKey:key];
            }
            else
            {
                [(*previewSetting) setValue:[NSNumber numberWithBool:[defaultValue boolValue]] forKey:key];
            }
        }
    }
}

+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting printerName:(NSString *)name
{
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            NSString *type = [setting objectForKey:@"type"];
            
            /* Get printer-specific defaultValue, if defined */
            NSString *defaultValue = [setting objectForKey:[NSString stringWithFormat:@"default%@",[self getPrinterType:name]]];
            /* If printer-specific defaultValue is nil, use value from "default" */
            if (defaultValue == nil)
            {
                defaultValue = [setting objectForKey:@"default"];
            }
            
            if ([(*printSetting) respondsToSelector:NSSelectorFromString(key)] == NO)
            {
                continue;
            }
            
            if ([type isEqualToString:@"list"] || [type isEqualToString:@"numeric"])
            {
                [(*printSetting) setValue:[NSNumber numberWithInteger:[defaultValue integerValue]] forKey:key];
            }
            else
            {
                [(*printSetting) setValue:[NSNumber numberWithBool:[defaultValue boolValue]] forKey:key];
            }
        }
    }
}

+ (void)copyPrintSettings:(PrintSetting *)printSetting toPreviewSetting:(PreviewSetting **) previewSetting;
{
    if(printSetting == nil || previewSetting == nil || *previewSetting == nil)
    {
        return;
    }
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
#if GET_ORIENTATION_FROM_PDF_ENABLED
            //the default orientation is the orientation of the first page of the PDF. do not override with value print settings
            if ([key isEqualToString:KEY_ORIENTATION])
            {
                continue;
            }
#endif
            [(*previewSetting) setValue:[printSetting valueForKey:key] forKey:key];
        }
    }
}

+ (void)addObserver:(id)observer toPreviewSetting:(PreviewSetting **)previewSetting
{
    if(previewSetting == nil || *previewSetting == nil)
    {
        return;
    }
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            [(*previewSetting) addObserver:observer forKeyPath:key options:NSKeyValueObservingOptionOld|NSKeyValueObservingOptionNew context:PREVIEWSETTING_CONTEXT];
        }
    }
}
+ (void)removeObserver:(id)observer fromPreviewSetting:(PreviewSetting **)previewSetting
{
    if(previewSetting == nil || *previewSetting == nil)
    {
        return;
    }
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            [(*previewSetting) removeObserver:observer forKeyPath:key];
        }
    }
}

+ (NSString *)getPrinterType:(NSString *)printerName
{
    if ([self isISSeries:printerName])
    {
        return @"IS";
    }
    else if ([self isFWSeries:printerName])
    {
        return @"FW";
    }
    else if([self isGDSeries:printerName])
    {
        return @"GD";
    }
    else if([self isFTSeries:printerName])
    {
        return @"FT";
    }
    else if([self isGLSeries:printerName])
    {
        return @"GL";
    }
    else
    {
        return @"";
    }
}

+ (BOOL)isISSeries:(NSString *)printerName
{
    if([printerName isEqualToString:@"RISO IS1000C-J"] ||
       [printerName isEqualToString:@"RISO IS1000C-G"] ||
       [printerName isEqualToString:@"RISO IS950C-G"])
    {
        return YES;
    }

    return NO;
}

+ (BOOL)isFWSeries:(NSString *)printerName
{
    NSRange SearchResult = [printerName rangeOfString:@" FW"];
    if(SearchResult.location == NSNotFound){
        return NO;
    }

    return YES;
}

+ (BOOL)isGDSeries:(NSString *)printerName
{
    NSRange SearchResult = [printerName rangeOfString:@" GD"];
    if(SearchResult.location == NSNotFound){
        return NO;
    }

    return YES;
}

+ (BOOL)isFTSeries:(NSString *)printerName
{
    NSRange SearchResult = [printerName rangeOfString:@" FT"];
    if(SearchResult.location == NSNotFound){
        SearchResult = [printerName rangeOfString:@" OIS"];
        if(SearchResult.location == NSNotFound){
            return NO;
        }
    }

    return YES;
}

+ (BOOL)isGLSeries:(NSString *)printerName
{
    NSRange SearchResult = [printerName rangeOfString:@" GL"];
    if (SearchResult.location == NSNotFound) {
        return NO;
    }
    
    return YES;
}

+ (BOOL)isFTorGLSeries: (NSString *) printerName
{
    return [self isFTSeries:printerName] || [self isGLSeries:printerName];
}

@end
