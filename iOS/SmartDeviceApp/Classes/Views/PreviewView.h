//
//  PreviewView.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PreviewViewDelegate;

@interface PreviewView : UIView<UIGestureRecognizerDelegate>

/**
 Orientation Modes
 */
typedef enum
{
    /**
     Portrait
     */
    kPreviewViewOrientationPortrait,
    
    /**
     Landscape
     */
    kPreviewViewOrientationLandscape
} kPreviewViewOrientation;

/**
 * Page content view
 */
@property (nonatomic, strong) UIView *pageContentView;

/**
 Orientation mode
 */
@property (nonatomic, readonly) kPreviewViewOrientation orientation;

/**
 Aspect ratio (W:H)
 */
@property (nonatomic, readonly) CGFloat aspectRatio;

/**
 Maximum allowed scale value
 */
@property (nonatomic) CGFloat maxScale;

/**
 Minimum allowed scale value
 */
@property (nonatomic) CGFloat minScale;

/**
 Delegate
 */
@property (nonatomic, weak) id<PreviewViewDelegate> delegate;

/**
 Setup preview with desired orientation mode and aspect ratio
 */
- (void)setPreviewWithOrientation:(kPreviewViewOrientation)orientation aspectRatio:(CGFloat)ratio;
@end

@protocol PreviewViewDelegate <NSObject>

@optional
/**
 Notifies the delegate that the zoom level has changed
 */
- (void)previewView:(PreviewView *) previewView didChangeZoomMode:(BOOL)zoomed;
@end
