//
//  SNMPManagerMock.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SNMPManagerMock.h"
#import "SNMPManager.h"
#import "common.h"

@interface SNMPManager()

- (void)addRealPrinter:(snmp_device*)device;
- (void)endRealSearchWithResult:(BOOL)success;

@end

@implementation SNMPManagerMock

- (void)searchForPrinterSuccessful:(NSString*)printerIP
{
    SNMPManager *manager = [SNMPManager sharedSNMPManager];
    snmp_device *device = snmp_device_new([printerIP UTF8String]);
    [manager addRealPrinter:device];
    [manager endRealSearchWithResult:(NO)];
    snmp_device_free(device);
}

- (void)searchForPrinterFail:(NSString*)printerIP
{
    SNMPManager *manager = [SNMPManager sharedSNMPManager];
    
    [manager endRealSearchWithResult:(NO)];
}


@end
