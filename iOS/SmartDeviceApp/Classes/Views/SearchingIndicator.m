//
//  SearchingIndicator.m
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SearchingIndicator.h"
#import "UIColor+Theme.h"

@interface SearchingIndicator ()

/**
 * Flag that indicates whether the frame should be resized.
 */
@property (assign, nonatomic) BOOL invalidFrame;

@end

@implementation SearchingIndicator

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        self.invalidFrame = NO;
    }
    return self;
}

#pragma mark - Custom Drawing

- (void)setFrameIsInvalid:(BOOL)invalid
{
    self.invalidFrame = invalid;
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{    
    if (self.invalidFrame)
    {
        CGRect newRect = CGRectMake(rect.origin.x,
                                    rect.origin.y,
                                    rect.size.width,
                                    HEIGHT_WHILE_REFRESHING);
    
        NSArray* subViews = [self subviews];
        for (UIView* view in subViews)
        {
            [view setFrame:newRect];
        }
        self.invalidFrame = NO; 
        
        [super drawRect:newRect];
    }
    else
    {
        [super drawRect:rect];
    }
}

#pragma mark - Refreshing

- (void)beginRefreshing
{
    [self setNeedsDisplay];

    // adjust colors
    if (@available(iOS 13.0, *)) {
        [self setBackgroundColor:[UIColor colorNamed:@"color_gray5_gray6"]];
    } else {
        [self setBackgroundColor:[UIColor gray4ThemeColor]];
    }
    [self setTintColor:[UIColor whiteThemeColor]];

    // adjust frame if not approximately equal to the determined height while refreshing
    CGFloat diff = self.frame.size.height - HEIGHT_WHILE_REFRESHING;
    if (fabs(diff) > 0.0001)
    {
        [self setFrameIsInvalid:YES];
        [self layoutIfNeeded]; // will call drawRect:
    }
    
    [super beginRefreshing];
}

- (void)endRefreshing
{
    [self setNeedsDisplay];
    
    // adjust colors
    if (@available(iOS 13.0, *)) {
        [self setBackgroundColor:[UIColor colorNamed:@"color_gray2_gray5"]];
    } else {
        [self setBackgroundColor:[UIColor gray2ThemeColor]];
    }
    [self setTintColor:[UIColor gray2ThemeColor]];

    [super endRefreshing];
}

@end
