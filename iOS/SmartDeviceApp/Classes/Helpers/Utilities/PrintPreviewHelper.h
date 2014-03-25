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

@interface PrintPreviewHelper : NSObject
/**
 Helper function to determine if preview is in grayscale based on color mode setting value
 
 @param colorMode - color mode setting value
 
 @return YES if grayscale; NO otherwise
 **/
+(BOOL) isGrayScaleColorForColorModeSetting : (NSUInteger) colorMode;

/**
 Helper function to determine spine location of page view controller based on settings affecting spine location
 
 @param bind - bind setting value
 @param duplex - duplex setting value
 @param isBookletBind - indicate if setting for booklet bind is On
 
 @return UIPageViewControllerSpineLocation
 **/
+(UIPageViewControllerSpineLocation) spineLocationForBindSetting: (NSUInteger) bind
                                                   duplexSetting: (NSUInteger)  duplex
                                            bookletBindSettingOn: (BOOL) isBookletBind;

/**
 Helper function to determine navigation orientation of page view controller based on bind setting
 
 @param bind - bind setting value
 
 @return UIPageViewControllerNavigationOrientation
 **/
+(UIPageViewControllerNavigationOrientation) navigationOrientationForBindSetting : (NSUInteger) bind;

/**
 Helper function to determine if paper orientation is landscape based on combination of settings
 
 @param previewSetting - preview setting object containing setting values
 
 @return YES if paper is landscape; NO otherwise
 **/
+(BOOL) isPaperLandscapeForPreviewSetting:(PreviewSetting*) setting;

/**
 Helper function to get ratio of paper height versus paper width based on paper size settings
 
 @param paperSize - value of paper size setting
 
 @return ratio of paper height versus paper width
 **/
+(CGFloat) heightToWidthRatioForPaperSizeSetting:(NSUInteger) paperSize;

/**
 Helper function to determine number of pages in a sheet based on the pagination settings
 
 @param pagination - value of paginationsetting
 
 @return Number of pages per sheet
 **/
+(NSUInteger) numberOfPagesPerSheetForPaginationSetting: (NSUInteger) pagination;

@end

