//
//  PrintJob+Log.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJob+Log.h"
#import "Printer.h"
#import "NSDate+Format.h"

@implementation PrintJob (Log)

- (void)log
{
    NSMutableString* msg = [NSMutableString stringWithString:@"  Print Job:"];
    [msg appendFormat:@"\n   Name=%@", self.name];
    [msg appendFormat:@"\n   Result=%@", [self.result boolValue] ? @"YES" : @"NO"];
    [msg appendFormat:@"\n   Date=%@", [self.date formattedString]];
    [msg appendFormat:@"\n   Printer=%@", self.printer.name];
    NSLog(@"[INFO][PrintJob]\n%@\n", msg);
}

@end
