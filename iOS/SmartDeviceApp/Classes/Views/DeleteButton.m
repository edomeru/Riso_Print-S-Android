//
//  DeleteButton.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DeleteButton.h"
#import "UIColor+Theme.h"

const float ANIMATION_SPEED = 0.1f;

@interface DeleteButton()

@property (assign, nonatomic) CGRect offscreenPos;
@property (assign, nonatomic) CGRect onscreenPos;
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

- (id)initAtOffscreenPos:(CGRect)offscreen withOnscreenPos:(CGRect)onscreen
{
    self = [super initWithFrame:offscreen];
    if (self)
    {
        [self setTitle:[NSLocalizedString(IDS_LBL_DELETE, @"Delete") uppercaseString] forState:UIControlStateNormal];
        [self setTitleColor:[UIColor blackThemeColor] forState:UIControlStateNormal];
        [self setTitleEdgeInsets:UIEdgeInsetsMake(10.0f,    //T
                                                  15.0f,    //L
                                                  10.0f,    //B
                                                  15.0f)];  //R
        self.titleLabel.font = [UIFont systemFontOfSize:13.0f];
        [self setBackgroundColor:[UIColor whiteThemeColor]];
        [self setUserInteractionEnabled:YES];
        
        self.highlightedColor = [UIColor purple1ThemeColor];
        self.highlightedTextColor = [UIColor whiteThemeColor];
        self.offscreenPos = offscreen;
        self.onscreenPos = onscreen;
        self.stayHighlighted = NO;
        
        self.delegate = nil;
    }
    return self;
}

+ (id)createAtOffscreenPosition:(CGRect)offscreen withOnscreenPosition:(CGRect)onscreen
{
    return [[self alloc] initAtOffscreenPos:offscreen withOnscreenPos:onscreen];
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

#pragma mark - Animation

- (void)animateOnscreen:(void (^)(BOOL))completion
{
    [UIView animateWithDuration:ANIMATION_SPEED animations:^{
        
        self.frame = self.onscreenPos;
        
    } completion:completion];
}

- (void)animateOffscreen:(void (^)(BOOL))completion
{
    [UIView animateWithDuration:ANIMATION_SPEED animations:^{
        
         self.frame = self.offscreenPos;
        
    } completion:completion];
}

@end
