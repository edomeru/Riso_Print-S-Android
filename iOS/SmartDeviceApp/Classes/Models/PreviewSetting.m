//
//  PreviewSetting.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"

@implementation PreviewSetting

- (NSString *)formattedString
{
    NSMutableString *string = [[NSMutableString alloc] init];
    
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            NSString *type = [setting objectForKey:@"type"];
            
            if ([self respondsToSelector:NSSelectorFromString(key)] == NO)
            {
                continue;
            }
            
            NSString *item;
            NSNumber *value = [self valueForKey:key];
            if ([type isEqualToString:@"list"] || [type isEqualToString:@"numeric"])
            {
                item = [NSString stringWithFormat:@"%@=%d\n", key, [value intValue]];
            }
            else
            {
                int boolValue = 0;
                if ([value boolValue])
                {
                    boolValue = 1;
                }
                item = [NSString stringWithFormat:@"%@=%d\n", key, boolValue];
            }
            [string appendString:item];
        }
    }
    
    return string;
}

@end
