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


- (void) setStatus: (BOOL) isOnline
{
    if(self.onlineStatus == isOnline)
    {
        return; //do nothing if status is the same
    }
    
    //TODO Refine Printer Status View
    self.onlineStatus = isOnline;
    if(isOnline){
        //NSLog(@"Set to online");
        [self setBackgroundColor:[UIColor greenColor]];
    }
    else{
        //NSLog(@"Set to offline");
        [self setBackgroundColor:[UIColor grayColor]];
    }
}

- (void) updateStatus: (BOOL) isOnline
{
    //NSLog(@"%@ online status = %d", self.statusHelper.ipAddress, isOnline);
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
