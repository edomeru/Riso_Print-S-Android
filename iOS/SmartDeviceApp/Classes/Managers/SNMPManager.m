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

static NSLock *lock = nil;
static SNMPManager* sharedSNMPManager = nil;

@interface SNMPManager ()

/**
 * Handler for the snmpPrinterAddedCallback of the SNMP common library.
 * Parses the printer name, IP address, port, and capabilities from the device
 * object then posts a NOTIF_SNMP_ADD notification which contains the said printer info.
 *
 * @param device object containing the printer name, IP, port, and printer capabilities
 */
- (void)addRealPrinter:(snmp_device*)device;

#if DEBUG_SNMP_USE_FAKE_PRINTERS
/**
 * THIS IS FOR DEBUGGING PURPOSES ONLY.
 * This simulates the Add Printer callback of the SNMP common library.
 *
 * @param fakeIP IP address of the fake printer
 */
- (void)addFakePrinter:(NSString*)fakeIP;
#endif

/**
 * Handler for the the snmpDiscoveryEndedCallback of the SNMP common library.
 * Posts the NOTIF_SNMP_END notification which contains the result of the search.
 *
 * @param success YES if at least one printer was found, NO otherwise
 */
- (void)endSearchWithResult:(BOOL)success;

@end

@implementation SNMPManager

#pragma mark - Initialization

- (id)init
{
    self = [super init];
    if (self)
    {
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

#pragma mark - State

+ (BOOL)idle
{
    BOOL result = YES;
    [lock lock];
    result = (snmpContext == nil);
    [lock unlock];
    
    return result;
}

#pragma mark - Printer Search (Manual Search)

- (void)searchForPrinter:(NSString*)printerIP;
{
#if !DEBUG_SNMP_USE_FAKE_PRINTERS
    snmpContext = snmp_context_new(&snmpDiscoveryEndedCallback, &snmpPrinterAddedCallback);
    snmp_manual_discovery(snmpContext, [printerIP UTF8String]);
#else
    // "Fake" SNMP
    // 1. receive the Printer Added callback after 2 seconds
    // 2. receive the Discovery Ended callback after 10 seconds
    // 3. if timeout is enabled, the Printer Added callback will never be received
    
#if DEBUG_SNMP_USE_TIMEOUT
    
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] search timeout");
#endif
    
    [NSThread sleepForTimeInterval:8];
    [self endSearchWithResult:NO];
    
#else
    
    [NSThread sleepForTimeInterval:2];
    [self addFakePrinter:printerIP];
    [NSThread sleepForTimeInterval:8];
    [self endSearchWithResult:YES];
    
#endif // DEBUG_SNMP_USE_TIMEOUT
    
#endif // DEBUG_SNMP_USE_FAKE_PRINTERS
}

#pragma mark - Printer Search (Device Discovery)

- (void)searchForAvailablePrinters
{
#if !DEBUG_SNMP_USE_FAKE_PRINTERS
    snmpContext = snmp_context_new(&snmpDiscoveryEndedCallback, &snmpPrinterAddedCallback);
    snmp_device_discovery(snmpContext);
#else
    [NSThread sleepForTimeInterval:1];
    [self addFakePrinter:@"192.168.1.1"];
    
    [NSThread sleepForTimeInterval:2];
    [self addFakePrinter:@"192.168.2.2"];
    
    [NSThread sleepForTimeInterval:2];
    [self addFakePrinter:@"192.168.3.3"];
    
    [NSThread sleepForTimeInterval:1];
    [self addFakePrinter:@"192.168.4.4"];
    
    [NSThread sleepForTimeInterval:1];
    [self addFakePrinter:@"192.168.5.5"];
    
    [NSThread sleepForTimeInterval:3];
    [self endSearchWithResult:YES];
#endif
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
    pd.port = [NSNumber numberWithInt:0];
    pd.enBookletFinishing = (snmp_device_get_capability_status(device, kSnmpCapabilityBookletFinishing) > 0 ? YES : NO);
    pd.enFinisher23Holes = (snmp_device_get_capability_status(device, kSnmpCapabilityFin23Holes) > 0 ? YES : NO);
    pd.enFinisher24Holes = (snmp_device_get_capability_status(device, kSnmpCapabilityFin24Holes) > 0 ? YES : NO);
    pd.enLpr = (snmp_device_get_capability_status(device, kSnmpCapabilityLPR) > 0 ? YES : NO);
    pd.enRaw = (snmp_device_get_capability_status(device, kSnmpCapabilityRaw) > 0 ? YES : NO);
    pd.enStaple = (snmp_device_get_capability_status(device, kSnmpCapabilityStapler) > 0 ? YES : NO);
    pd.enTrayFaceDown = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayFaceDown) > 0 ? YES : NO);
    pd.enTrayStacking = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayStack) > 0 ? YES : NO);
    pd.enTrayTop = (snmp_device_get_capability_status(device, kSnmpCapabilityTrayTop) > 0 ? YES : NO);
    pd.isPrinterFound = YES;
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] name=%@", pd.name);
    NSLog(@"[INFO][SNMPM] ip=%@", pd.ip);
    NSLog(@"[INFO][SNMPM] port=%d", [pd.port intValue]);
    NSLog(@"[INFO][SNMPM] enBookletFinishing=%@", pd.enBookletFinishing ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enFinisher23Holes=%@", pd.enFinisher23Holes ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enFinisher24Holes=%@", pd.enFinisher24Holes ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enLpr=%@", pd.enLpr ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enRaw=%@", pd.enRaw ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enStaple=%@", pd.enStaple ? @YES : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayFaceDown=%@", pd.enTrayFaceDown ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayStacking=%@", pd.enTrayStacking ? @"YES" : @"NO");
    NSLog(@"[INFO][SNMPM] enTrayTop=%@", pd.enTrayTop ? @"YES" : @"NO");
#endif
    
    // notify observer that a printer was found (background thread)
    NSDictionary *userInfo = @{@"printerDetails":pd};
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD
                                                            object:self
                                                          userInfo:userInfo];
    });
}

#if DEBUG_SNMP_USE_FAKE_PRINTERS
- (void)addFakePrinter:(NSString*)fakeIP
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] adding fake printer");
#endif
    
    // invent printer info and capabilities
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.ip = fakeIP;
    pd.name = [NSString stringWithFormat:@"RISO Printer %@", pd.ip];
    pd.port = [NSNumber numberWithInt:0]; 
    pd.enBookletFinishing = YES;
    pd.enFinisher23Holes = NO;
    pd.enFinisher24Holes = YES;
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.enStaple = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
    pd.isPrinterFound = YES;
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] name=%@", pd.name);
    NSLog(@"[INFO][SNMPM] ip=%@", pd.ip);
    NSLog(@"[INFO][SNMPM] all capabilities = YES");
#endif
    
    // notify observer that a "printer" was found (background thread)
    NSDictionary *userInfo = @{@"printerDetails":pd};
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD
                                                            object:self
                                                          userInfo:userInfo];
    });
}
#endif

- (void)endSearchWithResult:(BOOL)success
{
#if DEBUG_LOG_SNMP_MANAGER
    NSLog(@"[INFO][SNMPM] ending search, success=%@", success ? @"YES" : @"NO");
#endif
    
    // notify observer that the search has ended (background thread)
    NSDictionary *userInfo = @{@"result":[NSNumber numberWithBool:success]};
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END
                                                            object:self
                                                          userInfo:userInfo];
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
    [manager endSearchWithResult:(result > 0 ? YES : NO)];
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
