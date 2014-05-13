//
//  CustomSegmentedControl.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "CustomSegmentedControl.h"
#import "UIColor+Theme.h"

#define DEFAULT_CORNER_RADIUS 10

@interface CustomSegmentedControl()

@property (nonatomic, strong) UIColor *defaultTintColor;
@property (nonatomic, strong) UIColor *defaultDisabledColor;
@property (nonatomic, strong) UIColor *defaultHighlightTextColor;
@property (nonatomic, assign) CGRect bgImageRect;
@property (nonatomic, assign) CGRect bgInsetRect;
@property (nonatomic, assign) CGRect dividerImageRect;
@property (nonatomic, assign) CGRect dividerInsetRect;
@property (nonatomic, assign) CGFloat scale;

- (void)initialize;
- (UIImage *)createSelectedImage;
- (UIImage *)createNormalImage;
- (UIImage *)createDividerLeftImage;
- (UIImage *)createDividerRightImage;

@end

@implementation CustomSegmentedControl

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self initialize];
    }
    return self;
}

- (id)initWithItems:(NSArray *)items
{
    self = [super initWithItems:items];
    if (self) {
        [self initialize];
    }
    
    return self;
}

- (void)initialize
{
    self.scale = [[UIScreen mainScreen] scale];
    self.defaultTintColor = [UIColor purple2ThemeColor];
    self.defaultHighlightTextColor = [UIColor whiteThemeColor];
    self.defaultDisabledColor = [UIColor gray4ThemeColor];
    self.bgImageRect = CGRectMake(0.0f, 0.0f, (DEFAULT_CORNER_RADIUS * 2 + 1) * self.scale,  self.frame.size.height * self.scale);
    self.bgInsetRect = CGRectInset(self.bgImageRect, floorf(self.scale / 2.0f), floorf(self.scale / 2.0f));
    self.dividerImageRect = CGRectMake(0.0f, 0.0f, self.scale * 2.0f, self.frame.size.height * self.scale);
    self.dividerInsetRect = CGRectInset(self.dividerImageRect, floorf(self.scale / 2.0f), floorf(self.scale / 2.0f));
    
    UIImage *selectedBgImage = [self createSelectedImage];
    UIImage *normalBgImage = [self createNormalImage];
    UIImage *dividerLeftImage = [self createDividerLeftImage];
    UIImage *dividerRightImage = [self createDividerRightImage];
    
    // Assign images
    
    // Left is selected
    [self setDividerImage:dividerLeftImage forLeftSegmentState:UIControlStateSelected rightSegmentState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerLeftImage forLeftSegmentState:UIControlStateSelected rightSegmentState:UIControlStateHighlighted barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerLeftImage forLeftSegmentState:UIControlStateSelected rightSegmentState:UIControlStateDisabled barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerLeftImage forLeftSegmentState:UIControlStateHighlighted rightSegmentState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
    
    // Right is selected
    [self setDividerImage:dividerRightImage forLeftSegmentState:UIControlStateNormal rightSegmentState:UIControlStateSelected barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerRightImage forLeftSegmentState:UIControlStateHighlighted rightSegmentState:UIControlStateSelected barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerRightImage forLeftSegmentState:UIControlStateDisabled rightSegmentState:UIControlStateSelected barMetrics:UIBarMetricsDefault];
    [self setDividerImage:dividerRightImage forLeftSegmentState:UIControlStateNormal rightSegmentState:UIControlStateHighlighted barMetrics:UIBarMetricsDefault];
    
    // BG images
    [self setBackgroundImage:normalBgImage forState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
    [self setBackgroundImage:selectedBgImage forState:UIControlStateSelected barMetrics:UIBarMetricsDefault];
    [self setBackgroundImage:selectedBgImage forState:UIControlStateHighlighted barMetrics:UIBarMetricsDefault];
    
    // Font colors
    [self setTitleTextAttributes:@{NSForegroundColorAttributeName: self.defaultTintColor} forState:UIControlStateNormal];
    [self setTitleTextAttributes:@{NSForegroundColorAttributeName: self.defaultHighlightTextColor} forState:UIControlStateHighlighted];
    [self setTitleTextAttributes:@{NSForegroundColorAttributeName: self.defaultHighlightTextColor} forState:UIControlStateSelected];
    [self setTitleTextAttributes:@{NSForegroundColorAttributeName: self.defaultDisabledColor} forState:UIControlStateDisabled];
    
    [self setTitleTextAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:15.0f]} forState:UIControlStateNormal];
    [self setTitleTextAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:15.0f]} forState:UIControlStateHighlighted];
    [self setTitleTextAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:15.0f]} forState:UIControlStateSelected];
    [self setTitleTextAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:15.0f]} forState:UIControlStateDisabled];
}

- (UIImage *)createSelectedImage
{
    UIGraphicsBeginImageContext(self.bgImageRect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Draw rounded rect (fill only)
    UIBezierPath *rounderRectPath = [UIBezierPath bezierPathWithRoundedRect:self.bgImageRect cornerRadius:DEFAULT_CORNER_RADIUS * self.scale];
    CGContextSetFillColorWithColor(context, [self.defaultTintColor CGColor]);
    [rounderRectPath fill];
    
    CGImageRef cgImage = CGBitmapContextCreateImage(context);
    UIImage *image = [UIImage imageWithCGImage:cgImage scale:self.scale orientation:UIImageOrientationUp];
    CGImageRelease(cgImage);
    UIGraphicsEndImageContext();
    return image;
}

- (UIImage *)createNormalImage
{
    UIGraphicsBeginImageContext(self.bgImageRect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Draw rounded rect (outline only)
    UIBezierPath *rounderRectPath = [UIBezierPath bezierPathWithRoundedRect:self.bgInsetRect cornerRadius:DEFAULT_CORNER_RADIUS * self.scale];
    CGContextSetStrokeColorWithColor(context, [self.defaultTintColor CGColor]);
    [rounderRectPath setLineWidth:self.scale];
    [rounderRectPath stroke];
    
    CGImageRef cgImage = CGBitmapContextCreateImage(context);
    UIImage *image = [UIImage imageWithCGImage:cgImage scale:self.scale orientation:UIImageOrientationUp];
    CGImageRelease(cgImage);
    UIGraphicsEndImageContext();
    return image;
}

- (UIImage *)createDividerRightImage
{
    UIGraphicsBeginImageContext(self.dividerImageRect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Draw incomplete rectangle
    CGContextMoveToPoint(context, self.dividerImageRect.origin.x, self.dividerInsetRect.origin.y);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x + self.dividerImageRect.size.width, self.dividerInsetRect.origin.y);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x + self.dividerImageRect.size.width, self.dividerInsetRect.origin.y + self.dividerInsetRect.size.height);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x, self.dividerInsetRect.origin.y + self.dividerInsetRect.size.height);
    CGContextSetStrokeColorWithColor(context, [self.defaultTintColor CGColor]);
    CGContextSetLineWidth(context, self.scale);
    CGContextStrokePath(context);
    
    CGImageRef cgImage = CGBitmapContextCreateImage(context);
    UIImage *image = [UIImage imageWithCGImage:cgImage scale:self.scale orientation:UIImageOrientationUp];
    CGImageRelease(cgImage);
    UIGraphicsEndImageContext();
    return image;
}

- (UIImage *)createDividerLeftImage
{
    UIGraphicsBeginImageContext(self.dividerImageRect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Draw incomplete rectangle
    CGContextMoveToPoint(context, self.dividerImageRect.origin.x + self.dividerImageRect.size.width, self.dividerInsetRect.origin.y);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x, self.dividerInsetRect.origin.y);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x, self.dividerInsetRect.origin.y + self.dividerInsetRect.size.height);
    CGContextAddLineToPoint(context, self.dividerImageRect.origin.x + self.dividerImageRect.size.width, self.dividerInsetRect.origin.y + self.dividerInsetRect.size.height);
    CGContextSetStrokeColorWithColor(context, [self.defaultTintColor CGColor]);
    CGContextSetLineWidth(context, self.scale);
    CGContextStrokePath(context);
    
    CGImageRef cgImage = CGBitmapContextCreateImage(context);
    UIImage *image = [UIImage imageWithCGImage:cgImage scale:self.scale orientation:UIImageOrientationUp];
    CGImageRelease(cgImage);
    UIGraphicsEndImageContext();
    return image;
}

@end
