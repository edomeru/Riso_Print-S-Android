//
//  NSDate+Format.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "NSDate+Format.h"

static NSDateFormatter* dateFormatter = nil;

@implementation NSDate (Format)

- (NSString*)formattedString
{
//format is fixed
//    NSDateFormatter* formattedDate = [[NSDateFormatter alloc] init];
//    [formattedDate setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US"]];
//    [formattedDate setDateFormat:@"yyyy/MM/dd HH:mm"];
//    [formattedDate setTimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
//    return [formattedDate stringFromDate:self];
    
//format is dependent on current locale
    if (dateFormatter == nil)
    {
        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateStyle:NSDateFormatterShortStyle];
        [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    
    [dateFormatter setLocale:[NSLocale currentLocale]];    
    return [dateFormatter stringFromDate:self];
}

@end
