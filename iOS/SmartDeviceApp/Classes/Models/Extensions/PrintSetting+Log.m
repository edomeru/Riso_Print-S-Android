//
//  PrintSetting+Log.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSetting+Log.h"
#import "PrintSettingsHelper.h"

@implementation PrintSetting (Log)

- (void)log
{
    NSMutableString* msg = [NSMutableString stringWithString:@"  Print Settings:"];
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            NSString *text = [setting objectForKey:@"text"];
            [msg appendFormat:@"\n   %@=%ld", NSLocalizedString([text uppercaseString], @""), (long)[[self valueForKey:key] integerValue]];
        }
    }
    
    NSLog(@"[INFO][PrintSetting]\n%@\n", msg);
}

@end
