//
//  PrintSettingsHelper.m
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsHelper.h"
#import "XMLParser.h"
#import "PreviewSetting.h"

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
            NSString *key = [setting objectForKey:@"key"];
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

@end
