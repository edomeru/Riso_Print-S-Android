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

+ (BOOL)searchForPrinter:(Printer**)printer
{
    //TODO send SNMP message to the printer
    
    //TODO wait for SNMP response
    
    //TODO check and get printer capabilities
    
    //TODO save Printer info and capabilities
    (*printer).name = @"New Printer";
    
    return YES;
}

@end
