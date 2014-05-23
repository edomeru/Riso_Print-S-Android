//
//  DeleteButton.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DeleteButton.h"
#import "UIColor+Theme.h"

@interface DeleteButton()

@property (assign, nonatomic) BOOL stayHighlighted;

@end

@implementation DeleteButton

#pragma mark - Initialization

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

#pragma mark - UI Properties

- (void)keepHighlighted:(BOOL)enable
{
    self.stayHighlighted = enable;
}

- (void)setHighlighted:(BOOL)highlighted
{
    if (self.delegate != nil && [self.delegate respondsToSelector:@selector(shouldHighlightButton)])
    {
        if (![self.delegate shouldHighlightButton])
            return;
    }
    
    [super setHighlighted:highlighted];
    
    if (highlighted || self.stayHighlighted)
    {
        self.backgroundColor = self.highlightedColor;
        [self setTitleColor:self.highlightedTextColor forState:UIControlStateNormal];
    }
    else
    {
        self.backgroundColor = [UIColor whiteThemeColor];
        [self setTitleColor:[UIColor blackThemeColor] forState:UIControlStateNormal];
    }
}

@end
