//
//  SlidingViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum _SlideDirection
{
    SlideLeft = 0,
    SlideRight = 1,
} SlideDirection;

@interface SlidingViewController : UIViewController

@property (nonatomic) SlideDirection slideDirection;
@property (nonatomic) BOOL isFixedSize;

- (void)close;

@end
