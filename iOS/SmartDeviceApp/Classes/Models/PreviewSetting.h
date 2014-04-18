//
//  PreviewSetting.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

#define KEY_ORIENTATION         @"orientation"
#define KEY_DUPLEX              @"duplex"
#define KEY_IMPOSITION          @"imposition"
#define KEY_IMPOSITION_ORDER    @"impositionOrder"
#define KEY_BOOKLET             @"booklet"
#define KEY_BOOKLET_FINISH      @"bookletFinish"
#define KEY_BOOKLET_LAYOUT      @"bookletLayout"
#define KEY_FINISHING_SIDE      @"finishingSide"
#define KEY_STAPLE              @"staple"
#define KEY_PUNCH               @"punch"

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

@property (nonatomic, weak, readonly) NSString *formattedString;

@end
