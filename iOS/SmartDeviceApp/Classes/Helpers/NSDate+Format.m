//
//  NSDate+Format.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "NSDate+Format.h"

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
    return [NSDateFormatter localizedStringFromDate:self
                                          dateStyle:NSDateFormatterShortStyle
                                          timeStyle:NSDateFormatterShortStyle];
}

@end
