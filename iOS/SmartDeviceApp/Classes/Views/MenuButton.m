//
//  MenuButton.m
//  SmartDeviceApp
//
//  Created by Seph on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "MenuButton.h"

@implementation MenuButton

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)drawRect:(CGRect)rect
{
    if (self.borderHidden == YES)
    {
        return;
    }
    
    CGContextRef contextRef = UIGraphicsGetCurrentContext();
    
    CGContextBeginPath(contextRef);
    CGContextMoveToPoint(contextRef, self.contentEdgeInsets.left, rect.origin.y + rect.size.height);
    CGContextAddLineToPoint(contextRef, rect.origin.x + rect.size.width, rect.origin.y + rect.size.height);
    [[UIColor whiteColor] setStroke];
    CGContextStrokePath(contextRef);
}

@end
