//
//  PreviewView.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
 Constraint that ensures that the width of the content view is not greater than the container
 */
@property (nonatomic, weak) NSLayoutConstraint *maxWidthConstraint;

/**
 Constraint that ensures that the height of the content view is not greater than the container
 */
@property (nonatomic, weak) NSLayoutConstraint *maxHeightConstraint;

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

@property (nonatomic) CGFloat sizeOffset;

/**
 Initializes the content view and property values
 */
- (void)initialize;

/**
 Snap dimensions and position to nearest valid value
 */
- (void)snap;

- (void)enableMaxDimensionRules;
- (void)disableMaxDimensionRules;

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
    [self removeConstraint:self.maxWidthConstraint];
    [self removeConstraint:self.maxHeightConstraint];
    
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
    
    NSLayoutConstraint *maxWidthConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationLessThanOrEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *maxHeightConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationLessThanOrEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f];
                                               
    // Add new constraints
    //[self addSubview:self.contentView];
    sizeConstraint.priority = UILayoutPriorityDefaultLow;
    [self.contentView addConstraint:aspectRatioConstraint];
    [self addConstraints:@[sizeConstraint, xAlignConstraint, yAlignConstraint, maxWidthConstraint, maxHeightConstraint]];
    
    // Save references
    self.aspectRatioConstraint = aspectRatioConstraint;
    self.sizeConstraint = sizeConstraint;
    self.xAlignConstraint = xAlignConstraint;
    self.yAlignConstraint = yAlignConstraint;
    self.maxWidthConstraint = maxWidthConstraint;
    self.maxHeightConstraint = maxHeightConstraint;
    
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
    
    if (self.scale == 1.0f)
    {
        [self enableMaxDimensionRules];
    }
    
    self.sizeConstraint.constant = referenceSize * (self.scale - 1.0f) + self.sizeOffset;
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

- (void)enableMaxDimensionRules
{
    [self removeConstraint:self.sizeConstraint];
    
    NSLayoutConstraint *sizeConstraint;
    if (self.orientation == kPreviewViewOrientationPortrait)
    {
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f];
    }
    else
    {
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];
    }
    sizeConstraint.priority = UILayoutPriorityDefaultLow;
    
    NSLayoutConstraint *maxWidthConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationLessThanOrEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *maxHeightConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationLessThanOrEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f];
    
    [self addConstraints:@[sizeConstraint, maxWidthConstraint, maxHeightConstraint]];
    
    self.sizeConstraint = sizeConstraint;
    self.maxWidthConstraint = maxWidthConstraint;
    self.maxHeightConstraint = maxHeightConstraint;
}


- (void)disableMaxDimensionRules
{
    [self removeConstraint:self.sizeConstraint];
    [self removeConstraint:self.maxWidthConstraint];
    [self removeConstraint:self.maxHeightConstraint];
    
    NSLayoutConstraint *sizeConstraint;
    if (self.orientation == kPreviewViewOrientationPortrait)
    {
        //CGFloat difference =
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0f constant:0.0f];
    }
    else
    {
        sizeConstraint = [NSLayoutConstraint constraintWithItem:self.contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0f constant:0.0f];
    }
    
    sizeConstraint.priority = UILayoutPriorityRequired;
    [self addConstraint:sizeConstraint];
    
    self.sizeConstraint = sizeConstraint;
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
        if (self.scale == 1.0f)
        {
            CGRect containerFrame = self.frame;
            CGRect contentFrame = self.contentView.frame;
            if (self.orientation == kPreviewViewOrientationPortrait)
            {
                self.sizeOffset = contentFrame.size.height - containerFrame.size.height;
            }
            else
            {
                self.sizeOffset = contentFrame.size.width - containerFrame.size.width;
            }
            [self disableMaxDimensionRules];
            [self layoutIfNeeded];
        }
        // Center of pinch (relative to center of the container
        CGPoint contentMid = CGPointMake(self.frame.size.width / 2.0f, self.frame.size.height / 2.0f);
        CGPoint contentPinchMid = [pincher locationInView:self];
        self.zoomAnchor = CGPointMake(-(contentPinchMid.x - contentMid.x), -(contentPinchMid.y - contentMid.y));
        
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
    self.sizeConstraint.constant = referenceSize * scale - referenceSize + self.sizeOffset;
    [self layoutIfNeeded];
    
    CGPoint scaledAnchor = CGPointMake(self.zoomAnchor.x * pincher.scale, self.zoomAnchor.y * pincher.scale);
    CGPoint adjustedAnchor = CGPointMake(scaledAnchor.x - self.zoomAnchor.x, scaledAnchor.y - self.zoomAnchor.y);
    CGPoint newPosition = CGPointMake(adjustedAnchor.x, adjustedAnchor.y);
    self.xAlignConstraint.constant = newPosition.x;
    self.yAlignConstraint.constant = newPosition.y;
    
    [self layoutIfNeeded];
    
    if (pincher.state == UIGestureRecognizerStateEnded)
    {
        self.scale = scale;
        self.position = newPosition;
        self.zooming = NO;
        self.zoomAnchor = CGPointZero;
        
        [self.delegate previewView:self didChangeZoomMode:(self.scale > 1.0f)];
        
        [self snap];
        self.sizeOffset = 0.0f;
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

@end
