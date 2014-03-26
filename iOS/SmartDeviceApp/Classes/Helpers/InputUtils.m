//
//  InputUtils.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/21/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "InputUtils.h"

@implementation InputUtils

#pragma mark - IP Address

+ (NSString*)trimIP:(NSString*)inputIP
{
    //leading zeroes are disregarded
    NSString* pattern = @"^0+";
    
    NSError* error = nil;
    NSRegularExpression* regex = [NSRegularExpression regularExpressionWithPattern:pattern
                                                                           options:0
                                                                             error:&error];
    NSMutableString* trimmedIP = [NSMutableString stringWithString:inputIP];
    [regex replaceMatchesInString:trimmedIP
                          options:0
                            range:NSMakeRange(0, [inputIP length])
                     withTemplate:@""];
    
    return trimmedIP;
}

+ (BOOL)isIPValid:(NSString*)inputIP;
{
    // quick check : # of characters must be 7-15
    NSUInteger numChars = [inputIP length];
    if (numChars < 7 || numChars > 15)
        return NO;
    
    // create the IP Address regex pattern
    NSMutableString* pattern = [NSMutableString stringWithString:@""];
    [pattern appendString:@"^"];                                //start
    [pattern appendString:@"([01]?\\d\\d?|2[0-4]\\d|25[0-5])"]; //1st digit
    [pattern appendString:@"\\."];                              //dot separator
    [pattern appendString:@"([01]?\\d\\d?|2[0-4]\\d|25[0-5])"]; //2nd digit
    [pattern appendString:@"\\."];                              //dot separator
    [pattern appendString:@"([01]?\\d\\d?|2[0-4]\\d|25[0-5])"]; //3rd digit
    [pattern appendString:@"\\."];                              //dot separator
    [pattern appendString:@"([01]?\\d\\d?|2[0-4]\\d|25[0-5])"]; //4th digit
    [pattern appendString:@"$"];                                //end
    
    // create the regex
    NSError* error = nil;
    NSRegularExpression* regex = [NSRegularExpression regularExpressionWithPattern:pattern
                                                                           options:0
                                                                             error:&error];
    
    // find the pattern in the input string
    NSRange matches = [regex rangeOfFirstMatchInString:inputIP
                                               options:NSMatchingReportProgress
                                                 range:NSMakeRange(0, [inputIP length])];
    
    // check if input matched the pattern
    if (matches.location == NSNotFound)
        return NO;
    else
        return YES;
}

@end
