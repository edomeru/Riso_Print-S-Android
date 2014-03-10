//
//  MenuScrollView.m
//  SmartDeviceApp
//
//  Created by Seph on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "MenuScrollView.h"

@implementation MenuScrollView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.delaysContentTouches = NO;
    }
    return self;
}

- (BOOL)touchesShouldCancelInContentView:(UIView *)view
{
    if ([view isKindOfClass:UIButton.class])
    {
        return YES;
    }
    
    return [super touchesShouldCancelInContentView:view];
}

@end
