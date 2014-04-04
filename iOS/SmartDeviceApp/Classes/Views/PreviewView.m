//
//  PreviewView.m
//  SmartDeviceApp
//
//  Created by Seph on 4/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PreviewView.h"

#define MAX_SCALE 4.0f
#define MIN_SCALE 1.0f

@interface PreviewView ()

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic) kPreviewViewOrientation orientation;
@property (nonatomic) CGFloat aspectRatio;
@property (nonatomic, weak) NSLayoutConstraint *aspectRatioConstraint;
@property (nonatomic, weak) NSLayoutConstraint *sizeConstraint;
@property (nonatomic, weak) NSLayoutConstraint *xAlignConstraint;
@property (nonatomic, weak) NSLayoutConstraint *yAlignConstraint;
@property (nonatomic, weak) UIPinchGestureRecognizer *pincher;
@property (nonatomic, weak) UIPanGestureRecognizer *panner;

@property (nonatomic) CGFloat scale;
@property (nonatomic) CGPoint position;
@property (nonatomic) CGPoint zoomAnchor;
@property (nonatomic) BOOL zooming;

- (void)initialize;

- (IBAction)pinchAction:(id)sender;
- (IBAction)panAction:(id)sender;
- (void)snap;

@end

@implementation PreviewView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)dealloc
{
}

- (void)initialize
{
    // Create content view
    self.contentView = [[UIView alloc] init];
    self.contentView.translatesAutoresizingMaskIntoConstraints = NO;
    [self addSubview:self.contentView];
    
    // Create gesture recognizers
    UIPinchGestureRecognizer *pincher = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(pinchAction:)];
    UIPanGestureRecognizer *panner = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panAction:)];
    pincher.delegate = self;
    panner.delegate = self;
    [self.contentView addGestureRecognizer:pincher];
    [self.contentView addGestureRecognizer:panner];
    self.pincher = pincher;
    self.panner = panner;
    
    // Initialize properties/flags
    self.scale = 1.0f;
    self.position = CGPointZero;
    self.zoomAnchor = CGPointZero;
    self.minScale = MIN_SCALE;
    self.maxScale = MAX_SCALE;
}

- (void)setPreviewWithOrientation:(kPreviewViewOrientation)orientation aspectRatio:(CGFloat)ratio
{
    // Save parameters
    self.orientation = orientation;
    self.aspectRatio = ratio;
    
    // Remove existing constraints
    //[self.contentView removeFromSuperview];
    [self.contentView removeConstraint:self.aspectRatioConstraint];
    [self removeConstraint:self.sizeConstraint];
    [self removeConstraint:self.xAlignConstraint];
    [self removeConstraint:self.yAlignConstraint];
    
    // Create new constraints
    // Size constraint
    NSLayoutConstraint *sizeConstraint;
    NSLayoutConstraint *aspectRatioConstraint;
    if (orientation == kPreviewViewOrientationPortrait)
    {
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f];
        aspectRatioConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeHeight multiplier:self.aspectRatio constant:0.0f];
    }
    else
    {
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];
        aspectRatioConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeWidth multiplier:self.aspectRatio constant:0.0f];
    }
    
    NSLayoutConstraint *xAlignConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *yAlignConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0f constant:0.0f];
    
    // Add new constraints
    //[self addSubview:self.contentView];
    [self.contentView addConstraint:aspectRatioConstraint];
    [self addConstraints:@[sizeConstraint, xAlignConstraint, yAlignConstraint]];
    
    // Save references
    self.aspectRatioConstraint = aspectRatioConstraint;
    self.sizeConstraint = sizeConstraint;
    self.xAlignConstraint = xAlignConstraint;
    self.yAlignConstraint = yAlignConstraint;
    
    // Reset flags
    self.scale = 1.0f;
    self.position = CGPointZero;
    self.zoomAnchor = CGPointZero;
    
    [self layoutIfNeeded];
}

#pragma - UIGestureRecognizerDelegate

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return NO;
}

#pragma - Actions

- (IBAction)pinchAction:(id)sender
{
    UIPinchGestureRecognizer *pincher = sender;
    
    if (pincher.state == UIGestureRecognizerStateBegan)
    {
        self.zooming = YES;
        [self.delegate previewView:self didChangeZoomMode:YES];
    }
    
    CGFloat referenceSize;
    if (self.orientation == kPreviewViewOrientationPortrait)
    {
        referenceSize = self.frame.size.height;
    }
    else
    {
        referenceSize = self.frame.size.width;
    }
    CGFloat scale = self.scale * pincher.scale;
    self.sizeConstraint.constant =referenceSize * scale - referenceSize;
    [self layoutIfNeeded];
    
    if (pincher.state == UIGestureRecognizerStateBegan)
    {
        CGPoint position = [pincher locationInView:self.contentView];
        CGSize contentSize = self.contentView.frame.size;
        CGPoint center = CGPointMake(contentSize.width / 2.0f, contentSize.height / 2.0f);
        
        self.zoomAnchor = CGPointMake((center.x - position.x) / 2.0f, (center.y - position.y) / 2.0f);
    }
    CGPoint zoomAnchor = CGPointMake(self.zoomAnchor.x * pincher.scale, self.zoomAnchor.y * pincher.scale);
    CGPoint position = CGPointMake(self.position.x + zoomAnchor.x - self.zoomAnchor.x, self.position.y + zoomAnchor.y - self.zoomAnchor.y);
    self.xAlignConstraint.constant = position.x;
    self.yAlignConstraint.constant = position.y;
    
    [self layoutIfNeeded];
    
    if (pincher.state == UIGestureRecognizerStateEnded)
    {
        self.scale = scale;
        self.position = position;
        self.zooming = NO;
        self.zoomAnchor = CGPointZero;
        
        [self.delegate previewView:self didChangeZoomMode:(self.scale > 1.0f)];
        
        [self snap];
    }
}

- (IBAction)panAction:(id)sender
{
    UIPanGestureRecognizer *panner = sender;
    
    // Ignore panning when zoom = 1.0 and is not currently zooming
    if (self.sizeConstraint.constant <= 0 && !self.zooming)
    {
        return;
    }
    
    CGPoint translationInView = [panner translationInView:self];
    CGPoint position = CGPointMake(self.position.x + translationInView.x, self.position.y + translationInView.y);
    self.xAlignConstraint.constant = position.x;
    self.yAlignConstraint.constant = position.y;
    [self layoutIfNeeded];
    
    if (panner.state == UIGestureRecognizerStateEnded)
    {
        self.position = position;
        [self snap];
    }
}

- (void)snap
{
    // Snap scale
    self.scale = MIN(MAX(self.scale, self.minScale), self.maxScale);
    CGFloat referenceSize;
    if (self.orientation == kPreviewViewOrientationPortrait)
    {
        referenceSize = self.frame.size.height;
    }
    else
    {
        referenceSize = self.frame.size.width;
    }
    self.sizeConstraint.constant = referenceSize * (self.scale - 1.0f);
    [UIView animateWithDuration:0.3f delay:0.0f options:UIViewAnimationOptionCurveEaseOut animations:^
     {
         [self layoutIfNeeded];
         // Snap position
         CGPoint position = self.position;
         CGSize containerSize = self.frame.size;
         CGSize contentSize = self.contentView.frame.size;
         // Snap left/right sides to container
         if (contentSize.width >= containerSize.width)
         {
             CGFloat maxX = (contentSize.width - containerSize.width) / 2.0f;
             position.x = MIN(MAX(position.x, -maxX), maxX);
         }
         else
         {
             position.x = 0.0f;
         }
         // Snap top/bottom sides to container
         if (contentSize.height >= containerSize.height)
         {
             CGFloat maxY = (contentSize.height - containerSize.height) / 2.0f;
             position.y = MIN(MAX(position.y, -maxY), maxY);
         }
         else
         {
             position.y = 0.0f;
         }
         self.position = position;
         self.xAlignConstraint.constant = position.x;
         self.yAlignConstraint.constant = position.y;
         
         [self layoutIfNeeded];
     } completion:nil];
}

@end
