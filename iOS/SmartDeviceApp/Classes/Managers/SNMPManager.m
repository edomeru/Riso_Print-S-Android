//
//  SNMPManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SNMPManager.h"
#import "Printer.h"

@implementation SNMPManager

+ (NSArray*)searchForPrinter:(NSString*)printerIP;
{
    //TODO: send SNMP message to the printer
    
    //TODO: wait for SNMP response
    
    //TODO: check and get printer capabilities
    //TODO: check if printer is supported
    
    //TODO: if printer is supported, store printer info and capabilities
    NSArray* printerInfoCapabilities = @[printerIP, @"New Printer"];
    return printerInfoCapabilities;
    
    //TODO: if printer is not supported
    //return nil;
}

+ (BOOL) getPrinterStatus:(NSString *) ipAddress
{
    //TODO Get status from SNMP
    
    //TODO remove stub code
    //STUB Code
    int onlineStatus = arc4random() % 2;
    
    return onlineStatus;
}
@end
