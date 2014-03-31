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
    /*[msg appendFormat:@"\n   %@=%ld", PS_BIND, (long)[self.bind integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_BOOKLET_BINDING, (long)[self.booklet_binding integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_BOOKLET_TRAY, (long)[self.booklet_tray integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_CATCH_TRAY, (long)[self.catch_tray integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_COLOR_MODE, (long)[self.color_mode integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_COPIES, (long)[self.copies integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_DUPLEX, (long)[self.duplex integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_IMAGE_QUALITY, (long)[self.image_quality integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_PAGINATION, (long)[self.pagination integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_PAPER_SIZE, (long)[self.paper_size integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_PAPER_TYPE, (long)[self.paper_type integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_PUNCH, (long)[self.punch integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_SORT, (long)[self.sort integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_STAPLE, (long)[self.staple integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_ZOOM, (long)[self.zoom integerValue]];
    [msg appendFormat:@"\n   %@=%ld", PS_ZOOM_RATE, (long)[self.zoom_rate integerValue]];*/
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"key"];
            NSString *text = [setting objectForKey:@"text"];
            [msg appendFormat:@"\n   %@=%ld", NSLocalizedString([text uppercaseString], @""), (long)[[self valueForKey:key] integerValue]];
        }
    }
    
    NSLog(@"[INFO][PrintSetting]\n%@\n", msg);
}

@end
