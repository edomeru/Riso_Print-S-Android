//
//  PrintPreviewHelper.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintPreviewHelper.h"
struct {
    float width;
    float height;
} paperDimensionsMM[] = {
    {297.0, 420.0}, //A3
    {210.0, 297.0}, //A4
    {215.9, 279.4}, //Letter
    {215.9, 355.6}, //Legal
    {110.0, 220.0}  //Envelope
};

BOOL isGrayScale(NSUInteger colorMode)
{
    if(colorMode == COLORMODE_MONO)
    {
        return YES;
    }
    return NO;
}

UIPageViewControllerSpineLocation getSpineLocation(NSUInteger bind, NSUInteger duplex, BOOL isBookletBind)
{
    if(duplex > DUPLEX_OFF || isBookletBind == YES)
    {
        return UIPageViewControllerSpineLocationMid;
    }
    if(bind == BIND_RIGHT)
    {
        return UIPageViewControllerSpineLocationMax;
    }
    
    return UIPageViewControllerSpineLocationMin;
}

UIPageViewControllerNavigationOrientation getNavigationOrientation(NSUInteger bind)
{
    if(bind == BIND_TOP)
    {
        return UIPageViewControllerNavigationOrientationVertical;
    }
    return UIPageViewControllerNavigationOrientationHorizontal;
}

BOOL isPaperLandscape(PreviewSetting *setting)
{
    if(setting == nil)
    {
        return NO;
    }
    
    if(setting.isBookletBind == YES && setting.bind != BIND_TOP)
    {
        return YES;
    }
    
    if(setting.pagination == PAGINATION_2IN1 || setting.pagination == PAGINATION_6IN1)
    {
        return YES;
    }
    
    if(setting.orientation == ORIENTATION_LANDSCAPE)
    {
        return YES;
    }
    
    return NO;
}

float getHeightToWidthRatio(NSUInteger paperSize)
{
    float ratio = paperDimensionsMM[paperSize].height / paperDimensionsMM[paperSize].width;
    return ratio;
}

NSUInteger getNumberOfPagesPerSheet(NSUInteger pagination)
{
    switch(pagination)
    {
        case PAGINATION_2IN1:
            return 2;
        case PAGINATION_4IN1:
            return 4;
        case PAGINATION_6IN1:
            return 6;
        case PAGINATION_9IN1 :
            return 9;
        case PAGINATION_16IN1:
            return 16;
    }
    return 1;
}