//
//  SNMPManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SNMPManager.h"
#import "PListUtils.h"
#import "PrinterDetails.h"

static BOOL useSNMPCommonLib;       /** if YES, use real SNMP lib. if NO, use "fake" SNMP implementation. */
static BOOL useSNMPUnicastTimeout;  /** (for "fake" SNMP) if YES, will cause the manual search to timeout. */
static NSUInteger printerCount;     /** (for "fake" SNMP) auto-incremented tag for each new printer. */
static BOOL isManualSearch;         /** if YES, searching a specific printer IP. if NO, searching all printers. */
static NSString* searchedIP;        /** stores the manually searched printer IP address */

@interface SNMPManager ()

/**
 Handler for the SNMP Add Searched Printer callback.
 Parses the Printer info and capabilities then posts a 
 notification that a printer was found.
 */
+ (void)add;

/**
 Handler for the SNMP End callback.
 Posts a notification that the search has ended.
 */
+ (void)end;

@end

@implementation SNMPManager

#pragma mark - Printer Search (Manual Search)

+ (void)searchForPrinter:(NSString*)printerIP;
{
    isManualSearch = YES;
    searchedIP = printerIP;
    
    useSNMPCommonLib = [PListUtils readBool:PL_BOOL_USE_SNMP];
    if (useSNMPCommonLib)
    {
        //TODO: initiate SNMP Manual Search
    }
    else
    {
        // "fake" SNMP
        // 1. receive "SNMP Add Printer Callback" after 2 seconds
        // 2. the SNMP Common Library returns after 4 seconds
        // 3. if timeout is enabled, the "SNMP Add Printer Callback" will never be received
        useSNMPUnicastTimeout = [PListUtils readBool:PL_BOOL_USE_SNMP_TIMEOUT];
        if (!useSNMPUnicastTimeout)
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [NSTimer scheduledTimerWithTimeInterval:2
                                                 target:self
                                               selector:@selector(add)
                                               userInfo:nil
                                                repeats:NO];
            });
        }
        else
        {
            NSLog(@"[INFO][SNMP] search timeout");
        }
        
        [NSThread sleepForTimeInterval:4];
        [self end];
    }
}

#pragma mark - Printer Search (Device Discovery)

+ (void)searchForAvailablePrinters
{
    isManualSearch = NO;
    searchedIP = nil;
    
    useSNMPCommonLib = [PListUtils readBool:PL_BOOL_USE_SNMP];
    if (useSNMPCommonLib)
    {
        //TODO: initiate SNMP Device Discovery
    }
    else
    {
        // "fake" SNMP
        //  1. receive "SNMP Add Searched Printer Callback" every x=5, x+=5,.. seconds
        //  2. post notification containing the new printer
        //  3. receive the "SNMP End Callback" after 30 seconds
        //  4. post notification that the search is over
        // note: timers need to be in main queue
        printerCount = 0;
        dispatch_async(dispatch_get_main_queue(), ^{
            [NSTimer scheduledTimerWithTimeInterval:5
                                             target:self
                                           selector:@selector(add)
                                           userInfo:nil
                                            repeats:NO];
            [NSTimer scheduledTimerWithTimeInterval:10
                                             target:self
                                           selector:@selector(add)
                                           userInfo:nil
                                            repeats:NO];
            [NSTimer scheduledTimerWithTimeInterval:15
                                             target:self
                                           selector:@selector(add)
                                           userInfo:nil
                                            repeats:NO];
            [NSTimer scheduledTimerWithTimeInterval:20
                                             target:self
                                           selector:@selector(add)
                                           userInfo:nil
                                            repeats:NO];
            [NSTimer scheduledTimerWithTimeInterval:25
                                             target:self
                                           selector:@selector(add)
                                           userInfo:nil
                                            repeats:NO];
            [NSTimer scheduledTimerWithTimeInterval:30
                                             target:self
                                           selector:@selector(end)
                                           userInfo:nil
                                            repeats:NO];
        });
    }
}

#pragma mark - Get Printer Status

+ (BOOL)getPrinterStatus:(NSString*)ipAddress
{
    //TODO Get status from SNMP
    
    //TODO remove stub code
    //STUB Code
    int onlineStatus = arc4random() % 2;
    
    return onlineStatus;
}

#pragma mark - SNMP Callback Handlers

+ (void)add
{
    NSLog(@"[INFO][SNMP] received SNMP Add Printer callback %d", ++printerCount);
    
    // get/parse printer info and capabilities
    PrinterDetails* printerDetails = [[PrinterDetails alloc] init];
    
    if (useSNMPCommonLib)
    {
        //TODO: parse results of SNMP Device Discovery
    }
    else
    {
        // "fake" SNMP
        // invent printer info and capabilities
        if (isManualSearch)
            printerDetails.ip = searchedIP;
        else
            printerDetails.ip = [NSString stringWithFormat:@"192.168.%d.%d", printerCount, printerCount];
        printerDetails.name = [NSString stringWithFormat:@"RISO Printer %@", printerDetails.ip];
        printerDetails.port = [NSNumber numberWithUnsignedInt:printerCount*100];
        printerDetails.enBind = NO;
        printerDetails.enBookletBind = NO;
        printerDetails.enDuplex = NO;
        printerDetails.enPagination = NO;
        printerDetails.enStaple = NO;
        printerDetails.enLPR = YES;
        printerDetails.enLPR = NO;
    }
    
    // notify observer that a printer was found (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD
                                                            object:printerDetails];
    });
}

+ (void)end
{
    NSLog(@"[INFO][SNMP] received SNMP End callback");
    
    // notify observer that the search has ended (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END
                                                            object:nil];
    });
}

@end
