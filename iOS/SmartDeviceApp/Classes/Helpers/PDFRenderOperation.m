//
//  PDFRenderOperation.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintPreviewHelper.h"
#import "PDFFileManager.h"
#import "UIColor+Theme.h"

@interface PDFRenderOperation()

/**
 * Indices of the pages to be rendered
 */
@property (nonatomic, strong) NSArray *pageIndices;

/**
 * Print Document object
 */
@property (nonatomic, weak) PrintDocument *printDocument;

/**
 * Preview setting of the print document
 */
@property (nonatomic, weak) PreviewSetting *previewSetting;

/**
 * Dimension of the ouput images
 */
@property (nonatomic) CGSize size;

/**
 * Dimension of the paper (in points)
 */
@property (nonatomic) CGSize paperSize;

/**
 * Current that is being rendered
 */
@property (nonatomic) NSUInteger currentPage;

/**
 * Whether or not the current page is a front-facing page (for duplex/booklet modes)
 */
@property (nonatomic) BOOL isFrontPage;

/**
 * Renders PDF pages for the current page
 */
- (void)drawPagesInContext:(CGContextRef)contextRef;

/**
 * Renders 2-in-1 page
 */
- (void)draw2In1InContext:(CGContextRef)contextRef;

/**
 * Renders 4-in-1 page
 */
- (void)draw4In1InContext:(CGContextRef)contextRef;

/**
 * Renders a PDF page based on size and page scaling
 */
- (void)drawPage:(NSUInteger)pageNumber forSize:(CGSize)size withPageScale:(CGFloat)pageScale inContext: (CGContextRef)contextRef;

/**
 * Renders dashed lines for duplex and booklet modes
 */
- (void)drawPaperEdgeLineInContext:(CGContextRef)contextRef;

/**
 * Calculates the scale for length based on area scale
 */
- (CGFloat)computeScaleForLength:(CGFloat)length areaScale:(CGFloat)areaScale;

/**
 * Determines whether or not an image should be inverted (for duplex mode)
 */
- (BOOL)shouldInvertImage;

/**
 * Determines whether or not a rect is landscape
 */
- (BOOL) isSizeLandscape:(CGSize)rect;

/**
 * Gets the rect of the pdf page with applied rotation angle
 */
- (CGRect)getRectForPdfPage:(CGPDFPageRef)pageRef box:(CGPDFBox)box rotation:(int)rotation;

@end

@implementation PDFRenderOperation

#pragma mark - Public Methods

- (id)initWithPageIndexSet:(NSArray *)pageIndices size:(CGSize)size delegate:(id<PDFRenderOperationDelegate>)delegate
{
    self = [super init];
    if (self)
    {
        _pageIndices = pageIndices;
        _size = size;
        _delegate = delegate;
        _images = [[NSMutableDictionary alloc] init];
        _printDocument = [[PDFFileManager sharedManager] printDocument];
        _previewSetting = _printDocument.previewSetting;
    }
    return self;
}

- (void)main
{
    @autoreleasepool
    {
        // Adjust size forr retina display
        CGFloat screenScale = [[UIScreen mainScreen] scale];
        self.size = CGSizeApplyAffineTransform(self.size, CGAffineTransformMakeScale(screenScale, screenScale));
        
        // Compute paper size
        BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.previewSetting];
        self.paperSize = [PrintPreviewHelper getPaperDimensions:(kPaperSize)self.previewSetting.paperSize isLandscape:isLandscape];
        
        // Cancel check
        if (self.isCancelled)
        {
            return;
        }
        
        // Create color space
        CGColorSpaceRef colorSpaceRef;
        CGBitmapInfo bitmapInfo;
        if ([PrintPreviewHelper isGrayScaleColorForColorModeSetting:(kColorMode)self.previewSetting.colorMode])
        {
            colorSpaceRef = CGColorSpaceCreateDeviceGray();
            bitmapInfo = (CGBitmapInfo)kCGImageAlphaNone;
        }
        else
        {
            colorSpaceRef = CGColorSpaceCreateDeviceRGB();
            bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
        }
        
        // Cancel check
        if (self.isCancelled)
        {
            CGColorSpaceRelease(colorSpaceRef);
            return;
        }
        
        // Create bitmap context
        CGContextRef contextRef = CGBitmapContextCreate(nil, self.size.width, self.size.height, 8, 0, colorSpaceRef, bitmapInfo);
        CGColorSpaceRelease(colorSpaceRef);
        
        if (self.isCancelled)
        {
            CGContextRelease(contextRef);
            return;
        }
        
        __weak PDFRenderOperation *weakSelf = self;
        
        // Render pages
        [self.pageIndices enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            __block NSNumber *index = obj;
            self.currentPage = [index unsignedIntegerValue];
            self.isFrontPage = ((self.currentPage % 2) == 0); //front side if index is even
            
            CGContextSaveGState(contextRef);
            
            // Flip transform
            if ([self shouldInvertImage] == NO)
            {
                CGContextConcatCTM(contextRef, CGAffineTransformMake(1.0f, 0.0f, 0.0f, -1.0f, 0.0f, self.size.height));
            }
            else
            {
                CGContextConcatCTM(contextRef, CGAffineTransformMake(-1.0f, 0.0f, 0.0f, 1.0f, self.size.width, 0.0f));
            }
            
            // Clear context with white fill
            CGContextSetFillColorWithColor(contextRef, [[UIColor whiteColor] CGColor]);
            CGContextFillRect(contextRef, CGRectMake(0.0f, 0.0f, self.size.width, self.size.height));
            
            // Render page
            [self drawPagesInContext:contextRef];
            if (self.currentPage > 0 && self.isFrontPage == YES &&
                (self.previewSetting.booklet == YES || self.previewSetting.duplex != kDuplexSettingOff))
            {
                [self drawPaperEdgeLineInContext:contextRef];
            }
            
            // Cancel check
            if (self.isCancelled)
            {
                *stop = YES;
                return;
            }
            
            CGContextRestoreGState(contextRef);
            
            // Create image
            CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
            UIImage *image = [UIImage imageWithCGImage:imageRef scale:screenScale orientation:UIImageOrientationUp];
            [self.images setObject:image forKey:index];
            CGImageRelease(imageRef);
            dispatch_async(dispatch_get_main_queue(), ^(void)
            {
                // Notify delegate that a page has finished rendering
                [weakSelf.delegate renderOperation:self didFinishRenderingImageForPage:index];
            });
        }];
        
        CGContextRelease(contextRef);
        
        // Notify the delegate that the render operation has finished rendering all the pages
        [(NSObject *)self.delegate performSelectorOnMainThread:@selector(renderDidDFinish:) withObject:self waitUntilDone:YES];
    }
}

#pragma mark - Helper Methods

- (void)drawPagesInContext:(CGContextRef)contextRef
{
    CGContextSaveGState(contextRef);
    
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.previewSetting];
    
    // Draw page depending on the following modes that affects output size
    if (self.previewSetting.booklet == YES)
    {
        // Scale the paper size to booklet dimensions
        CGSize paperSize = self.paperSize;
        if (isLandscape == YES)
        {
            paperSize.width /= 2;
        }
        else
        {
            paperSize.height /= 2;
        }
        
        // Scale rendering to paper size
        CGFloat scale = self.size.width / paperSize.width;
        CGContextScaleCTM(contextRef, scale, scale);
        
        // Draw PDF scaled by 0.5 (in booklet, the paper is divided into two)
        [self drawPage:self.currentPage + 1 forSize:paperSize withPageScale:0.5f inContext:contextRef];
    }
    else if (self.previewSetting.imposition == kImposition2Pages)
    {
        // Draw as 2-in-1
        [self draw2In1InContext:contextRef];
    }
    else if (self.previewSetting.imposition == kImposition4pages)
    {
        // Draw as 4-in-1
        [self draw4In1InContext:contextRef];
    }
    else
    {
        // Scale rendering to paper size
        CGFloat scale = self.size.width / self.paperSize.width;
        CGContextScaleCTM(contextRef, scale, scale);
        
        // Draw normal page
        [self drawPage:self.currentPage + 1 forSize:self.paperSize withPageScale:1.0f inContext:contextRef];
    }
    
    CGContextRestoreGState(contextRef);
}

- (void)draw2In1InContext:(CGContextRef)contextRef
{
    NSArray *rects;
    
    // Compute dimensions for each pdf page in a paper
    CGSize layerSize = self.size;
    CGSize paperSize = self.paperSize;
    if (self.previewSetting.orientation == kOrientationPortrait)
    {
        layerSize.width /= 2.0f;
        paperSize.width /= 2.0f;
        
        CGRect leftRect = CGRectMake(0.0f, 0.0f, layerSize.width, layerSize.height);
        CGRect rightRect = CGRectMake(self.size.width / 2.0f, 0.0f, layerSize.width, layerSize.height);
        
        if (self.previewSetting.impositionOrder == kImpositionOrderLeftToRight)
        {
            rects = @[[NSValue valueWithCGRect:leftRect], [NSValue valueWithCGRect:rightRect]];
        }
        else
        {
            rects = @[[NSValue valueWithCGRect:rightRect], [NSValue valueWithCGRect:leftRect]];
        }
    }
    else
    {
        layerSize.height /= 2.0f;
        paperSize.height /= 2.0f;
        
        CGRect topRect = CGRectMake(0.0f, 0.0f, layerSize.width, layerSize.height);
        CGRect bottomRect = CGRectMake(0.0f, self.size.height / 2.0f, layerSize.width, layerSize.height);
        
        if (self.previewSetting.impositionOrder == kImpositionOrderLeftToRight) {
            rects = @[[NSValue valueWithCGRect:topRect], [NSValue valueWithCGRect:bottomRect]];
        } else {
            rects = @[[NSValue valueWithCGRect:bottomRect], [NSValue valueWithCGRect:topRect]];
        }
    }
    
    CGFloat scale = layerSize.width / paperSize.width;
    NSUInteger pageNumber = 1;
    
    for (NSValue *rectValue in rects)
    {
        CGRect rect = [rectValue CGRectValue];
        
        // Create a layer for each pdf page
        CGLayerRef layerRef = CGLayerCreateWithContext(contextRef, layerSize, NULL);
        CGContextRef layerContextRef = CGLayerGetContext(layerRef);
        CGContextScaleCTM(layerContextRef, scale, scale);
        
        // Draw PDF scaled by 0.5 (in 2-up, the paper is divided into two) to the layer
        [self drawPage:(self.currentPage * 2) + pageNumber forSize:paperSize withPageScale:0.5f inContext:layerContextRef];
        
        // Draw layer to the page at the rect origin
        CGContextSaveGState(contextRef);
        CGContextDrawLayerAtPoint(contextRef, rect.origin, layerRef);
        CGContextRestoreGState(contextRef);
        
        CGLayerRelease(layerRef);
        
        pageNumber++;
    }
}

- (void)draw4In1InContext:(CGContextRef)contextRef
{
    NSArray *rects;
    
    CGSize layerSize = self.size;
    CGSize paperSize = self.paperSize;
    
    // Scale sizes to 0.25 (page is divided in to 4)
    layerSize.width /= 2.0f;
    layerSize.height /= 2.0f;
    paperSize.width /= 2.0f;
    paperSize.height /= 2.0f;
    
    CGRect topLeft = CGRectMake(0.0f, 0.0f, layerSize.width / 2.0f, layerSize.height);
    CGRect topRight = CGRectOffset(topLeft, self.size.width / 2.0f, 0.0f);
    CGRect bottomLeft = CGRectOffset(topLeft, 0.0f, self.size.height / 2.0f);
    CGRect bottomRight = CGRectOffset(topLeft, self.size.width / 2.0f, self.size.height / 2.0f);
    
    if (self.previewSetting.impositionOrder == kImpositionOrderUpperLeftToRight)
    {
        rects = @[
                  [NSValue valueWithCGRect:topLeft],
                  [NSValue valueWithCGRect:topRight],
                  [NSValue valueWithCGRect:bottomLeft],
                  [NSValue valueWithCGRect:bottomRight],
                  ];
    }
    else if (self.previewSetting.impositionOrder == kImpositionOrderUpperLeftToBottom)
    {
        rects = @[
                  [NSValue valueWithCGRect:topLeft],
                  [NSValue valueWithCGRect:bottomLeft],
                  [NSValue valueWithCGRect:topRight],
                  [NSValue valueWithCGRect:bottomRight],
                  ];
    }
    else if (self.previewSetting.impositionOrder == kImpositionOrderUpperRightToLeft)
    {
        rects = @[
                  [NSValue valueWithCGRect:topRight],
                  [NSValue valueWithCGRect:topLeft],
                  [NSValue valueWithCGRect:bottomRight],
                  [NSValue valueWithCGRect:bottomLeft],
                  ];
    }
    else
    {
        rects = @[
                  [NSValue valueWithCGRect:topRight],
                  [NSValue valueWithCGRect:bottomRight],
                  [NSValue valueWithCGRect:topLeft],
                  [NSValue valueWithCGRect:bottomLeft],
                  ];
    }
    
    CGFloat scale = layerSize.width / paperSize.width;
    NSUInteger pageNumber = 1;
    for (NSValue *rectValue in rects)
    {
        CGRect rect = [rectValue CGRectValue];
        
        // Create a layer for each pdf page
        CGLayerRef layerRef = CGLayerCreateWithContext(contextRef, layerSize, NULL);
        CGContextRef layerContextRef = CGLayerGetContext(layerRef);
        CGContextScaleCTM(layerContextRef, scale, scale);
        
        // Draw PDF scaled by 0.25 (in 4-up, the paper is divided into four) to the layer
        [self drawPage:(self.currentPage * 4) + pageNumber forSize:paperSize withPageScale:0.25f inContext:layerContextRef];
        
        // Draw layer to the page at the rect origin
        CGContextSaveGState(contextRef);
        CGContextDrawLayerAtPoint(contextRef, rect.origin, layerRef);
        CGContextRestoreGState(contextRef);
        
        CGLayerRelease(layerRef);
        
        pageNumber++;
    }
}

- (void)drawPage:(NSUInteger)pageNumber forSize:(CGSize)size withPageScale:(CGFloat)pageScale inContext:(CGContextRef)contextRef
{
    // Get PDF data
    CGPDFDocumentRef documentRef = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.printDocument.url);
    CGPDFPageRef pageRef = CGPDFDocumentGetPage(documentRef, pageNumber);
    if (pageRef == NULL)
    {
        CGPDFDocumentRelease(documentRef);
        return;
    }
    
    // Get PDF rects
    CGRect pdfRect = CGPDFPageGetBoxRect(pageRef, kCGPDFMediaBox);
    CGRect cropRect = CGPDFPageGetBoxRect(pageRef, kCGPDFCropBox);
    int rotation = CGPDFPageGetRotationAngle(pageRef);
    CGRect rotatedPdfPrect = [self getRectForPdfPage:pageRef box:kCGPDFMediaBox rotation:rotation];
    
    CGContextSaveGState(contextRef);
   
    // Rotate page depending on PDF orientation
    BOOL isPageLandscape = [self isSizeLandscape:size];
    BOOL isPDFLandscape = [self isSizeLandscape:rotatedPdfPrect.size];
    if (isPageLandscape != isPDFLandscape) {
        rotation -= 90;
    }
    
    if (rotation != 0)
    {
        CGContextTranslateCTM(contextRef, size.width / 2.0f, size.height / 2.0f);
        CGContextRotateCTM(contextRef, (rotation * M_PI / 180.0f));
        CGContextTranslateCTM(contextRef, -size.height / 2.0f, -size.width / 2.0f);
        size = CGSizeMake(size.height, size.width);
    }
    
    // Invert Y - required by CGContextDrawPDFPage
    CGContextScaleCTM(contextRef, 1.0f, -1.0f);
    CGContextTranslateCTM(contextRef, 0.0f, -size.height);
    
    if (self.previewSetting.scaleToFit == YES)
    {
        // Pick a scale that will fit the PDF to expected size
        CGFloat xScale = size.width / pdfRect.size.width;
        CGFloat yScale = size.height / pdfRect.size.height;
        CGFloat scale = (xScale < yScale) ? xScale : yScale;
        
        // Apply scale and center-align
        CGAffineTransform scaleTransform = CGAffineTransformMakeScale(scale, scale);
        CGRect scaledPDFRect = CGRectApplyAffineTransform(pdfRect, scaleTransform);
        CGContextTranslateCTM(contextRef, (size.width - scaledPDFRect.size.width) / 2.0f, (size.height - scaledPDFRect.size.height) / 2.0f);
        CGContextConcatCTM(contextRef, scaleTransform);
    }
    else
    {
        // Compute scale for length
        CGFloat scale = [self computeScaleForLength:pdfRect.size.height areaScale:pageScale];
        CGContextTranslateCTM(contextRef, 0, -(pdfRect.size.height * scale - size.height));
        CGContextScaleCTM(contextRef, scale, scale);
    }
    
    // Clip rendering to crop rect of PDF
    CGContextClipToRect(contextRef, cropRect);
    
    // Adjust scale quality and render PDF page
    CGContextSetInterpolationQuality(contextRef, kCGInterpolationHigh);
    CGContextDrawPDFPage(contextRef, pageRef);
    
    CGContextRestoreGState(contextRef);
    
    CGPDFDocumentRelease(documentRef);
}

- (void)drawPaperEdgeLineInContext:(CGContextRef)contextRef
{
    CGPoint startPoint = CGPointZero;
    CGPoint endPoint = CGPointZero;
    
    if (self.previewSetting.booklet == YES)
    {
        if (self.previewSetting.orientation == kOrientationPortrait)
        {
            if (self.previewSetting.bookletLayout == kBookletLayoutForward)
            {
                startPoint = CGPointMake(0.0f, 0.0f);
                endPoint = CGPointMake(0.0f, self.size.height);
            }
            else
            {
                startPoint = CGPointMake(self.size.width, 0.0f);
                endPoint = CGPointMake(self.size.width, self.size.height);
            }
        }
        else
        {
            if (self.previewSetting.bookletLayout == kBookletLayoutForward)
            {
                startPoint = CGPointMake(0.0f, 0.0f);
                endPoint = CGPointMake(self.size.width, 0.0f);
            }
            else
            {
                startPoint = CGPointMake(0.0f, self.size.height);
                endPoint = CGPointMake(self.size.width, self.size.height);
            }
        }
    }
    else if (self.previewSetting.booklet == NO && self.previewSetting.duplex != kDuplexSettingOff)
    {
        if (self.previewSetting.finishingSide == kFinishingSideLeft)
        {
            startPoint = CGPointMake(0.0f, 0.0f);
            endPoint = CGPointMake(0.0f, self.size.height);
        }
        else if (self.previewSetting.finishingSide == kFinishingSideRight)
        {
            startPoint = CGPointMake(self.size.width, 0.0f);
            endPoint = CGPointMake(self.size.width, self.size.height);
        }
        else
        {
            startPoint = CGPointMake(0.0f, 0.0f);
            endPoint = CGPointMake(self.size.width, 0.0f);
        }
    }
    
    CGContextSaveGState(contextRef);
    
    CGContextSetLineWidth(contextRef, 2.0f);
    CGFloat dashLine[] = { 16.0f, 16.0f };
    
    // Draw BG line (white)
    CGContextSetLineDash(contextRef, 16.0f, dashLine, 2);
    CGContextSetStrokeColorWithColor(contextRef, [[UIColor whiteColor] CGColor]);
    CGContextMoveToPoint(contextRef, startPoint.x, startPoint.y);
    CGContextAddLineToPoint(contextRef, endPoint.x, endPoint.y);
    CGContextStrokePath(contextRef);
    
    // Draw dash line (Gray 3)
    CGContextSetLineDash(contextRef, 0.0f, dashLine, 2);
    CGContextSetStrokeColorWithColor(contextRef, [[UIColor gray3ThemeColor] CGColor]);
    CGContextMoveToPoint(contextRef, startPoint.x, startPoint.y);
    CGContextAddLineToPoint(contextRef, endPoint.x, endPoint.y);
    CGContextStrokePath(contextRef);
    
    CGContextRestoreGState(contextRef);
}

- (CGFloat)computeScaleForLength:(CGFloat)length areaScale:(CGFloat)areaScale
{
    return (sqrtf(powf(length, 2) * areaScale) / length);
}

- (BOOL)shouldInvertImage
{
    if(self.isFrontPage == NO && self.printDocument.previewSetting.duplex != kDuplexSettingOff
       && self.printDocument.previewSetting.booklet != YES)
    {
        if(self.printDocument.previewSetting.finishingSide == kFinishingSideTop) //for vertical navigation, ios automatically inverts back image so reverse case
        {
            if(self.printDocument.previewSetting.duplex == kDuplexSettingShortEdge && self.size.width > self.size.height)
            {
                return YES;
            }
            if(self.printDocument.previewSetting.duplex == kDuplexSettingLongEdge && self.size.width < self.size.height)
            {
                return YES;
            }
        }
        else if(self.printDocument.previewSetting.duplex == kDuplexSettingShortEdge && self.size.width < self.size.height)
        {
            return YES;
        }
        else if(self.printDocument.previewSetting.duplex == kDuplexSettingLongEdge && self.size.width > self.size.height)
        {
            return YES;
        }
    }
    return NO;
}

- (BOOL) isSizeLandscape:(CGSize)size
{
    return (size.width > size.height);
}

- (CGRect)getRectForPdfPage:(CGPDFPageRef)pageRef box:(CGPDFBox)box rotation:(int)rotation
{
    CGRect pageRect = CGPDFPageGetBoxRect(pageRef, box);
    if ((rotation % 180) > 0)
    {
        CGFloat temp = pageRect.size.width;
        pageRect.size.width = pageRect.size.height;
        pageRect.size.height = temp;
    }
    
    return pageRect;
}

@end
