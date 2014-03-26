//
//  MenuButton.m
//  SmartDeviceApp
//
//  Created by Seph on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "MenuButton.h"

@interface MenuButton()

@end

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
    CGContextMoveToPoint(contextRef, self.titleEdgeInsets.left, rect.origin.y + rect.size.height);
    CGContextAddLineToPoint(contextRef, rect.origin.x + rect.size.width, rect.origin.y + rect.size.height);
    [[UIColor whiteColor] setStroke];
    CGContextStrokePath(contextRef);
}

- (void)setHighlighted:(BOOL)highlighted
{
    [super setHighlighted:highlighted];
    
    if (self.isSelected)
    {
        return;
    }
    
    if (self.isHighlighted)
    {
        self.backgroundColor = self.highlightColor;
        [self setNeedsDisplay];
    }
    else
    {
        self.backgroundColor = [UIColor clearColor];
        [self setNeedsDisplay];
    }
}

- (void)setSelected:(BOOL)selected
{
    [super setSelected:selected];
    
    if (self.isSelected)
    {
        self.backgroundColor = self.selectedColor;
        [self setNeedsDisplay];
    }
    else
    {
        self.backgroundColor = [UIColor clearColor];
        [self setNeedsDisplay];
    }
}

@end