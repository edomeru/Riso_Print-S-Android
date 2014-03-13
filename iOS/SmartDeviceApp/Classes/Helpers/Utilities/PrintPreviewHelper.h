//
//  PrintPreviewHelper.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>


BOOL isGrayScale(NSUInteger colorMode);

UIPageViewControllerSpineLocation getSpineLocation(NSUInteger bind, BOOL duplex, BOOL bookletBinding);

UIPageViewControllerNavigationOrientation getNavigationOrientation(NSUInteger bind);

BOOL isPaperLandscape(NSUInteger pagination);

float getHeightToWidthRatio(NSUInteger paperSize);

NSUInteger getNumberOfPagesPerSheet(NSUInteger pagination);



