//
//  PrinterStatusView.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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

- (void)setStatus:(BOOL)isOnline;
{
    if(self.onlineStatus == isOnline)
    {
        return; //do nothing if status is the same
    }
    
    self.onlineStatus = isOnline;
    if(isOnline)
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"[INFO][PSView] set to online");
#endif
        [self setHighlighted:YES];
    }
    else
    {
#if DEBUG_LOG_PRINTER_STATUS_VIEW
        NSLog(@"[INFO][PSView] set to offline");
#endif
        [self setHighlighted:NO];
    }
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
