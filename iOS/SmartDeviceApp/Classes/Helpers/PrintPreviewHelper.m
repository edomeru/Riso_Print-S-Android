//
//  PrintPreviewHelper.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintPreviewHelper.h"

CGSize paperDimensionsMM[] = {
    {306.0f, 460.0f}, // A3W
    {297.0f, 420.0f}, // A3
    {210.0f, 297.0f}, // A4
    {148.0f, 210.0f}, // A5
    {105.0f, 148.0f}, // A6
    {257.0f, 364.0f}, // B4
    {182.0f, 257.0f}, // B5
    {128.0f, 182.0f}, // B6
    {216.0f, 340.0f}, // Foolscap
    {280.0f, 432.0f}, // Tabloid
    {216.0f, 356.0f}, // Legal
    {216.0f, 280.0f}, // Letter
    {140.0f, 216.0f}, // Statement
};

@implementation PrintPreviewHelper

+(BOOL) isGrayScaleColorForColorModeSetting : (kColorMode) colorMode
{
    if(colorMode == kColorModeBlack)
    {
        return YES;
    }
    return NO;
}

+(CGFloat) getAspectRatioForPaperSize:(kPaperSize) paperSize
{
    CGFloat ratio = paperDimensionsMM[paperSize].height / paperDimensionsMM[paperSize].width;
    return ratio;
}

+(UIPageViewControllerSpineLocation) spineLocationForBindSetting: (NSUInteger) bind
                                    duplexSetting: (NSUInteger)  duplex
                                    bookletBindSettingOn: (BOOL) isBookletBind
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

+ (CGSize) getPaperDimensions:(kPaperSize)paperSize
{
    return paperDimensionsMM[paperSize];
}


+(UIPageViewControllerNavigationOrientation) navigationOrientationForBindSetting : (NSUInteger) bind
{
    if(bind == BIND_TOP)
    {
        return UIPageViewControllerNavigationOrientationVertical;
    }
    return UIPageViewControllerNavigationOrientationHorizontal;
}

+(BOOL) isPaperLandscapeForPreviewSetting:(PreviewSetting*) setting
{
    if(setting == nil)
    {
        return NO;
    }
    
    //if(setting.isBookletBind == YES && setting.bind != BIND_TOP)
    {
        return YES;
    }
    
    //if(setting.pagination == PAGINATION_2IN1 || setting.pagination == PAGINATION_6IN1)
    {
        return YES;
    }
    
    if(setting.orientation == ORIENTATION_LANDSCAPE)
    {
        return YES;
    }
    
    return NO;
}

+(CGFloat) heightToWidthRatioForPaperSizeSetting:(NSUInteger) paperSize
{
    CGFloat ratio = paperDimensionsMM[paperSize].height / paperDimensionsMM[paperSize].width;
    return ratio;
}

+(NSUInteger) numberOfPagesPerSheetForPaginationSetting: (NSUInteger) pagination
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

@end
