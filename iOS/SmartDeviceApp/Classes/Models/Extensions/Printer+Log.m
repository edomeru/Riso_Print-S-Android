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
    [msg appendFormat:@"\n   Port=%ld", (long)[self.port integerValue]];
    [msg appendFormat:@"\n   enabled_bind=%ld", (long)[self.enabled_bind integerValue]];
    [msg appendFormat:@"\n   enabled_booklet_binding=%ld", (long)[self.enabled_booklet_binding integerValue]];
    [msg appendFormat:@"\n   enabled_duplex=%ld", (long)[self.enabled_duplex integerValue]];
    [msg appendFormat:@"\n   enabled_lpr=%ld", (long)[self.enabled_lpr integerValue]];
    [msg appendFormat:@"\n   enabled_raw=%ld", (long)[self.enabled_raw integerValue]];
    [msg appendFormat:@"\n   enabled_pagination=%ld", (long)[self.enabled_pagination integerValue]];
    [msg appendFormat:@"\n   enabled_staple=%ld", (long)[self.enabled_staple integerValue]];
    if (self.printsetting != nil)
#if DEBUG_LOG_PRINTSETTING_MODEL
        [self.printsetting log];
#endif
    NSLog(@"[INFO][Printer]\n%@\n", msg);
}

@end
