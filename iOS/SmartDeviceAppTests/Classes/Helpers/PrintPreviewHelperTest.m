//
//  PreviewHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintPreviewHelper.h"
#import "PreviewSetting.h"

#define PRINTPREVIEW_HELPER_TEST 1
#if PRINTPREVIEW_HELPER_TEST

@interface PrintPreviewHelperTest:GHTestCase

@end

@implementation PrintPreviewHelperTest
{
    CGSize A3WSize;
    CGSize A3Size;
    CGSize A4Size;
    CGSize A5Size;
    CGSize A6Size;
    CGSize B4Size;
    CGSize B5Size;
    CGSize B6Size;
    CGSize FoolscapSize;
    CGSize TabloidSize;
    CGSize LegalSize;
    CGSize LetterSize;
    CGSize StatementSize;
    CGFloat diffThreshold;
}
- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    A3WSize = CGSizeMake(895.748, 1303.937);
    A3Size = CGSizeMake(841.890, 1190.551);
    A4Size = CGSizeMake(595.276, 841.890);
    A5Size = CGSizeMake(419.528, 595.276);
    A6Size = CGSizeMake(297.638, 419.528);
    B4Size = CGSizeMake(728.504, 1031.811);
    B5Size = CGSizeMake(515.906, 728.504);
    B6Size = CGSizeMake(362.835, 515.906);
    FoolscapSize = CGSizeMake(612.283, 963.780);
    TabloidSize = CGSizeMake(793.701, 1224.567);
    LegalSize = CGSizeMake(612.283, 1009.134);
    LetterSize = CGSizeMake(612.283, 793.701);
    StatementSize = CGSizeMake(396.850, 612.283);
    diffThreshold = 0.001f;
}

// Run at end of all tests in the class
- (void)tearDownClass
{
}

// Run before each test method
- (void)setUp
{
}

// Run after each test method
- (void)tearDown
{
}



#pragma mark - Test Cases

- (void)test001_isGrayScaleColorForColorModeSetting
{
    GHAssertFalse( [PrintPreviewHelper isGrayScaleColorForColorModeSetting:kColorModeAuto], @"Input:kColorModeAuto");
    GHAssertFalse( [PrintPreviewHelper isGrayScaleColorForColorModeSetting:kColorModeFullColor], @"Input:kColorModeFullColor");
    GHAssertTrue([PrintPreviewHelper isGrayScaleColorForColorModeSetting:kColorModeBlack], @"Input:kColorModeBlack");
}

- (void)test002_getAspectRatioForPaperSize
{
    CGFloat actualValue, expectedValue;
    
    //A3W
    expectedValue = A3WSize.width/A3WSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeA3W];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"A3WSize aspect ratio");
    //A3
    expectedValue = A3Size.width/A3Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeA3];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"A3Size aspect ratio");
    //A4
    expectedValue = A4Size.width/A4Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeA4];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"A4Size aspect ratio");
    //A5
    expectedValue = A5Size.width/A5Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeA5];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"A5Size aspect ratio");
    //A6
    expectedValue = A6Size.width/A6Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeA6];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"A6Size aspect ratio");
    //B4
    expectedValue = B4Size.width/B4Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeB4];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"B4Size aspect ratio");
    //B5
    expectedValue = B5Size.width/B5Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeB5];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"B5Size aspect ratio");
    //B6
    expectedValue = B6Size.width/B6Size.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeB6];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"B6Size aspect ratio");
    //Foolscap
    expectedValue = FoolscapSize.width/FoolscapSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeFoolscap];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"FoolscapSize aspect ratio");
    //Tabloid
    expectedValue = TabloidSize.width/TabloidSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeTabloid];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"TabloidSize aspect ratio");
    //Legal
    expectedValue = LegalSize.width/LegalSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeLegal];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"LegalSize aspect ratio");
    //Letter
    expectedValue = LetterSize.width/LetterSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeLetter];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"LetterSize aspect ratio");
    //Statement
    expectedValue = StatementSize.width/StatementSize.height;
    actualValue = [PrintPreviewHelper getAspectRatioForPaperSize:kPaperSizeStatement];
    GHAssertLessThanOrEqual(fabsf(actualValue - expectedValue),diffThreshold,@"StatementSize aspect ratio");
}

- (void)test003_getPaperDimensions
{
    CGSize actualValue, expectedValue;

    //A3W
    expectedValue = A3WSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA3W isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"A3WSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"A3WSize height");
    //A3
    expectedValue = A3Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA3 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"A3Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"A3Size height");
    //A4
    expectedValue = A4Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA4 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"A4Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"A4Size height");
    //A5
    expectedValue = A5Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA5 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"A5Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"A5Size height");
    //A6
    expectedValue = A6Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA6 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"A6Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"A6Size height");
    //B4
    expectedValue = B4Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB4 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"B4Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"B4Size height");
    //B5
    expectedValue = B5Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB5 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"B5Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"B5Size height");
    //B6
    expectedValue = B6Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB6 isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"B6Size width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"B6Size height");
    //Foolscap
    expectedValue = FoolscapSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeFoolscap isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"FoolscapSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"FoolscapSize height");
    //Tabloid
    expectedValue = TabloidSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeTabloid isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"TabloidSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"TabloidSize height");
    //Legal
    expectedValue = LegalSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeLegal isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"LegalSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"LegalSize height");
    //Letter
    expectedValue = LetterSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeLetter isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"LetterSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"LetterSize height");
    //Statement
    expectedValue = StatementSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeStatement isLandscape:NO];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.width),diffThreshold,@"StatementSize width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.height),diffThreshold,@"StatementSize height");
    
    //A3W Landscape
    expectedValue = A3WSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA3W isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"A3WSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"A3WSize Landscape height");
    //A3 Landscape
    expectedValue = A3Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA3 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"A3Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"A3Size Landscape height");
    //A4 Landscape
    expectedValue = A4Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA4 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"A4Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"A4Size Landscape height");
    //A5 Landscape
    expectedValue = A5Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA5 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"A5Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"A5Size Landscape height");
    //A6 Landscape
    expectedValue = A6Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeA6 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"A6Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"A6Size Landscape height");
    //B4 Landscape
    expectedValue = B4Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB4 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"B4Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"B4Size Landscape height");
    //B5 Landscape
    expectedValue = B5Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB5 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"B5Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"B5Size Landscape height");
    //B6 Landscape
    expectedValue = B6Size;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeB6 isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"B6Size Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"B6Size Landscape height");
    //Foolscap Landscape
    expectedValue = FoolscapSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeFoolscap isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"FoolscapSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"FoolscapSize Landscape height");
    //Tabloid Landscape
    expectedValue = TabloidSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeTabloid isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"TabloidSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"TabloidSize Landscape height");
    //Legal Landscape
    expectedValue = LegalSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeLegal isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"LegalSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"LegalSize Landscape height");
    //Letter Landscape
    expectedValue = LetterSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeLetter isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"LetterSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"LetterSize Landscape height");
    //Statement Landscape
    expectedValue = StatementSize;
    actualValue = [PrintPreviewHelper getPaperDimensions:kPaperSizeStatement isLandscape:YES];
    GHAssertLessThanOrEqual(fabsf(actualValue.width - expectedValue.height),diffThreshold,@"StatementSize Landscape width");
    GHAssertLessThanOrEqual(fabsf(actualValue.height - expectedValue.width),diffThreshold,@"StatementSize Landscape height");
}


- (void)test004_isPaperLandscapeForPreviewSetting
{
    PreviewSetting *previewSettingData = [[PreviewSetting alloc] init];
    
    previewSettingData.booklet = YES;
    previewSettingData.bookletLayout = kBookletLayoutForward;
    previewSettingData.orientation = kOrientationLandscape;
    previewSettingData.imposition = kImpositionOff;

    GHAssertFalse([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.orientation = kOrientationPortrait;
    GHAssertTrue([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.booklet = NO;
    previewSettingData.imposition = kImposition2Pages;
    GHAssertTrue([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.orientation = kOrientationLandscape;
    GHAssertFalse([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.imposition = kImposition4pages;
    GHAssertTrue([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.orientation = kOrientationPortrait;
    GHAssertFalse([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
    
    previewSettingData.imposition = kImpositionOff;
    previewSettingData.orientation = kOrientationLandscape;
    GHAssertTrue([PrintPreviewHelper isPaperLandscapeForPreviewSetting:previewSettingData], @"");
}

- (void)test005_getNumberOfPagesPerSheetForImpostionSetting
{
    NSUInteger expectedResult = 2;
    GHAssertEquals([PrintPreviewHelper getNumberOfPagesPerSheetForImpostionSetting:kImposition2Pages], expectedResult, @"Input:kImposition2Pages");
    
    expectedResult = 4;
    GHAssertEquals([PrintPreviewHelper getNumberOfPagesPerSheetForImpostionSetting:kImposition4pages], expectedResult, @"Input:kImposition4Pages");
    
    expectedResult = 1;
    GHAssertEquals([PrintPreviewHelper getNumberOfPagesPerSheetForImpostionSetting:kImpositionOff], expectedResult, @"Input:kImpositionOff");
    GHAssertEquals([PrintPreviewHelper getNumberOfPagesPerSheetForImpostionSetting:kImposition4pages + 1], expectedResult, @"Input:Othervalues");
}
@end
#endif
