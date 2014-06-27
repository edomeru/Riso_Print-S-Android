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
#define PUNCH_3_4_BINDING_SIDE_CONSTRAINT_ENABLED 0
#define GET_ORIENTATION_FROM_PDF_ENABLED 1

/**
 * List of possible value for Color Mode option.
 */
typedef enum {
    kColorModeAuto, /**< Auto */
    kColorModeFullColor, /**< Full Color */
    kColorModeBlack /**< Black */
} kColorMode;

/**
 * List of possible value for Paper Size option.
 */
typedef enum {
    kPaperSizeA3, /**< A3 */
    kPaperSizeA3W, /**< A3W */
    kPaperSizeA4, /**< A4 */
    kPaperSizeA5, /**< A5 */
    kPaperSizeA6, /**< A6 */
    kPaperSizeB4, /**< B4 */
    kPaperSizeB5, /**< B5 */
    kPaperSizeB6, /**< B6 */
    kPaperSizeFoolscap, /**< Foolscap */
    kPaperSizeTabloid, /**< Tabloid */
    kPaperSizeLegal, /**< Legal */
    kPaperSizeLetter, /**< Letter */
    kPaperSizeStatement /**< Statement */
} kPaperSize;

/**
 * List of possible value for Imposition option.
 */
typedef enum {
    kImpositionOff, /**< Off */
    kImposition2Pages, /**< 2 pages  */
    kImposition4pages /**< 4 pages */
}kImposition;

/**
 * List of possible value for Imposition Order option.
 */
typedef enum {
    kImpositionOrderLeftToRight, /**< Left to right */
    kImpositionOrderRightToLeft, /**< Right to left */
    kImpositionOrderUpperLeftToRight, /**< Left top to right */
    kImpositionOrderUpperRightToLeft, /**< Right top to left */
    kImpositionOrderUpperLeftToBottom, /**< Left top to bottom */
    kImpositionOrderUpperRightToBottom /**< Right top to bottom */
}kImpositionOrder;

/**
 * List of possible value for Orientation option.
 */
typedef enum {
    kOrientationPortrait, /**< Portrait */
    kOrientationLandscape /**< Landscape */
}kOrientation;

/**
 * List of possible value for Staple Type option.
 */
typedef enum{
    kStapleTypeNone, /**< None */
    kStapleTypeUpperLeft, /**< Upper left */
    kStapleTypeUpperRight, /**< Upper right */
    kStapleType1Pos, /**< 1 */
    kStapleType2Pos /**< 2 */
}kStapleType;

/**
 * List of possible value for Finishing Side option.
 */
typedef enum{
    kFinishingSideLeft, /**< Left */
    kFinishingSideTop, /**< Top */
    kFinishingSideRight /**< Right */
}kFinishingSide;

/**
 * List of possible value for Punch Type option.
 */
typedef enum{
    kPunchTypeNone, /**< None */
    kPunchType2Holes, /**< 2 holes */
    kPunchType3or4Holes, /**< 4 holes */
}kPunchType;

/**
 * List of possible value for Duplex Setting option.
 */
typedef enum {
    kDuplexSettingOff, /**< Off */
    kDuplexSettingLongEdge, /**< Long edge */
    kDuplexSettingShortEdge /**< Short edge */
} kDuplexSetting;

/**
 * List of possible value for Booklet Type option.
 */
typedef enum {
    kBookletTypeOff, /**< Off */
    kBookletTypeFold, /**< Fold */
    kBookletTypeFoldAndStaple  /**< Fold and staple */
} kBookletType;

/**
 * List of possible value for Booklet Layout option.
 */
typedef enum {
    kBookletLayoutForward, /**< Forward */
    kBookletLayoutReverse, /**< Reverse */
} kBookletLayout;

/**
 * List of possible value for Output Tray option.
 */
typedef enum {
    kOutputTrayAuto, /**< Auto */
    kOutputTrayFaceDownTray, /**< Face down */
    kOutputTrayTop, /**< Top */
    kOutputTrayStacking /**< Stacking */
} kOutputTray;

/**
 * PrintPreviewHelper is a helper class that provides methods concerning the viewing of the document's print preview.
 */
@interface PrintPreviewHelper : NSObject

/**
 Helper function to determine if preview is in grayscale based on color mode setting value
 @param colorMode - color mode setting value
 @return YES if grayscale; NO otherwise
 */
+(BOOL) isGrayScaleColorForColorModeSetting : (kColorMode) colorMode;

/**
 Helper function to determine the aspect ratio of the paper
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

