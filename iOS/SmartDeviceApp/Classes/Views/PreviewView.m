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

/**
 Container of the preview page
 */
@property (nonatomic, strong) UIView *contentView;

/**
 Orientation mode
 */
@property (nonatomic) kPreviewViewOrientation orientation;

/**
 Aspect ratio (W:H)
 */
@property (nonatomic) CGFloat aspectRatio;

/**
 Constraint that ensures that the aspect ratio is maintained
 */
@property (nonatomic, weak) NSLayoutConstraint *aspectRatioConstraint;

/**
 Constraint that ensures that the size of the content view is based on the container + scale
 */
@property (nonatomic, weak) NSLayoutConstraint *sizeConstraint;

/**
 Constraint that ensure that the content view is centered horizontally inside the container + pan value
 */
@property (nonatomic, weak) NSLayoutConstraint *xAlignConstraint;

/**
 Constraint that ensure that the content view is centered vertically inside the container + pan value
 */
@property (nonatomic, weak) NSLayoutConstraint *yAlignConstraint;

/**
 Pinch gesture recognizer for scaling
 */
@property (nonatomic, weak) UIPinchGestureRecognizer *pincher;

/**
 Pan gesture recognizer for panning
 */
@property (nonatomic, weak) UIPanGestureRecognizer *panner;

/**
 Current scale
 */
@property (nonatomic) CGFloat scale;

/**
 Current position
 */
@property (nonatomic) CGPoint position;

/**
 Center point of the pinch gesture
 */
@property (nonatomic) CGPoint zoomAnchor;

/**
 Indicates whether or not the content view is currently being scaled
 */
@property (nonatomic) BOOL zooming;

/**
 Initializes the content view and property values
 */
- (void)initialize;

/**
 Snap dimensions and position to nearest valid value
 */
- (void)snap;

/**
 Action when content view is pinched/being pinched
 */
- (IBAction)pinchAction:(id)sender;

/**
 Action when content view is panned/being panned
 */
- (IBAction)panAction:(id)sender;

@end

@implementation PreviewView

#pragma mark - Public Methods

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
        if((self.frame.size.height * self.aspectRatio) > self.frame.size.width)// should not exceed width of parent view
        {
            sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f]; //keep the width constant
        }
        else
        {
            sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f]; //keep the height constant
        }
        aspectRatioConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeHeight multiplier:self.aspectRatio constant:0.0f];
    }
    else
    {
        if((self.frame.size.width * self.aspectRatio) > self.frame.size.height) //should not exceed height of parent view
        {
            sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f]; //keep the height constant
        }
        else
        {
            sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];//keep the width constant
        }
        
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

#pragma mark - Helper Methods

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
    //[self.contentView addGestureRecognizer:panner];
    self.pincher = pincher;
    self.panner = panner;
    
    // Initialize properties/flags
    self.scale = 1.0f;
    self.position = CGPointZero;
    self.zoomAnchor = CGPointZero;
    self.minScale = MIN_SCALE;
    self.maxScale = MAX_SCALE;
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

#pragma - UIGestureRecognizerDelegate Methods

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return NO;
}

#pragma - IBACtion Methods

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

- (void)layoutSubviews
{
    [super layoutSubviews];
    //if other layout of views the content exceeds bounds of parent view but is not zoomed, recompute constraints of view
    if((self.frame.size.height < self.contentView.frame.size.height || self.frame.size.width < self.contentView.frame.size.width) && self.scale <= 1.0f)
    {
        [self setPreviewWithOrientation:self.orientation aspectRatio:self.aspectRatio];
    }
}

@end
