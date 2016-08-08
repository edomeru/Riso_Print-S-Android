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
 * Container of the preview page.
 * This view will be resized using the orientation, aspectRatio, scale and position values.
 *
 * @see orientation
 * @see aspectRatio
 * @see scale
 * @see position
 */
@property (nonatomic, strong) UIView *contentView;

/**
 * Orientation mode.
 * - kPreviewOrientationPortrait: The page will be displayed in Portrait mode.
 * The page will adjust its size according to its height.
 * - kPreviewOrientationLandscape: The page will be displayed in Landscape mode.
 * The page will adjust its size according to its width.
 *
 * @see aspectRatio
 */
@property (nonatomic) kPreviewViewOrientation orientation;

/**
 * Aspect ratio (W:H).
 * Used in computing the length of the page's sides based on the orientation setting.
 *
 * @see orientation
 */
@property (nonatomic) CGFloat aspectRatio;

/**
 * Constraint that ensures that the aspect ratio is maintained.
 *
 * @see aspectRatio
 */
@property (nonatomic, weak) NSLayoutConstraint *aspectRatioConstraint;

/**
 * Constraint that ensures that the size of the content view is based on the container + scale.
 *
 * @see scale
 */
@property (nonatomic, weak) NSLayoutConstraint *sizeConstraint;

/**
 * Constraint that ensure that the content view is centered horizontally inside the container + pan value.
 *
 * @see position
 */
@property (nonatomic, weak) NSLayoutConstraint *xAlignConstraint;

/**
 * Constraint that ensure that the content view is centered vertically inside the container + pan value.
 *
 * @see position
 */
@property (nonatomic, weak) NSLayoutConstraint *yAlignConstraint;

/**
 * Constraint that ensures that the width of the content view is not greater than the container.
 * This constraint is only applied when the contentView is not scaled.
 */
@property (nonatomic, weak) NSLayoutConstraint *maxWidthConstraint;

/**
 * Constraint that ensures that the height of the content view is not greater than the container.
 * This constraint is only applied when the contentView is not scaled.
 */
@property (nonatomic, weak) NSLayoutConstraint *maxHeightConstraint;

/**
 * Pinch gesture recognizer for scaling.
 *
 * @see - (IBAction)pinchAction:(id)sender;
 */
@property (nonatomic, strong) UIPinchGestureRecognizer *pincher;

/**
 * Pan gesture recognizer for panning.
 *
 * @see - (IBAction)panAction:(id)sender;
 */
@property (nonatomic, strong) UIPanGestureRecognizer *panner;

/**
 * Double tap gesture recognizer for zooming out.
 *
 * @see - (IBAction)tapAction:(id)sender;
 */
@property (nonatomic, strong) UITapGestureRecognizer *tapper;

/**
 * Current scale.
 * Used in sizeConstraint. Allowed values are determined by minScale and maxScale.
 *
 * @see sizeConstraint
 * @see maxScale
 * @see minScale
 */
@property (nonatomic) CGFloat scale;

/**
 * Current position.
 * Used in xAlignConstraint and yAlignConstraint
 *
 * @see xAlignConstraint
 * @see yAlignConstraint
 */
@property (nonatomic) CGPoint position;

/**
 * Center point of the pinch gesture.
 * Used for determing the center of zooming.
 */
@property (nonatomic) CGPoint zoomAnchor;

/**
 * Indicates whether or not the content view is currently being scaled.
 */
@property (nonatomic) BOOL zooming;

/**
 * Size difference of the container and contentView.
 */
@property (nonatomic) CGFloat sizeOffset;

/**
 * Initializes the content view and property values.
 */
- (void)initialize;

/**
 * Adds the pageContentView view to the contentView.
 *
 * @see contentView
 */
- (void)setPageContentView:(UIView *)pageContentView;

/**
 * Snaps dimensions and position to nearest valid value.
 * Used in enforcing the minScale, maxScale and center-aligning the contentView.
 */
- (void)snap;

/**
 * Enables scale to fit constraints.
 * Enables maxWidthConstraint and maxHeightConstraint.
 *
 * @see maxWidthConstraint
 * @see maxHeightConstraint
 */
- (void)enableMaxDimensionRules;

/*
 * Disables scale to fit constraints.
 * Disables maxWidthConstraint and maxHeightConstraint.
 *
 * @see maxWidthConstraint
 * @see maxHeightConstraint
 */
- (void)disableMaxDimensionRules;

/**
 * Handles pinch gesture.
 * Performs zooming action.
 *
 * @param sender Gesture recognizer that triggered the action
 */
- (IBAction)pinchAction:(id)sender;

/**
 * Handles pan gesture.
 * Performs panning action.
 *
 * @param sender Gesture recognizer that triggered the action
 */
- (IBAction)panAction:(id)sender;

/**
 * Handles double-tap gesture.
 * Performs zooming-out action (scale of 1.0)
 *
 * @param sender Gesture recognizer that triggered the action
 */
- (IBAction)tapAction:(id)sender;

/**
 * Adjust the position of the preview with respect to the center
 * of the preview screen based on the maximum pan positions.
 *
 * @param position The CGpoint position to adjust
 * @return the adjusted position
 */
- (CGPoint)adjustPreviewPosition:(CGPoint)position;

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
    [self removeConstraint:self.sizeConstraint];
    [self removeConstraint:self.xAlignConstraint];
    [self removeConstraint:self.yAlignConstraint];
    [self removeConstraint:self.maxWidthConstraint];
    [self removeConstraint:self.maxHeightConstraint];
    [self.contentView removeFromSuperview];
    
    // Create content view
    self.contentView = [[UIView alloc] init];
    self.contentView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.contentView addGestureRecognizer:self.panner];
    [self.contentView addGestureRecognizer:self.pincher];
    [self.contentView addGestureRecognizer:self.tapper];
    [self addSubview:self.contentView];
    
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
    
    [self setupPageContentView];
    
    [self layoutIfNeeded];
}

- (void)setPageContentView:(UIView *)pageContentView
{
    if (_pageContentView != nil)
    {
        [_pageContentView removeFromSuperview];
    }
    
    _pageContentView = pageContentView;
    [self setupPageContentView];
}

- (void)adjustPannedPosition
{
    self.position = [self adjustPreviewPosition:self.position];
}

#pragma mark - Helper Methods

- (void)initialize
{
    
    // Create gesture recognizers
    UIPinchGestureRecognizer *pincher = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(pinchAction:)];
    UIPanGestureRecognizer *panner = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panAction:)];
    UITapGestureRecognizer *tapper = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapAction:)];
    pincher.delegate = self;
    panner.delegate = self;
    tapper.delegate = self;
    tapper.numberOfTapsRequired = 2; // Double tap
    [self.contentView addGestureRecognizer:pincher];
    [self.contentView addGestureRecognizer:panner];
    [self.contentView addGestureRecognizer:tapper];
    self.pincher = pincher;
    self.panner = panner;
    self.tapper = tapper;
    
    // Initialize properties/flags
    self.scale = 1.0f;
    self.position = CGPointZero;
    self.zoomAnchor = CGPointZero;
    self.minScale = MIN_SCALE;
    self.maxScale = MAX_SCALE;
}

- (void)setupPageContentView
{
    if (self.pageContentView == nil)
    {
        return;
    }
    
    [self.contentView addSubview:self.pageContentView];
    
    NSDictionary *views = @{@"pageContentView": self.pageContentView};
    [self.contentView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[pageContentView]|" options:0 metrics:nil views:views]];
    [self.contentView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[pageContentView]|" options:0 metrics:nil views:views]];
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
        self.sizeOffset = 0.0f;
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
    // Disable simultaneous gesture only for preview view's gesture recognizer
    if (([otherGestureRecognizer isEqual:self.pincher] || [otherGestureRecognizer isEqual:self.panner] || [otherGestureRecognizer isEqual:self.tapper]) &&
        ([gestureRecognizer isEqual:self.pincher] || [gestureRecognizer isEqual:self.panner] || [gestureRecognizer isEqual:self.tapper]))
    {
        return NO;
    }
    return YES;
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
    
    CGFloat scale = MAX(0.5f, self.scale * pincher.scale); // Added to avoid scaling to very low factor
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
    
    position = [self adjustPreviewPosition:position];
    
    if (panner.state == UIGestureRecognizerStateEnded)
    {
        self.position = position;
        //20160624 - BTS 20133: Removed snap. This causes view to resize when panned after zooming and rotation
        //[self snap];
    }
}

- (IBAction)tapAction:(id)sender
{
    if (self.scale > 1.0f)
    {
        self.scale = 1.0f;
        self.position = CGPointZero;
        self.xAlignConstraint.constant = 0.0f;
        self.yAlignConstraint.constant = 0.0f;
        self.sizeConstraint.constant = 0.0f;
        [self enableMaxDimensionRules];
        
        [UIView animateWithDuration:0.2f animations:^{
            [self layoutIfNeeded];
        }completion:^(BOOL finished){
            [self.delegate previewView:self didChangeZoomMode:(self.scale > 1.0f)];
        }];
    }
}

- (CGPoint)adjustPreviewPosition:(CGPoint)position
{
    if (self.scale > 1.0f)
    {
        CGSize containerSize = self.frame.size;
        CGSize contentSize = self.contentView.frame.size;
    
        //do not allow pan if the content size is less than the container size
        if (contentSize.width <  containerSize.width)
        {
            position.x = 0.0f;
        }
        
        if (contentSize.height <  containerSize.height)
        {
            position.y = 0.0f;
        }
        
        //allow position only up to the point where the invisible parts of the content is visible in the container
        // maximum movement is only up to the difference of the the container size and the content size
        // divided by 2 for for distance going to the left/right or t
        CGFloat maxX = fabs(contentSize.width - containerSize.width) / 2.0f;
        CGFloat maxY = fabs(contentSize.height - containerSize.height) / 2.0f;
        
        if(fabs(position.x) > maxX)
        {
            if(position.x < 0.0f)
            {
                position.x = maxX * -1.0f;
            }
            else
            {
                position.x = maxX;
            }
        }
        
        if(fabs(position.y) > maxY)
        {
            if(position.y < 0.0f)
            {
                position.y = maxY * -1.0f;
            }
            else
            {
                position.y = maxY;
            }
        }
        
        //if invisible page to the left or top is shown in the content, do not allow the non-invisible
        //part to be panned out of the screen by allowing panning to the right or up to the point where the content view is centered in the container
        if (self.isLeftBookendShown)
        {
            if (contentSize.width > contentSize.height && position.x > 0.0f)
            {
                position.x = 0.0f;
            }
            
            if (contentSize.width < contentSize.height && position.y > 0.0f)
            {
                position.y = 0.0f;
            }
        }
        
        //if invisible page to the right  or bottom is shown in the content, do not allow the non-invisible
        //part to be panned out of the screen by allowing panning to the left or down to the point where the content view is centered in the container
        if (self.isRightBookendShown)
        {
            if (contentSize.width > contentSize.height && position.x < 0.0f)
            {
                position.x = 0.0f;
            }
            
            if (contentSize.width < contentSize.height && position.y < 0.0f)
            {
                position.y = 0.0f;
            }
        }
        
        self.xAlignConstraint.constant = position.x;
        self.yAlignConstraint.constant = position.y;
        [self layoutIfNeeded];
    }
    
    return position;
}
@end
