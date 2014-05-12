//
//  DeleteButton.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DeleteButton : UIButton

+ (id)createAtOffscreenPosition:(CGRect)offscreen withOnscreenPosition:(CGRect)onscreen;

- (void)animateOnscreen:(void (^)(BOOL))completion;
- (void)animateOffscreen:(void (^)(BOOL))completion;

@end
