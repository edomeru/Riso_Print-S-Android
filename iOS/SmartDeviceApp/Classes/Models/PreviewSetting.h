//
//  PreviewSetting.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PreviewSetting : NSObject
@property NSUInteger colorMode;
@property NSUInteger duplex;
@property NSUInteger paperSize;
@property NSUInteger pagination;
@property NSUInteger bind;
@property NSUInteger staple;
@property NSUInteger punch;
@property NSUInteger orientation;
@property BOOL isBookletBind;
@property BOOL isScaleToFit;
@end