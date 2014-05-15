//
//  DeleteButton.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol DeleteButtonDelegate <NSObject>

@optional
- (BOOL)shouldHighlightButton;

@end

@interface DeleteButton : UIButton

@property (weak, nonatomic) id<DeleteButtonDelegate> delegate;

@property (strong, nonatomic) UIColor* highlightedColor;
@property (strong, nonatomic) UIColor* highlightedTextColor;

+ (id)createAtOffscreenPosition:(CGRect)offscreen withOnscreenPosition:(CGRect)onscreen;

- (void)keepHighlighted:(BOOL)enable;
- (void)animateOnscreen:(void (^)(BOOL))completion;
- (void)animateOffscreen:(void (^)(BOOL))completion;

@end
