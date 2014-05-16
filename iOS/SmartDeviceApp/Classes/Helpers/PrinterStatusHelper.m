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
@property (assign, nonatomic) BOOL cancelledToBackground;

-(void) getPrinterStatus;

@end

@implementation PrinterStatusHelper

- (id)initWithPrinterIP:(NSString *) ipAddress
{
    self = [super init];
    
    if(self != nil)
    {
        self.ipAddress = [NSString stringWithString:ipAddress];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willResignActive) name:UIApplicationWillResignActiveNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
    
    [self stopPrinterStatusPolling];
}

- (void)getPrinterStatus
{
    // update UI for current ping status
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PSHelper] %@ is %@", self.ipAddress, (self.respondedToPing ? @"ONLINE" : @"OFFLINE"));
#endif
    [self.delegate printerStatusHelper:self statusDidChange:self.respondedToPing];
    
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
        NSLog(@"[INFO][PSHelper] stop pinger for %@", self.ipAddress);
#endif
        [self.pinger stop];
        self.pinger = nil;
    }
    
    if (self.pollingTimer != nil)
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"[INFO][PSHelper] stop polling timer for %@", self.ipAddress);
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
    NSLog(@"[INFO][PSHelper] pinger started for %@", self.ipAddress);
#endif
    
    // fire off the first ping
    [self.pinger sendPingWithData:nil];
    self.respondedToPing = NO;
    
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PSHelper] start polling timer for %@", self.ipAddress);
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
    NSLog(@"[INFO][PSHelper] ping sent %@", self.ipAddress);
#endif
}

- (void)simplePing:(SimplePing*)pinger didReceivePingResponsePacket:(NSData*)packet
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PSHelper] ping response %@", self.ipAddress);
#endif
    self.respondedToPing = YES;
}

- (void)simplePing:(SimplePing*)pinger didFailWithError:(NSError*)error
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PSHelper] ping failed %@", self.ipAddress);
#endif
}

- (void)simplePing:(SimplePing*)pinger didFailToSendPacket:(NSData*)packet error:(NSError*)error
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSLog(@"[INFO][PSHelper] ping send failed %@", self.ipAddress);
#endif
}

#pragma mark - Notifications

- (void)willResignActive
{
    if (self.isPolling)
    {
        self.cancelledToBackground = YES;
        [self stopPrinterStatusPolling];
    }
}

- (void)willEnterForeground
{
    if (self.cancelledToBackground == YES)
    {
        self.cancelledToBackground = NO;
        [self startPrinterStatusPolling];
    }
}

@end
