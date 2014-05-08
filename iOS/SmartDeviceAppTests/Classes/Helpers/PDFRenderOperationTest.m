//
//  PDFRenderOperationTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 5/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PrintPreviewHelper.h"
#import "PDFFileManager.h"
#import "Printer.h"
#import "DatabaseManager.h"

@interface PDFRenderOperation(Test)

@property (nonatomic, strong) NSArray *pageIndices;
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic) CGSize size;
@property (nonatomic) NSUInteger currentPage;
@property (nonatomic) BOOL isFrontPage;

- (void)drawPagesInRect:(CGRect)rect inContext:(CGContextRef)contextRef;
- (void)drawPage:(NSUInteger)pageNumber inRect:(CGRect)rect inContext:(CGContextRef)contextRef;
- (void)draw2In1InContext:(CGContextRef)contextRef;
- (void)draw4In1InContext:(CGContextRef)contextRef;
- (void)drawPagesInRects:(NSArray *)rectArray atStartPageNumber:(NSUInteger)pageNumber inContext:(CGContextRef)contextRef;
- (void)drawFinishing:(CGContextRef)contextRef;
- (void)drawStapleSingle:(CGContextRef)contextRef withStapleType:(kStapleType)stapleType atFinishingSide:(kFinishingSide)finishingSide;
- (void)drawStaple2Pos:(CGContextRef)contextRef atFinishingSide:(kFinishingSide)finishingSide withMargin:(CGFloat)margin;
- (void)drawPunch:(CGContextRef)contextRef withPunchType:(kPunchType)punchType atFinishingSide:(kFinishingSide)finishingSide;
- (BOOL)shouldInvertImage;
@end

@interface PDFRenderOperationTest : GHTestCase

@property (nonatomic,strong) UIImage *expectedImageUpperLeftStaple;

@end

@implementation PDFRenderOperationTest
{
    CGSize testSize;
    CGFloat drawingMargins;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;

}
 - (void)setUpClass
{
    testSize = CGSizeMake(612.283, 793.701);
    drawingMargins = 10.0f;
    
    pdfOriginalFileURL= [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[pdfOriginalFileURL.path lastPathComponent]]];
    
    testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
}

- (void)setUp
{
    NSError *error;
    [[NSFileManager defaultManager] copyItemAtPath:[pdfOriginalFileURL path] toPath: [testURL path] error:&error];
    PDFFileManager *manager = [PDFFileManager sharedManager];
    [manager setFileURL:testURL];
    [manager setupDocument];
}

-(void)test001_init
{
    NSArray *indices = [NSArray arrayWithObjects:[NSNumber numberWithInteger: 0], nil];
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:indices size:testSize delegate:nil];

    GHAssertNotNil(renderOperation, @"");
    GHAssertEqualObjects([[PDFFileManager sharedManager] printDocument], renderOperation.printDocument, @"");
    
}

-(void) test002_drawStapleSingle_1PosLeft
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStapleSingle:context withStapleType:kStapleType1Pos atFinishingSide:kFinishingSideLeft];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImageUpperLeftStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test003_drawStapleSingle_1PosRight
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStapleSingle:context withStapleType:kStapleType1Pos atFinishingSide:kFinishingSideRight];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImageUpperRightStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test004_drawStapleSingle_UpperLeftTop
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStapleSingle:context withStapleType:kStapleTypeUpperLeft atFinishingSide:kFinishingSideTop];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImageUpperLeftStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test005_drawStapleSingle_UpperRightTop
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStapleSingle:context withStapleType:kStapleTypeUpperRight atFinishingSide:kFinishingSideTop];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImageUpperRightStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test005_draw2PosLeft
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStaple2Pos:context atFinishingSide:kFinishingSideLeft withMargin:drawingMargins];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2PosLeftStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test006_draw2PosRight
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStaple2Pos:context atFinishingSide:kFinishingSideRight withMargin:drawingMargins];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2PosRightStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test007_draw2PosTop
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawStaple2Pos:context atFinishingSide:kFinishingSideTop withMargin:drawingMargins];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2PosTopStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}


-(void) test008_drawPunch2HoleLeft
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawPunch:context withPunchType:kPunchType2Holes atFinishingSide:kFinishingSideLeft];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2HolePunchLeft])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test009_drawPunch2HoleRight
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawPunch:context withPunchType:kPunchType2Holes atFinishingSide:kFinishingSideRight];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2HolePunchRight])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test010_drawPunch2HoleTop
{
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawPunch:context withPunchType:kPunchType2Holes atFinishingSide:kFinishingSideTop];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2HolePunchTop])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test011_drawPunch4Hole
{
    Printer *printer = (Printer*)[DatabaseManager addObject:E_PRINTER];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:YES];
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:NO];
    PDFFileManager *manager = [PDFFileManager sharedManager];
    manager.printDocument.printer = printer;
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawPunch:context withPunchType:kPunchType3or4Holes atFinishingSide:kFinishingSideLeft];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage4HolePunchLeft])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    [DatabaseManager discardChanges];
}

-(void) test012_drawPunch3Hole
{
    Printer *printer = (Printer*)[DatabaseManager addObject:E_PRINTER];
    printer.enabled_finisher_2_4_holes = [NSNumber numberWithBool:NO];
    printer.enabled_finisher_2_3_holes = [NSNumber numberWithBool:YES];
    PDFFileManager *manager = [PDFFileManager sharedManager];
    manager.printDocument.printer = printer;
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawPunch:context withPunchType:kPunchType3or4Holes atFinishingSide:kFinishingSideLeft];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage3HolePunchLeft])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    [DatabaseManager discardChanges];
}

-(void) test013_shouldInvertImage
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];

    renderOperation.isFrontPage = YES;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    renderOperation.isFrontPage = NO;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideTop;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    CGSize landscapeTestSize = CGSizeMake(testSize.height, testSize.width);
    renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:landscapeTestSize delegate:nil];
    
    renderOperation.isFrontPage = YES;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    renderOperation.isFrontPage = NO;
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideTop;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
}

-(void) test014_drawFinishingStaple1Pos
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.staple = kStapleType1Pos;

    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawFinishing:context];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImageUpperLeftStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test015_drawFinishingStaple2Pos
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.staple = kStapleType2Pos;
    
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawFinishing:context];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2PosLeftStaple])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(void) test016_drawFinishingPunch
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.punch = kPunchType2Holes;
    
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    
    [renderOperation drawFinishing:context];
    
    UIImage *actualImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    BOOL isImageEqual = [UIImagePNGRepresentation(actualImage) isEqualToData:UIImagePNGRepresentation([self getExpectedImage2HolePunchLeft])];
    
    GHAssertTrue(isImageEqual, @"");
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
}

-(UIImage *)getExpectedImageUpperLeftStaple
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat imageWidth = 30.0f;
    CGFloat imageHeight = 30.0f;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *stapleImage = [UIImage imageNamed:@"img_staple_left_top"];
    
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height - imageHeight - drawingMargins, imageWidth, imageHeight), [stapleImage CGImage]);
    
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImageUpperRightStaple
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat imageWidth = 30.0f;
    CGFloat imageHeight = 30.0f;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *stapleImage = [UIImage imageNamed:@"img_staple_right_top"];
    
    CGContextDrawImage(context, CGRectMake(testSize.width - imageWidth - drawingMargins, testSize.height - imageHeight- drawingMargins, 30.0f, 30.0f), [stapleImage CGImage]);
    
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2PosLeftStaple
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat imageWidth = 5.0f;
    CGFloat imageHeight = 42.4f;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *stapleImage = [UIImage imageNamed:@"img_staple_left"];
    
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * .25 - imageHeight/2, imageWidth, imageHeight), [stapleImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * .75 - imageHeight/2, imageWidth, imageHeight), [stapleImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2PosRightStaple
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat imageWidth = 5.0f;
    CGFloat imageHeight = 42.4f;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *stapleImage = [UIImage imageNamed:@"img_staple_right"];
    
    CGContextDrawImage(context, CGRectMake(testSize.width - imageWidth - drawingMargins, testSize.height * .25 - imageHeight/2, imageWidth, imageHeight), [stapleImage CGImage]);
    CGContextDrawImage(context, CGRectMake(testSize.width - imageWidth - drawingMargins, testSize.height * .75 - imageHeight/2, imageWidth, imageHeight), [stapleImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2PosTopStaple
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat imageWidth = 42.4f;
    CGFloat imageHeight = 5.0f;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *stapleImage = [UIImage imageNamed:@"img_staple_top"];
    
    CGContextDrawImage(context, CGRectMake(testSize.width *0.25f - imageWidth/2, testSize.height  - drawingMargins - imageHeight, imageWidth, imageHeight), [stapleImage CGImage]);
    CGContextDrawImage(context, CGRectMake(testSize.width *0.75f - imageWidth/2,  testSize.height  - drawingMargins - imageHeight, imageWidth, imageHeight), [stapleImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2HolePunchLeft
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat punchDistance = 228.0f;
    CGFloat imageWidth = 18.0f;
    CGFloat imageHeight = imageWidth;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 - punchDistance/2, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 + punchDistance/2, imageWidth, imageHeight), [punchImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2HolePunchRight
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat punchDistance = 228.0f;
    CGFloat imageWidth = 18.0f;
    CGFloat imageHeight = imageWidth;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    
    CGContextDrawImage(context, CGRectMake(testSize.width - drawingMargins - imageWidth, testSize.height * 0.5f - imageHeight/2 - punchDistance/2, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(testSize.width - drawingMargins - imageWidth, testSize.height * 0.5f - imageHeight/2 + punchDistance/2, imageWidth, imageHeight), [punchImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage2HolePunchTop
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat punchDistance = 228.0f;
    CGFloat imageWidth = 18.0f;
    CGFloat imageHeight = imageWidth;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    
    CGContextDrawImage(context, CGRectMake(testSize.width * 0.5f - imageWidth/2 - punchDistance/2, testSize.height - imageHeight- drawingMargins, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(testSize.width * 0.5f - imageWidth/2 + punchDistance/2, testSize.height - imageHeight - drawingMargins, imageWidth, imageHeight), [punchImage CGImage]);
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage4HolePunchLeft
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat punchDistance = 252.3f;
    CGFloat imageWidth = 18.0f;
    CGFloat imageHeight = imageWidth;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 - punchDistance * 1.5f, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 - punchDistance * 0.5f, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 + punchDistance * 0.5f, imageWidth, imageHeight), [punchImage CGImage]);
        CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 + punchDistance * 1.5f, imageWidth, imageHeight), [punchImage CGImage]);
    
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

-(UIImage *)getExpectedImage3HolePunchLeft
{
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceRGB();
    CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    CGContextRef context = CGBitmapContextCreate(nil, testSize.width, testSize.height, 8, 0, cs, bitmapInfo);
    CGFloat punchDistance = 306.1f;
    CGFloat imageWidth = 18.0f;
    CGFloat imageHeight = imageWidth;
    
    CGContextTranslateCTM(context, 0, testSize.height);
    CGContextScaleCTM(context, 1.0, -1.0f);
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 - punchDistance, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2, imageWidth, imageHeight), [punchImage CGImage]);
    CGContextDrawImage(context, CGRectMake(drawingMargins, testSize.height * 0.5f - imageHeight/2 + punchDistance, imageWidth, imageHeight), [punchImage CGImage]);
    
    UIImage *expectedImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
    
    CGContextRelease(context);
    CGColorSpaceRelease(cs);
    
    return expectedImage;
}

@end
