//
//  Printer+Log.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
    [msg appendFormat:@"\n   enabled_booklet_finishing=%ld", (long)[self.enabled_booklet_finishing integerValue]];
    [msg appendFormat:@"\n   enabled_finisher_2_3_holes=%ld", (long)[self.enabled_finisher_2_3_holes integerValue]];
    [msg appendFormat:@"\n   enabled_finisher_2_4_holes=%ld", (long)[self.enabled_finisher_2_4_holes integerValue]];
    [msg appendFormat:@"\n   enabled_lpr=%ld", (long)[self.enabled_lpr integerValue]];
    [msg appendFormat:@"\n   enabled_raw=%ld", (long)[self.enabled_raw integerValue]];
    [msg appendFormat:@"\n   enabled_staple=%ld", (long)[self.enabled_staple integerValue]];
    [msg appendFormat:@"\n   enabled_tray_face_down=%ld", (long)[self.enabled_tray_face_down integerValue]];
    [msg appendFormat:@"\n   enabled_tray_stacking=%ld", (long)[self.enabled_tray_stacking integerValue]];
    [msg appendFormat:@"\n   enabled_tray_top=%ld", (long)[self.enabled_tray_top integerValue]];
    [msg appendFormat:@"\n   enabled_external_feeder=%ld", (long)[self.enabled_external_feeder integerValue]];
    [msg appendFormat:@"\n   enabled_finisher_0_hole=%ld", (long)[self.enabled_finisher_0_hole integerValue]];
    if (self.printsetting != nil)
#if DEBUG_LOG_PRINTSETTING_MODEL
        [self.printsetting log];
#endif
    NSLog(@"[INFO][Printer]\n%@\n", msg);
}

@end
