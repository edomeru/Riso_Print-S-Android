//
//  PrinterStatusHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterStatusHelper.h"
#import "NetworkManager.h"

#define SECONDS_IN_BETWEEN_PINGS   5

@interface PrinterStatusHelper ()

@property (nonatomic) NSTimer *pollingTimer;

@property (strong, nonatomic) SimplePing* pinger;
@property (assign, nonatomic) BOOL respondedToPing;

-(void) getPrinterStatus;

@end

@implementation PrinterStatusHelper

- (id)initWithPrinterIP:(NSString *) ipAddress
{
    self = [super init];
    
    if(self != nil)
    {
        self.ipAddress = [NSString stringWithString:ipAddress];
    }
    
    return self;
}

- (void)getPrinterStatus
{
    // update UI for current ping status
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] %@ is %@", self.ipAddress, (self.respondedToPing ? @"ONLINE" : @"OFFLINE"));
#endif
    [self.delegate statusDidChange:self.respondedToPing];
    
    [self.pinger sendPingWithData:nil];
    self.respondedToPing = NO;
}

- (void)startPrinterStatusPolling
{
    if (self.pinger == nil)
    {
        self.pinger = [SimplePing simplePingWithHostName:self.ipAddress];
        self.pinger.delegate = self;
        
        [self.pinger start];
    }
}

- (void)stopPrinterStatusPolling
{
    if (self.pinger != nil)
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"[INFO][PrinterStatus] stop pinger for %@", self.ipAddress);
#endif
        [self.pinger stop];
        self.pinger = nil;
    }
    
    if (self.pollingTimer != nil)
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"[INFO][PrinterStatus] stop polling timer for %@", self.ipAddress);
#endif
        [self.pollingTimer invalidate];
        self.pollingTimer = nil;
    }
}

- (BOOL)isPolling
{
    return ((self.pinger != nil) && (self.pollingTimer != nil));
}

#pragma mark - SimplePingDelegate

- (void)simplePing:(SimplePing*)pinger didStartWithAddress:(NSData*)address
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] pinger started for %@", self.ipAddress);
#endif
    
    // fire off the first ping
    [self.pinger sendPingWithData:nil];
    self.respondedToPing = NO;
    
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] start polling timer for %@", self.ipAddress);
#endif
    
    // set a timer that will repeatedly ping the IP
    self.pollingTimer = [NSTimer scheduledTimerWithTimeInterval:SECONDS_IN_BETWEEN_PINGS
                                                         target:self
                                                       selector:@selector(getPrinterStatus)
                                                       userInfo:nil
                                                        repeats:YES];
}

- (void)simplePing:(SimplePing*)pinger didSendPacket:(NSData*)packet
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] ping sent %@", self.ipAddress);
#endif
}

- (void)simplePing:(SimplePing*)pinger didReceivePingResponsePacket:(NSData*)packet
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] ping response %@", self.ipAddress);
#endif
    self.respondedToPing = YES;
}

- (void)simplePing:(SimplePing*)pinger didFailWithError:(NSError*)error
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] ping failed %@", self.ipAddress);
#endif
}

- (void)simplePing:(SimplePing*)pinger didFailToSendPacket:(NSData*)packet error:(NSError*)error
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PrinterStatus] ping send failed %@", self.ipAddress);
#endif
}

@end
