//
//  PrinterStatusView.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterStatusView.h"

@implementation PrinterStatusView 

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        
    }
    return self;
}

- (void)dealloc
{
    [self.statusHelper stopPrinterStatusPolling];
}

- (void) setStatus: (BOOL) isOnline
{
    if(self.onlineStatus == isOnline)
    {
        return; //do nothing if status is the same
    }
    
    //TODO Refine Printer Status View
    self.onlineStatus = isOnline;
    if(isOnline)
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"Set to online");
#endif
        [self setHighlighted:YES];
    }
    else
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"Set to offline");
#endif
        [self setHighlighted:NO];
    }
}

- (void) statusDidChange: (BOOL) isOnline
{
#if DEBUG_LOG_PRINTER_STATUS_VIEW
    NSString* onlineStatus = isOnline ? @"YES" : @"NO";
    NSLog(@"%@ online status = %@", self.statusHelper.ipAddress, onlineStatus);
#endif
    [self setStatus:isOnline];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
