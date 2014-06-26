//
//  PreviewView.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PreviewViewDelegate;

/**
 * Custom view for resizable and moveable page view.
 * Used for displaying the contents of a PDF file.
 */
@interface PreviewView : UIView<UIGestureRecognizerDelegate>

/**
 * Orientation modes of the PreviewView
 */
typedef enum
{
    kPreviewViewOrientationPortrait, /**< Portrait mode */
    kPreviewViewOrientationLandscape /**< Landscape mode */
} kPreviewViewOrientation;

/**
 * Page content view.
 * UIView of the page contents to be displayed in the PreviewView
 */
@property (nonatomic, strong) UIView *pageContentView;

/**
 * Orientation mode.
 * - kPreviewOrientationPortrait: The page will be displayed in Portrait mode.
 * The page will adjust its size according to its height.
 * - kPreviewOrientationLandscape: The page will be displayed in Landscape mode.
 * The page will adjust its size according to its width.
 *
 * @see aspectRatio
 */
@property (nonatomic, readonly) kPreviewViewOrientation orientation;

/**
 * Aspect ratio (W:H).
 * Used in computing the length of the page's sides based on the orientation setting.
 *
 * @see orientation
 */
@property (nonatomic, readonly) CGFloat aspectRatio;

/**
 * Maximum allowed scale value.
 * Restricts the scale factor when zooming-in via pinch gesture.
 * Default value: 4.0f
 *
 * @see minScale
 */
@property (nonatomic) CGFloat maxScale;

/**
 * Minimum allowed scale value.
 * Restricts the scale factor when zooming-out via pinch gesture.
 * 
 * @see maxScale
 */
@property (nonatomic) CGFloat minScale;

/**
 * Delegate.
 * Delegate that will handle zoom events
 */
@property (nonatomic, weak) id<PreviewViewDelegate> delegate;

/**
 * Prepares the page for display.
 * Sets up the page by computing the dimensions using the orientation and aspectRatio values.
 *
 * @see orientation
 * @see aspectRatio
 * @param orientation Orientation of the view.
 * @param aspectRatio Aspect Ratio of the view (W:H).
 */
- (void)setPreviewWithOrientation:(kPreviewViewOrientation)orientation aspectRatio:(CGFloat)ratio;
@end

/**
 * Handles PreviewView events
 * Used for handling events that will affect UI behavior.
 */
@protocol PreviewViewDelegate <NSObject>

@optional
/**
 * Notifies that the zoom level has changed
 * Allows the delegate to change the behavior of the PreviewView when the page is scaled.
 *
 * @param previewView PreviewView that triggered the event.
 * @param zoomed Whether or not the PreviewView is zoomed.
 */
- (void)previewView:(PreviewView *) previewView didChangeZoomMode:(BOOL)zoomed;
@end
