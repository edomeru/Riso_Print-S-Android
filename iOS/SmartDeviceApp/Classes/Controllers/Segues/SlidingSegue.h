//
//  SlidingSegue.h
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum _SlideDirection
{
    SlideLeft = 0,
    SlideRight = 1,
} SlideDirection;

@interface SlidingSegue : UIStoryboardSegue

@property (nonatomic) SlideDirection slideDirection;
@property (nonatomic) BOOL isUnwinding;

@end
