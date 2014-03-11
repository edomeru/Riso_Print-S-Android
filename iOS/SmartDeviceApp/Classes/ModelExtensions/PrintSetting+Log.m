//
//  PrintSetting+Log.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSetting+Log.h"

@implementation PrintSetting (Log)

- (void)log
{
    NSMutableString* msg = [NSMutableString stringWithString:@"  [INFO] Print Settings:"];
    [msg appendFormat:@"\n   %@=%d", PS_BIND, [self.bind integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_BOOKLET_BINDING, [self.booklet_binding integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_BOOKLET_TRAY, [self.booklet_tray integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_CATCH_TRAY, [self.catch_tray integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_COLOR_MODE, [self.color_mode integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_COPIES, [self.copies integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_DUPLEX, [self.duplex integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_IMAGE_QUALITY, [self.image_quality integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_PAGINATION, [self.pagination integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_PAPER_SIZE, [self.paper_size integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_PAPER_TYPE, [self.paper_type integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_PUNCH, [self.punch integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_SORT, [self.sort integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_STAPLE, [self.staple integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_ZOOM, [self.zoom integerValue]];
    [msg appendFormat:@"\n   %@=%d", PS_ZOOM_RATE, [self.zoom_rate integerValue]];
    NSLog(@"\n%@\n", msg);
}

@end
