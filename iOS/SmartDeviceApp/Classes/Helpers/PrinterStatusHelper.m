//
//  PrinterStatusHelper.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterStatusHelper.h"
#import "SNMPManager.h"

@interface PrinterStatusHelper ()

@property (nonatomic) NSTimer *pollingTimer;
@property (nonatomic) NSString *notifName;

-(void) getPrinterStatus;
@end

@implementation PrinterStatusHelper

-(id) initWithPrinterIP:(NSString *) ipAddress
{
    self = [super init];
    
    if(self != nil)
    {
        self.ipAddress = [NSString stringWithString:ipAddress];
    }
    
    return self;
}

-(void) getPrinterStatus
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        BOOL onlineStatus = [SNMPManager getPrinterStatus:self.ipAddress];
        //notification should be on main queue to update the UI
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate statusDidChange:onlineStatus];
        });
    });
}

-(void) startPrinterStatusPolling
{
    if(self.pollingTimer == nil)
    {
        self.pollingTimer = [NSTimer scheduledTimerWithTimeInterval:5 target: self selector: @selector(getPrinterStatus) userInfo:nil repeats:YES];
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
