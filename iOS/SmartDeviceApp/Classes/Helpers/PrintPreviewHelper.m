//
//  PrintPreviewHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintPreviewHelper.h"

#define POINTS_PER_INCH 72.0f //PDF API converts PDF dimensions from actual size to points at 72 ppi
#define MM_PER_INCH 25.4f //1 inch == ~25.4 mm

CGSize paperDimensionsMM[] = {
    {297.0f, 420.0f}, // A3
    {316.0f, 460.0f}, // A3W
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
    //return ratio for portrait
    CGFloat ratio = paperDimensionsMM[paperSize].width / paperDimensionsMM[paperSize].height ;
    return ratio;
}

+ (CGSize)getPaperDimensions:(kPaperSize)paperSize isLandscape:(BOOL)isLandscape
{
    CGSize size = paperDimensionsMM[paperSize];
    //To have paper size in actual proportion to size of PDF in points, convert paper mm dimensions to points at 72 PPI
    size.width = (size.width/MM_PER_INCH) * POINTS_PER_INCH;
    size.height = (size.height/MM_PER_INCH) * POINTS_PER_INCH;
    //Sizes are in portrait, for Landscape, interchange height and width
    if(isLandscape == YES)
    {
        CGFloat temp = size.width;
        size.width = size.height;
        size.height = temp;
    }
    return size;
}

+(BOOL) isPaperLandscapeForPreviewSetting:(PreviewSetting*) setting
{
    if(setting == nil)
    {
        return NO;
    }
    
    if(setting.booklet == YES)
    {
        if(setting.orientation == kOrientationPortrait)
        {
            return YES;
        }
        return NO;
    }
    
    if(setting.imposition == kImposition2Pages && setting.orientation == kOrientationPortrait)
    {
        return YES;
    }
    
    if(setting.imposition == kImposition2Pages && setting.orientation == kOrientationLandscape)
    {
        return NO;
    }
    
    if(setting.orientation == kOrientationLandscape)
    {
        return YES;
    }
    
    return NO;
}

+ (NSUInteger)getNumberOfPagesPerSheetForImpostionSetting:(NSUInteger)imposition
{
    switch(imposition)
    {
        case kImposition2Pages:
            return 2;
        case kImposition4pages:
            return 4;
        default:
            break;
    }
    return 1;
}

@end
