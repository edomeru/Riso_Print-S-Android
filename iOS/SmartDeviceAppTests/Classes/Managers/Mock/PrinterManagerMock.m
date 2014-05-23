//
//  PrinterManagerMock.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterManagerMock.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "NotificationNames.h"

@interface PrinterManager ()

- (void)notifyPrinterFound:(NSNotification*)notif;
- (void)notifySearchEnded:(NSNotification*)notif;

@end

@implementation PrinterManagerMock

- (void)searchForPrinterSuccessful:(NSString*)printerIP
{
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.name = @"RISO Printer";
    pd.ip = @"192.168.0.199";
    pd.port = [NSNumber numberWithInt:0];
    pd.enBooklet = YES;
    pd.enFinisher23Holes = NO;
    pd.enFinisher24Holes = YES;
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.enStaple = YES;
    pd.enTrayAutoStacking = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
    NSNotification* notif = [NSNotification notificationWithName:NOTIF_SNMP_ADD object:self userInfo:@{@"printerDetails": pd}];
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    [pm notifyPrinterFound:notif];
    [pm notifySearchEnded:[NSNotification notificationWithName:NOTIF_SNMP_END
                                                        object:self userInfo:@{@"result": [NSNumber numberWithBool:YES]}]];
}

- (void)searchForPrinterFail:(NSString*)printerIP
{
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    [pm notifySearchEnded:[NSNotification notificationWithName:NOTIF_SNMP_END
                                                        object:self userInfo:@{@"result": [NSNumber numberWithBool:NO]}]];
}

- (void)searchForAllPrintersSuccessful
{
    NSString* prefix = @"192.168.0.";
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    for (int i = 0; i < 3; i++) 
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = @"RISO Printer";
        pd.ip = [NSString stringWithFormat:@"%@%d", prefix, i];
        pd.port = [NSNumber numberWithInt:0];
        pd.enBooklet = YES;
        pd.enFinisher23Holes = NO;
        pd.enFinisher24Holes = YES;
        pd.enLpr = YES;
        pd.enRaw = YES;
        pd.enStaple = YES;
        pd.enTrayAutoStacking = YES;
        pd.enTrayFaceDown = YES;
        pd.enTrayStacking = YES;
        pd.enTrayTop = YES;
        
        [pm notifyPrinterFound:[NSNotification notificationWithName:NOTIF_SNMP_ADD object:nil userInfo:@{@"printerDetails": pd}]];
    }
    
    [pm notifySearchEnded:[NSNotification notificationWithName:NOTIF_SNMP_END
                                                        object:self userInfo:@{@"result": [NSNumber numberWithBool:YES]}]];
}

@end
