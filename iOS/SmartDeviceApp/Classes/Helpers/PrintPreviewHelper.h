//
//  PrintPreviewHelper.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/12/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PreviewSetting.h"

#define DEBUG_LOG_PRINT_PREVIEW 0 //TODO: transfer to AppDebug.h on merge

typedef enum {
    kColorModeAuto,
    kColorModeFullColor,
    kColorModeBlack
} kColorMode;

typedef enum {
    kPaperSizeA3W,
    kPaperSizeA3,
    kPaperSizeA4,
    kPaperSizeA5,
    kPaperSizeA6,
    kPaperSizeB4,
    kPaperSizeB5,
    kPaperSizeB6,
    kPaperSizeFoolscap,
    kPaperSizeTabloid,
    kPaperSizeLegal,
    kPaperSizeLetter,
    kPaperSizeStatement
} kPaperSize;

typedef enum {
    PAGINATION_OFF,
    PAGINATION_2IN1,
    PAGINATION_4IN1,
    PAGINATION_6IN1,
    PAGINATION_9IN1,
    PAGINATION_16IN1
}T_PAGINATION;

typedef enum  {
    BIND_LEFT,
    BIND_RIGHT,
    BIND_TOP
} T_BIND;

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
 */
+(BOOL) isGrayScaleColorForColorModeSetting : (kColorMode) colorMode;

/**
 Helper function to determine the aspect ration of the paper
 @param paperSize - paper size
 @return aspect ratio (height / width)
 */
+(CGFloat) getAspectRatioForPaperSize:(kPaperSize) paperSize;


+ (CGSize) getPaperDimensions:(kPaperSize)paperSize;

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

