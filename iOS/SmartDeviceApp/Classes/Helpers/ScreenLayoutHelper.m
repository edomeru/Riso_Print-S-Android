//
//  ScreenLayoutHelper.m
//  RISOSmartPrint
//
//  Created by Prnsoft on 7/20/16.
//  Copyright © 2016 aLink. All rights reserved.
//

#import "ScreenLayoutHelper.h"

@implementation ScreenLayoutHelper

+ (CGFloat)getPortraitScreenWidth
{
    CGSize screenSize =[[UIScreen mainScreen] bounds].size;
    return screenSize.width < screenSize.height ? screenSize.width : screenSize.height;
}

@end
