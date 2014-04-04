//
//  PreviewView.h
//  SmartDeviceApp
//
//  Created by Seph on 4/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PreviewViewDelegate;

@interface PreviewView : UIView<UIGestureRecognizerDelegate>

typedef enum
{
    kPreviewViewOrientationPortrait,
    kPreviewViewOrientationLandscape
} kPreviewViewOrientation;

@property (nonatomic, strong, readonly) UIView *contentView;
@property (nonatomic, readonly) kPreviewViewOrientation orientation;
@property (nonatomic, readonly) CGFloat aspectRatio; // W : H
@property (nonatomic) CGFloat maxScale;
@property (nonatomic) CGFloat minScale;
@property (nonatomic, weak) id<PreviewViewDelegate> delegate;

- (void)setPreviewWithOrientation:(kPreviewViewOrientation)orientation aspectRatio:(CGFloat)ratio;

@end

@protocol PreviewViewDelegate <NSObject>

@optional
- (void)previewView:(PreviewView *) previewView didChangeZoomMode:(BOOL)zoomed;

@end
