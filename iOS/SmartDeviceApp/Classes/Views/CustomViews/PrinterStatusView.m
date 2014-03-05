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
    self.onlineStatus = isOnline;
    if(isOnline){
        [self setBackgroundColor:[UIColor greenColor]];
    }
    else{
        [self setBackgroundColor:[UIColor grayColor]];
    }
   
}

- (void) updateStatus: (BOOL) isOnline
{
    [self setStatus: isOnline];
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
