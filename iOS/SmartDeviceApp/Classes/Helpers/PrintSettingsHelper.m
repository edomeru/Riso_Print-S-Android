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

+ (void)copyDefaultPrintSettings:(PrintSetting **)printSetting
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
            NSString *defaultValue = [setting objectForKey:@"default"];
            
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
@end
