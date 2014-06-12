//
//  PrintPreviewHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PreviewSetting.h"

#define OUTPUT_TRAY_CONSTRAINT_ENABLED 1

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
    kImpositionOff,
    kImposition2Pages,
    kImposition4pages
}kImposition;

typedef enum {
    kImpositionOrderLeftToRight,
    kImpositionOrderRightToLeft,
    kImpositionOrderUpperLeftToRight,
    kImpositionOrderUpperRightToLeft,
    kImpositionOrderUpperLeftToBottom,
    kImpositionOrderUpperRightToBottom
}kImpositionOrder;

typedef enum {
    kOrientationPortrait,
    kOrientationLandscape
}kOrientation;

typedef enum{
    kStapleTypeNone,
    kStapleTypeUpperLeft,
    kStapleTypeUpperRight,
    kStapleType1Pos,
    kStapleType2Pos
}kStapleType;

typedef enum{
    kFinishingSideLeft,
    kFinishingSideTop,
    kFinishingSideRight
}kFinishingSide;

typedef enum{
    kPunchTypeNone,
    kPunchType2Holes,
    kPunchType3or4Holes,
}kPunchType;

typedef enum {
    kDuplexSettingOff,
    kDuplexSettingLongEdge,
    kDuplexSettingShortEdge
} kDuplexSetting;

typedef enum {
    kBookletTypeOff,
    kBookletTypeFold,
    kBookletTypeFoldAndStaple
} kBookletType;

typedef enum {
    kBookletLayoutForward,
    kBookletLayoutReverse,
} kBookletLayout;

typedef enum {
    kOutputTrayAuto,
    kOutputTrayFaceDownTray,
    kOutputTrayTop,
    kOutputTrayStacking
} kOutputTray;

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

/**
 Helper function to determine actual paper dimensions in points
 @param paperSize - paper size
 @param isLandscape - if paper should be in landscape
 @return CGSize
 */
+ (CGSize)getPaperDimensions:(kPaperSize)paperSize isLandscape: (BOOL) isLandscape;

/**
 Helper function to determine if paper orientation is landscape based on combination of settings
 
 @param previewSetting - preview setting object containing setting values
 
 @return YES if paper is landscape; NO otherwise
 **/
+(BOOL) isPaperLandscapeForPreviewSetting:(PreviewSetting*) setting;

/**
 Helper function to determine number of pages in a sheet based on the pagination settings
 
 @param pagination - value of paginationsetting
 
 @return Number of pages per sheet
 **/
+ (NSUInteger)getNumberOfPagesPerSheetForImpostionSetting:(NSUInteger)imposition;

@end

