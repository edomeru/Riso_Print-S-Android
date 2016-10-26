//
//  ScreenLayoutHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "ScreenLayoutHelper.h"

@implementation ScreenLayoutHelper

+ (CGFloat)getPortraitScreenWidth
{
    CGSize screenSize =[[UIScreen mainScreen] bounds].size;
    return screenSize.width < screenSize.height ? screenSize.width : screenSize.height;
}

@end
