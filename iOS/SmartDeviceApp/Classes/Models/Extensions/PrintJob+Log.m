//
//  PrintJob+Log.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJob+Log.h"
#import "Printer.h"

@implementation PrintJob (Log)

- (void)log
{
    NSMutableString* msg = [NSMutableString stringWithString:@"  Print Job:"];
    
    [msg appendFormat:@"\n   Name=%@", self.name];
    [msg appendFormat:@"\n   Result=%@", [self.result boolValue] ? @"YES" : @"NO"];
    
    NSDateFormatter* timestampFormat = [[NSDateFormatter alloc] init];
    [timestampFormat setDateFormat:@"yyyy/MM/dd HH:mm"];
    [timestampFormat setTimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    [msg appendFormat:@"\n   Date=%@", [timestampFormat stringFromDate:self.date]];
    
    [msg appendFormat:@"\n   Printer=%@", self.printer.name];
    
    NSLog(@"[INFO][PrintJob]\n%@\n", msg);
}

@end
