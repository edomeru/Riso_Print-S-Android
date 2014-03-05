//
//  PrinterStatusHelper.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterStatusHelper.h"
#import "SNMPManager.h"

#define BASE_NOTIF_NAME @"jp.alink-group_printerstatus"

@interface PrinterStatusHelper ()
@property NSString *ipAddress;
@property NSTimer *pollingTimer;
@property NSString *notifName;

-(void) getPrinterStatus;
-(void) notifyPrinterStatus:(NSNotification *)notif;
@end

@implementation PrinterStatusHelper

-(id) initWithPrinterIP:(NSString *) ipAddress
{
    self = [super init];
    
    if(self != nil)
    {
        self.ipAddress = [NSString stringWithString:ipAddress];
        self.notifName = [NSString stringWithFormat:@"%@-%@", BASE_NOTIF_NAME, self.ipAddress];
    }
    
    return self;
}

-(void) getPrinterStatus
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        BOOL onlineStatus = [SNMPManager getPrinterStatus:self.ipAddress];
        [[NSNotificationCenter defaultCenter] postNotificationName:self.notifName object: [NSNumber numberWithBool:onlineStatus]];
    });
}

-(void) notifyPrinterStatus:(NSNotification *)notif
{
    BOOL isOnline = [(NSNumber *)[notif object] boolValue];
    [self.delegate updateStatus:isOnline];
}

-(void) startPrinterStatusPolling
{
    if(self.pollingTimer == nil)
    {
        self.pollingTimer = [NSTimer scheduledTimerWithTimeInterval:0.5 target: self selector: @selector(getPrinterStatus) userInfo:nil repeats:YES];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notifyPrinterStatus:) name:self.notifName object:nil];
    }
}

-(void)stopPrinterStatusPolling
{
    if(self.pollingTimer != nil)
    {
        [self.pollingTimer invalidate];
        self.pollingTimer = nil;
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
}

-(void)dealloc
{
    [self stopPrinterStatusPolling];
}
@end
