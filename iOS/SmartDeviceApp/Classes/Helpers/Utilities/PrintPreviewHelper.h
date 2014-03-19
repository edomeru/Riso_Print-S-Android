//
//  PrintPreviewHelper.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PreviewSetting.h"

typedef enum {
    PAGINATION_OFF,
    PAGINATION_2IN1,
    PAGINATION_4IN1,
    PAGINATION_6IN1,
    PAGINATION_9IN1,
    PAGINATION_16IN1
}T_PAGINATION;

typedef enum {
    PAPERSIZE_A3,
    PAPERSIZE_A4,
    PAPERSIZE_LETTER,
    PAPERSIZE_LEGAL,
    PAPERSIZE_ENVELOPE
}T_PAPERSIZE;

typedef enum  {
    BIND_LEFT,
    BIND_RIGHT,
    BIND_TOP
} T_BIND;

typedef enum{
    COLORMODE_AUTO,
    COLORMODE_COLOR,
    COLORMODE_MONO
}T_COLORMODE;

typedef enum
{
    ORIENTATION_PORTRAIT,
    ORIENTATION_LANDSCAPE
    
}T_ORIENTATION;

typedef enum
{
    DUPLEX_OFF,
    DUPLEX_SHORT_EDGE,
    DUPLEX_LONG_EDGE
} T_DUPLEX;

BOOL isGrayScale(NSUInteger colorMode);

UIPageViewControllerSpineLocation getSpineLocation(NSUInteger bind, NSUInteger duplex, BOOL isBookletBind);

UIPageViewControllerNavigationOrientation getNavigationOrientation(NSUInteger bind);

BOOL isPaperLandscape(PreviewSetting *setting);

float getHeightToWidthRatio(NSUInteger paperSize);

NSUInteger getNumberOfPagesPerSheet(NSUInteger pagination);



