//
//  SlidingViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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

@end
