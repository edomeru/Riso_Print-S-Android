//
//  PreviewSetting.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PreviewSetting : NSObject
@property (nonatomic) NSInteger colorMode;
@property (nonatomic) NSInteger orientation;
@property (nonatomic) NSInteger copies;
@property (nonatomic) NSInteger duplex;
@property (nonatomic) NSInteger paperSize;
@property (nonatomic) BOOL scaleToFit;
@property (nonatomic) NSInteger paperType;
@property (nonatomic) NSInteger inputTray;
@property (nonatomic) NSInteger imposition;
@property (nonatomic) NSInteger impositionOrder;
@property (nonatomic) NSInteger sort;
@property (nonatomic) BOOL booklet;
@property (nonatomic) NSInteger bookletFinish;
@property (nonatomic) NSInteger bookletLayout;
@property (nonatomic) NSInteger finishingSide;
@property (nonatomic) NSInteger staple;
@property (nonatomic) NSInteger punch;
@property (nonatomic) NSInteger outputTray;
@end
