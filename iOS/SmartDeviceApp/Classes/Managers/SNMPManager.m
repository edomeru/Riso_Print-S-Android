//
//  SNMPManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SNMPManager.h"
#import "PListHelper.h"
#import "PrinterDetails.h"
#import "NotificationNames.h"
#import "common.h" 

#define BROADCAST_ADDRESS @"255.255.255.255"

static snmp_context* snmpContext;
static void snmpDiscoveryEndedCallback(snmp_context* context, int result);
static void snmpPrinterAddedCallback(snmp_context* context, snmp_device* device);

static SNMPManager* sharedSNMPManager = nil;

@interface SNMPManager ()

/** 
 If YES, use Net-SNMP common library.
 If NO, use fake SNMP implementation. 
 */
@property (assign, nonatomic) BOOL useSNMPCommonLib;

/** 
 This property is used only for the fake SNMP implementation.
 If YES, the manual search will timeout without "receiving" the 
 Printer Added callback.
 */
@property (assign, nonatomic) BOOL useSNMPUnicastTimeout;

/**
 Handler for the Add Printer Callback of the Net-SNMP.
 Parses the printer name, IP, and capabilities from the device
 object then posts a notification that a printer was found.
 @param device
        object containing the device name, IP, and capabilities
 */
- (void)addRealPrinter:(snmp_device*)device;

/**
 Handler for the Add Printer Callback of the Fake SNMP.
 Generates a fake printer name, IP, and capabilities then
 posts a notification that a printer was found.
 FOR DEBUGGING PURPOSES ONLY.
 @param ip
        IP address for the fake printer
 */
- (void)addFakePrinter:(NSString*)fakeIP;

/**
 Handler for the Search End Callback of the Net-SNMP.
 Posts a notification that the search has ended.
 @param success
        YES if at least one printer was found, NO otherwise
 */
- (void)endRealSearchWithResult:(BOOL)success;

/**
 Handler for the Search End Callback of the Fake SNMP.
 Posts a notification that the search has ended.
 FOR DEBUGGING PURPOSES ONLY.
 */
- (void)endFakeSearch;

@end

@implementation SNMPManager

#pragma mark - Initialization

- (id)init
{
    self = [super init];
    if (self)
    {
        self.useSNMPCommonLib = [PListHelper readBool:kPlistBoolValUseSNMP];
        self.useSNMPUnicastTimeout = [PListHelper readBool:kPlistBoolValUseSNMPTimeout];
    }
    return self;
}

+ (SNMPManager*)sharedSNMPManager
{
    @synchronized(self)
    {
        if (sharedSNMPManager == nil)
            sharedSNMPManager = [[self alloc] init];
    }
    return sharedSNMPManager;
}

#pragma mark - Printer Search (Manual Search)

- (void)searchForPrinter:(NSString*)printerIP;
{
    if (self.useSNMPCommonLib)
    {
        // Net-SNMP
        // initiate SNMP Manual Search
        snmpContext = snmp_context_new(&snmpDiscoveryEndedCallback, &snmpPrinterAddedCallback);
        snmp_manual_discovery(snmpContext, [printerIP UTF8String]);
    }
    else
    {
        // "Fake" SNMP
        // 1. receive the Printer Added callback after 2 seconds
        // 2. receive the Discovery Ended callback after 30 seconds
        // 3. if timeout is enabled, the Printer Added callback will never be received
        
        if (self.useSNMPUnicastTimeout)
        {
#if DEBUG_LOG_SNMP_MANAGER
            NSLog(@"[INFO][SNMPM] search timeout");
#endif
        }
        else
        {
            [NSThread sleepForTimeInterval:2];
            [self addFakePrinter:printerIP];
        }
        
        [NSThread sleepForTimeInterval:28];
        [self endFakeSearch];
    }
}

#pragma mark - Printer Search (Device Discovery)

- (void)searchForAvailablePrinters
{
    if (self.useSNMPCommonLib)
    {
        // Net-SNMP
        // initiate Device Discovery
        snmpContext = snmp_context_new(&snmpDiscoveryEndedCallback, &snmpPrinterAddedCallback);
        snmp_device_discovery(snmpContext);
    }
    else
    {
        // "Fake" SNMP
        //  1. receive the Printer Added callback every x seconds
        //  2. receive the Discovery Ended callback after 30 seconds
        
        [NSThread sleepForTimeInterval:1];
        [self addFakePrinter:@"192.168.1.1"];
        
        [NSThread sleepForTimeInterval:2];
        [self addFakePrinter:@"192.168.2.2"];
        
        [NSThread sleepForTimeInterval:2];
        [self addFakePrinter:@"192.168.3.3"];
        
        [NSThread sleepForTimeInterval:5];
        [self addFakePrinter:@"192.168.4.4"];
        
        [NSThread sleepForTimeInterval:3];
        [self addFakePrinter:@"192.168.5.5"];
        
        [NSThread sleepForTimeInterval:17];
        [self endFakeSearch];
    }
}

#pragma mark - Cancel Search

- (void)cancelSearch
{
    if (snmpContext == nil) //there is no ongoing search
        return;
    
    snmp_cancel(snmpContext);
    snmp_context_free(snmpContext);
    snmpContext = nil;
    
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] search canceled");
#endif
}

#pragma mark - Net-SNMP Callback Handlers

- (void)addRealPrinter:(snmp_device*)device
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] adding real printer");
#endif

    // parse the printer name, IP, and capabilities
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.name = [NSString stringWithFormat:@"%s", snmp_device_get_name(device)];
    pd.ip = [NSString stringWithFormat:@"%s", snmp_device_get_ip_address(device)];
    pd.port = [NSNumber numberWithInt:0]; //TODO: get proper port (LPR or RAW)
    pd.enBooklet = (snmp_device_get_capability_status(device, kSnmpCapabilityBooklet) > 0 ? YES : NO);
    pd.enFinisher23Holes = (snmp_device_get_capability_status(device, kSnmpCapabilityFin23Holes) > 0 ? YES : NO);
    pd.enFinisher24Holes = (snmp_device_get_capability_status(device, kSnmpCapabilityFin24Holes) > 0 ? YES : NO);
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.enStaple = (snmp_device_get_capability_status(device, kSnmpCapabilityStapler) > 0 ? YES : NO);
    pd.enTrayAutoStacking = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayAutoStack) > 0 ? YES : NO);
    pd.enTrayFaceDown = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayFaceDown) > 0 ? YES : NO);
    pd.enTrayStacking = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayStack) > 0 ? YES : NO);
    pd.enTrayTop = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayTop) > 0 ? YES : NO);
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] name=%@", pd.name);
    NSLog(@"[INFO][SNMPM] ip=%@", pd.ip);
    NSLog(@"[INFO][SNMPM] port=%d", [pd.port intValue]);
    NSLog(@"[INFO][SNMPM] enBooklet=%@", pd.enBooklet ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enFinisher23Holes=%@", pd.enFinisher23Holes ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enFinisher24Holes=%@", pd.enFinisher24Holes ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enLpr=%@", pd.enLpr ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enRaw=%@", pd.enRaw ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enStaple=%@", pd.enStaple ? @YES : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayAutoStacking=%@", pd.enTrayAutoStacking ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayFaceDown=%@", pd.enTrayFaceDown ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayStacking=%@", pd.enTrayStacking ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayTop=%@", pd.enTrayTop ? @"YES" : @"NO");
#endif
    
    // notify observer that a printer was found (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD
                                                            object:pd];
    });
}

- (void)endRealSearchWithResult:(BOOL)success
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] ending real search, success=%@", success ? @"YES" : @"NO");
#endif

    // notify observer that the search has ended (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END
                                                            object:nil];
    });
}

#pragma mark - "Fake" SNMP Callback Handlers

- (void)addFakePrinter:(NSString*)fakeIP
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] adding fake printer");
#endif
    
    // invent printer info and capabilities
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.ip = fakeIP;
    pd.name = [NSString stringWithFormat:@"RISO Printer %@", pd.ip];
    pd.port = [NSNumber numberWithInt:0]; //TODO: use proper port (LPR or RAW)
    pd.enBooklet = YES;
    pd.enFinisher23Holes = YES;
    pd.enFinisher24Holes = NO;
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.enStaple = YES;
    pd.enTrayAutoStacking = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] name=%@", pd.name);
    NSLog(@"[INFO][SNMPM] ip=%@", pd.ip);
    NSLog(@"[INFO][SNMPM] all capabilities = YES");
#endif
    
    // notify observer that a "printer" was found (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD
                                                            object:pd];
    });
}

- (void)endFakeSearch
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] ending fake search");
#endif
    
    // notify observer that the "search" has ended (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END
                                                            object:nil];
    });
}

@end

#pragma mark - Net-SNMP Callbacks

static void snmpDiscoveryEndedCallback(snmp_context* context, int result)
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][Net-SNMP] received Discovery Ended callback");
#endif
    
    snmp_context_free(snmpContext);
    snmpContext = nil;
    
    // let the SNMPManager handle the result
    SNMPManager* manager = [SNMPManager sharedSNMPManager];
    [manager endRealSearchWithResult:(result > 0 ? YES : NO)];
}

static void snmpPrinterAddedCallback(snmp_context* context, snmp_device* device)
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][Net-SNMP] received Printer Added callback");
#endif
    
    // ignore devices that responded to the broadcast IP
    NSString* deviceIP = [NSString stringWithFormat:@"%s", snmp_device_get_ip_address(device)];
    if ([deviceIP isEqualToString:BROADCAST_ADDRESS])
    {
#if DEBUG_LOG_SNMP_MANAGER
        NSLog(@"[INFO][Net-SNMP] ignoring device with IP=%@", deviceIP);
#endif
        return;
    }
    
    // let the SNMPManager handle the device
    SNMPManager* manager = [SNMPManager sharedSNMPManager];
    [manager addRealPrinter:device];
}
