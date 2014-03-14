//
//  Printer+Log.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "Printer+Log.h"
#import "PrintSetting+Log.h"

@implementation Printer (Log)

- (void)log
{
    NSMutableString* msg = [NSMutableString stringWithString:@"  Printer:"];
    [msg appendFormat:@"\n   Name=%@", self.name];
    [msg appendFormat:@"\n   IP=%@", self.ip_address];
    [msg appendFormat:@"\n   Port=%d", [self.port integerValue]];
    [msg appendFormat:@"\n   enabled_bind=%d", [self.enabled_bind integerValue]];
    [msg appendFormat:@"\n   enabled_booklet_binding=%d", [self.enabled_booklet_binding integerValue]];
    [msg appendFormat:@"\n   enabled_duplex=%d", [self.enabled_duplex integerValue]];
    [msg appendFormat:@"\n   enabled_lpr=%d", [self.enabled_lpr integerValue]];
    [msg appendFormat:@"\n   enabled_raw=%d", [self.enabled_raw integerValue]];
    [msg appendFormat:@"\n   enabled_pagination=%d", [self.enabled_pagination integerValue]];
    [msg appendFormat:@"\n   enabled_staple=%d", [self.enabled_staple integerValue]];
    if (self.printsetting != nil)
        [self.printsetting log];
    NSLog(@"[INFO][Printer]\n%@\n", msg);
}

@end
