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
#import "Printer.h"

#define FINISHING_MARGIN  	10.0f
//approximate staple and punch dimensions in points
#define STAPLE_TOP_WIDTH        30.0f
#define STAPLE_SIDE_WIDTH   	5.0f
#define STAPLE_SIDE_HEIGHT  	42.4f //staple height when 
#define PUNCH_WIDTH             18.0f

//punch hole distance in points (converted from mm at 72dpi)
#define PUNCH_2HOLE_DISTANCE 	228.0f
#define PUNCH_3HOLE_DISTANCE 	306.1f 
#define PUNCH_4HOLE_DISTANCE 	252.3f

@interface PDFRenderOperation()

@property (nonatomic, strong) NSArray *pageIndices;
/**
 Print Document object
 */
@property (nonatomic, weak) PrintDocument *printDocument;

/**
 Dimension of the ouput images
 */
@property (nonatomic) CGSize size;

/**
 Current that is being rendered
 */
@property (nonatomic) NSUInteger currentPage;

/**
 
 */
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
    }
    return self;
}

- (void)main
{
    @autoreleasepool
    {
        CGRect rect = CGRectMake(0.0f, 0.0f, self.size.width, self.size.height);
       
        // Cancel check
        if (self.isCancelled)
        {
            return;
        }
        
        // Create color space
        CGColorSpaceRef colorSpaceRef;
        CGBitmapInfo bitmapInfo;
        if ([PrintPreviewHelper isGrayScaleColorForColorModeSetting:(kColorMode)self.printDocument.previewSetting.colorMode])
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
        
        
        // Render pages
        [self.pageIndices enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSNumber *index = obj;
            self.currentPage = [index unsignedIntegerValue];
            self.isFrontPage = ((self.currentPage % 2) == 0); //front side if index is even
            
            // Clear context with white fill
            CGContextSetRGBFillColor(contextRef, 1.0f, 1.0f, 1.0f, 1.0f);
            CGContextFillRect(contextRef, rect);
            
            // Flip transform
            CGContextSaveGState(contextRef);
            CGContextTranslateCTM(contextRef, 0.0f, self.size.height);
            CGContextScaleCTM(contextRef, 1.0f, -1.0f);
            
            // Render page
            [self drawPagesInRect:rect inContext:contextRef];
            
            // Cancel check
            if (self.isCancelled)
            {
                *stop = YES;
                return;
            }
            
            // Render finishing
            [self drawFinishing:contextRef];
            
            if (self.isCancelled)
            {
                *stop = YES;
                return;
            }
            
            CGContextRestoreGState(contextRef);
            
            // Create image
            CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
            UIImageOrientation imageOrientation = UIImageOrientationDownMirrored;
            if([self shouldInvertImage] == YES)
            {
                imageOrientation = UIImageOrientationUpMirrored;
            }
            UIImage *image = [UIImage imageWithCGImage:imageRef scale:1.0f orientation:imageOrientation];
            [self.images setObject:image forKey:index];
            CGImageRelease(imageRef);
            dispatch_sync(dispatch_get_main_queue(), ^(void)
            {
                // Notify delegate that a page has finished rendering
                [self.delegate renderOperation:self didFinishRenderingImageForPage:index];
            });
        }];
        
        CGContextRelease(contextRef);
        
        // Notify the delegate that the render operation has finished rendering all the pages
        [(NSObject *)self.delegate performSelectorOnMainThread:@selector(renderDidDFinish:) withObject:self waitUntilDone:YES];
    }
}

#pragma mark - Helper Methods

- (void)drawPagesInRect:(CGRect)rect inContext:(CGContextRef)contextRef
{
    kImposition imposition = (kImposition)self.printDocument.previewSetting.imposition;
    
    if(self.printDocument.previewSetting.booklet == YES)
    {
        imposition = kImpositionOff;
    }
    
    switch(imposition)
    {
        case kImposition2Pages:
            [self draw2In1InContext:contextRef];
            break;
        case kImposition4pages:
            [self draw4In1InContext:contextRef];
            break;
        default:
            [self drawPage: self.currentPage + 1 inRect:rect inContext:contextRef];
            break;
    }
}

- (void)drawPage:(NSUInteger)pageNumber inRect:(CGRect)rect inContext:(CGContextRef)contextRef
{
    CGPDFDocumentRef documentRef = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.printDocument.url);
    //CGPDFPageRef pageRef = CGPDFDocumentGetPage(self.printDocument.pdfDocument, pageNumber);
    CGPDFPageRef pageRef = CGPDFDocumentGetPage(documentRef, pageNumber);
    // Cancel check
    if (self.isCancelled)
    {
        CGPDFDocumentRelease(documentRef);
        return;
    }
    
    CGContextSaveGState(contextRef);
    //get the rect of pdf to know actual pdf size in points (which is at 72 ppi)
    CGRect pdfRect = CGPDFPageGetBoxRect(pageRef, kCGPDFMediaBox);
    
    if(self.printDocument.previewSetting.scaleToFit == YES ||
       self.printDocument.previewSetting.booklet == YES ||
       self.printDocument.previewSetting.imposition != kImpositionOff) //ScaleToFit is on or if there is booklet or imposition
    {
        //self.size is actual size of paper at 72ppi
        //check if paper is larger than pdf size.
        //if paper is larger than pdf, pdf must be scaled up to occupy whole paper but still retaining aspect ratio of pdf image
        if(pdfRect.size.height < rect.size.height && pdfRect.size.width < rect.size.width)
        {
            //use the ratio from the side with less difference to the original size of the pdf
            CGFloat scaleRatio  = rect.size.width/pdfRect.size.width;
            CGFloat heightScaleRatio = rect.size.height/pdfRect.size.height;
            if(scaleRatio > heightScaleRatio)
            {
                scaleRatio = heightScaleRatio;
            }
            rect.size.height/= scaleRatio;
            rect.size.width /= scaleRatio;
            CGContextScaleCTM(contextRef, scaleRatio, scaleRatio);
        }
   
        //draw pdf at the center of the paper
        CGContextConcatCTM(contextRef, CGPDFPageGetDrawingTransform(pageRef, kCGPDFMediaBox, rect, 0, true));
    }
    else
    {
        //Not scale to fit
        //translate the origin so the upper left corner of the pdf coincides to the upper left corner of the paper/rect
        CGContextTranslateCTM(contextRef, rect.origin.x, rect.origin.y);
        CGContextTranslateCTM(contextRef, 0, -(pdfRect.size.height - rect.size.height));
    }
    
    CGContextDrawPDFPage(contextRef, pageRef);
    CGContextRestoreGState(contextRef);
    CGPDFDocumentRelease(documentRef);
}

- (void)draw2In1InContext:(CGContextRef)contextRef
{
    kImpositionOrder order = (kImpositionOrder)self.printDocument.previewSetting.impositionOrder;
    CGFloat rectWidth = self.size.width/2;
    CGFloat rectHeight = self.size.height;
    CGFloat xOffset = rectWidth;
    CGFloat yOffset = 0;
    CGFloat yPos = 0;
    
    if(self.printDocument.previewSetting.orientation == kOrientationLandscape)
    {
        rectHeight = self.size.height/2;
        rectWidth = self.size.width;
        yOffset = -rectHeight;
        xOffset = 0;
        yPos = rectHeight;
    }
    
    CGRect leftRect = CGRectMake(0, yPos, rectWidth, rectHeight);
    CGRect rightRect = CGRectOffset(leftRect, xOffset, yOffset);
    
    NSUInteger pageNumber = (self.currentPage * 2) + 1;
    
    NSArray *rectArray = nil;
    
    if(order == kImpositionOrderRightToLeft)
    {
        rectArray = [NSArray arrayWithObjects:
                        [NSValue valueWithCGRect:rightRect],
                        [NSValue valueWithCGRect:leftRect],
                        nil];
    }
    else
    {
        rectArray = [NSArray arrayWithObjects:
                        [NSValue valueWithCGRect:leftRect],
                        [NSValue valueWithCGRect:rightRect],
                        nil];
    }
    [self drawPagesInRects:rectArray atStartPageNumber:pageNumber inContext:contextRef];
}

- (void)draw4In1InContext:(CGContextRef)contextRef
{
    kImpositionOrder order = (kImpositionOrder)self.printDocument.previewSetting.impositionOrder;
    CGFloat rectWidth = self.size.width/2;
    CGFloat rectHeight = self.size.height/2;
    
    CGRect leftBottomRect = CGRectMake(0, 0, rectWidth,rectHeight);
    CGRect rightBottomRect = CGRectOffset(leftBottomRect, rectWidth, 0);
    CGRect leftTopRect = CGRectOffset(leftBottomRect, 0, rectHeight);
    CGRect rightTopRect = CGRectOffset(leftTopRect, rectWidth, 0);
    
    NSUInteger pageNumber = (self.currentPage * 4) + 1;
    
    NSArray *rectArray = nil;
    if(order == kImpositionOrderUpperLeftToBottom)
    {
        rectArray = [NSArray arrayWithObjects:
                        [NSValue valueWithCGRect:leftTopRect],
                        [NSValue valueWithCGRect:leftBottomRect],
                        [NSValue valueWithCGRect:rightTopRect],
                        [NSValue valueWithCGRect:rightBottomRect],
                        nil];
    }
    else if(order == kImpositionOrderUpperRightToBottom)
    {
        rectArray = [NSArray arrayWithObjects:
                     [NSValue valueWithCGRect:rightTopRect],
                     [NSValue valueWithCGRect:rightBottomRect],
                     [NSValue valueWithCGRect:leftTopRect],
                     [NSValue valueWithCGRect:leftBottomRect],
                     nil];
    }
    else if(order == kImpositionOrderUpperRightToLeft)
    {
        rectArray = [NSArray arrayWithObjects:
                     [NSValue valueWithCGRect:rightTopRect],
                     [NSValue valueWithCGRect:leftTopRect],
                     [NSValue valueWithCGRect:rightBottomRect],
                     [NSValue valueWithCGRect:leftBottomRect],
                     nil];
   }
   else
   {
       rectArray = [NSArray arrayWithObjects:
                    [NSValue valueWithCGRect:leftTopRect],
                    [NSValue valueWithCGRect:rightTopRect],
                    [NSValue valueWithCGRect:leftBottomRect],
                    [NSValue valueWithCGRect:rightBottomRect],
                    nil];
   }
    
    [self drawPagesInRects:rectArray atStartPageNumber:pageNumber inContext:contextRef];
}

- (void)drawPagesInRects:(NSArray *)rectArray atStartPageNumber:(NSUInteger)pageNumber inContext:(CGContextRef)contextRef
{
    for(int i = 0; i < rectArray.count; i++)
    {
        CGRect rect = [(NSValue *)[rectArray objectAtIndex:i] CGRectValue];
        [self drawPage:pageNumber+i inRect:rect inContext:contextRef];
        
        // Cancel check
        if (self.isCancelled)
        {
            return;
        }
    }
}

- (void)drawFinishing:(CGContextRef)contextRef
{
    //For booklet finishing
    if(self.printDocument.previewSetting.booklet == YES)
    {
        if(self.printDocument.previewSetting.bookletFinish == kBookletTypeFoldAndStaple &&
           self.isFrontPage)
        {
            if(self.printDocument.previewSetting.orientation == kOrientationPortrait)
            {
                [self drawStaple2Pos:contextRef atFinishingSide:kFinishingSideLeft withMargin:0];
            }
            else
            {
                [self drawStaple2Pos:contextRef atFinishingSide:kFinishingSideTop withMargin:0];
            }
        }
        return;
    }
    
    //For duplex, adjust the context  for the correct location of the staple and punch in the backside of the paper
    if(self.printDocument.previewSetting.duplex > kDuplexSettingOff)
    {
        if(self.isFrontPage == NO)
        {
            if(([self shouldInvertImage] == NO && self.printDocument.previewSetting.finishingSide != kFinishingSideTop) ||
               ([self shouldInvertImage] == YES && self.printDocument.previewSetting.finishingSide == kFinishingSideTop))
            {
                //flip context horizontally so that finishing marks in the left will be drawn at the right and vice versa
                CGContextTranslateCTM(contextRef, self.size.width, 0);
                CGContextScaleCTM(contextRef, -1.0f, 1.0f);
            }
            if(([self shouldInvertImage] == YES && self.printDocument.previewSetting.finishingSide != kFinishingSideTop) ||
               ([self shouldInvertImage] == NO && self.printDocument.previewSetting.finishingSide == kFinishingSideTop))
            {
                //flip context vertically so that finishing at the top will be drawn at the bottom
                CGContextTranslateCTM(contextRef, 0, self.size.height);
                CGContextScaleCTM(contextRef, 1.0f, -1.0f);
            }
        }
        else
        {
            if(self.currentPage > 0) //don't draw on the first page
            {
                [self drawPaperEdgeLine:contextRef];
            }
        }
    }
    
    kFinishingSide finishingSide = (kFinishingSide) self.printDocument.previewSetting.finishingSide;
    kStapleType stapleType= (kStapleType)self.printDocument.previewSetting.staple;
    if(stapleType > kStapleTypeNone)
    {
        if(stapleType == kStapleType2Pos)
        {
            [self drawStaple2Pos:contextRef atFinishingSide:finishingSide withMargin:FINISHING_MARGIN];
        }
        else
        {
            [self drawStapleSingle:contextRef withStapleType:stapleType atFinishingSide:finishingSide];
        }
    }
    
    kPunchType punchType = (kPunchType)self.printDocument.previewSetting.punch;
    if(punchType > kPunchTypeNone)
    {
        [self drawPunch:contextRef withPunchType:punchType atFinishingSide: finishingSide];
    }
}

- (void)drawStapleSingle:(CGContextRef)contextRef withStapleType:(kStapleType)stapleType atFinishingSide:(kFinishingSide)finishingSide
{
    CGFloat xPos = FINISHING_MARGIN;
    CGFloat yPos = self.size.height - STAPLE_TOP_WIDTH - FINISHING_MARGIN;
    NSString *stapleImageName = @"img_staple_left_top";
    
    if(stapleType == kStapleTypeUpperRight || (stapleType == kStapleType1Pos && finishingSide == kFinishingSideRight))
    {
        xPos = self.size.width - FINISHING_MARGIN - STAPLE_TOP_WIDTH;
        stapleImageName = @"img_staple_right_top";
    }
    
    UIImage *stapleImage = [UIImage imageNamed:stapleImageName];
    CGRect stapleRect = CGRectMake(xPos, yPos, STAPLE_TOP_WIDTH, STAPLE_TOP_WIDTH);
    CGContextDrawImage(contextRef, stapleRect, [stapleImage CGImage]);
}

- (void)drawStaple2Pos:(CGContextRef)contextRef atFinishingSide:(kFinishingSide)finishingSide withMargin:(CGFloat)margin
{
    CGFloat xPos = FINISHING_MARGIN;
    CGFloat yPos = (self.size.height * 0.25f) - (STAPLE_SIDE_WIDTH * 0.5f);
    CGFloat xOffset = 0;
    CGFloat yOffset = self.size.height * 0.5f;
    CGFloat stapleRectHeight = STAPLE_SIDE_HEIGHT;
    CGFloat stapleRectWidth = STAPLE_SIDE_WIDTH;
    NSString *stapleImageName = @"img_staple_left";
    
    if( finishingSide == kFinishingSideRight)
    {
        xPos = self.size.width - margin;
        stapleImageName = @"img_staple_right";
    }
    else if ( finishingSide == kFinishingSideTop)
    {
        stapleRectWidth = STAPLE_SIDE_HEIGHT;
        stapleRectHeight = STAPLE_SIDE_WIDTH;
        xPos = (self.size.width * 0.25f) - (stapleRectWidth * 0.5f);
        yPos = self.size.height - margin - stapleRectHeight;
        xOffset = (self.size.width * 0.5f);
        yOffset = 0;
        stapleImageName = @"img_staple_top";
    }
    
    UIImage *stapleImage = [UIImage imageNamed:stapleImageName];
    CGRect stapleRect = CGRectMake(xPos, yPos, stapleRectWidth, stapleRectHeight);
    CGContextDrawImage(contextRef, stapleRect, [stapleImage CGImage]);
    
    // Cancel check
    if (self.isCancelled)
    {
        return;
    }
    
    CGContextDrawImage(contextRef, CGRectOffset(stapleRect, xOffset, yOffset), [stapleImage CGImage]);
}

- (void)drawPunch:(CGContextRef)contextRef withPunchType:(kPunchType)punchType atFinishingSide:(kFinishingSide)finishingSide
{
    BOOL isHorizontalLength = NO;
    CGFloat edgeLength = self.size.height;
    CGFloat xPos = FINISHING_MARGIN;
    CGFloat yPos =  0;
    
    if(finishingSide == kFinishingSideRight)
    {
        xPos = self.size.width - FINISHING_MARGIN - PUNCH_WIDTH;
    }
    else if(finishingSide == kFinishingSideTop)
    {
        yPos = self.size.height - FINISHING_MARGIN - PUNCH_WIDTH;
        xPos = 0;
        isHorizontalLength = YES;
        edgeLength = self.size.width;
    }
    
    CGFloat xOffset =0;
    CGFloat yOffset = 0;
    CGFloat startDistanceFromCenter = 0;
    CGFloat punchDistance = 0;
    NSUInteger numHoles = 0;

    if(punchType == kPunchType3or4Holes)
    {
        if([self.printDocument.printer.enabled_punch_3holes boolValue] == YES)
        {
            punchDistance = PUNCH_3HOLE_DISTANCE;
            //center of the first hole is 1 punch distance from the center of the length of the finishing side
            startDistanceFromCenter = punchDistance + (PUNCH_WIDTH * 0.5f);
            numHoles = 3;
        }
        else
        {
            punchDistance = PUNCH_4HOLE_DISTANCE;
            //center of the first hole is 1 and half the punch distance from the center of the length of the finishing side
            startDistanceFromCenter = (punchDistance * 1.5f) + (PUNCH_WIDTH * 0.5f);
            numHoles = 4;
        }
    }
    else
    {
        punchDistance = PUNCH_2HOLE_DISTANCE;
        //center of the first hole is half the punch distance from the center of the length of the finishing side
        startDistanceFromCenter =  (punchDistance * 0.5f) + (PUNCH_WIDTH * 0.5f);
        numHoles = 2;
    }
    
    if(isHorizontalLength == YES)
    {
        xOffset = punchDistance;
        yOffset = 0;
        xPos = (edgeLength * 0.5) - startDistanceFromCenter;
    }
    else
    {
        xOffset = 0;
        yOffset = punchDistance;
        yPos =  (edgeLength * 0.5) - startDistanceFromCenter;
    }
    
    UIImage *punchImage = [UIImage imageNamed:@"img_punch"];
    CGRect paperRec = CGRectMake(0, 0, self.size.width,self.size.height);
    
    for(int i = 0; i < numHoles; i++)
    {
        CGRect punchRect = CGRectMake(xPos, yPos, PUNCH_WIDTH, PUNCH_WIDTH);
        if(CGRectContainsRect(paperRec, punchRect) == YES)
        {
            CGContextDrawImage(contextRef, punchRect, [punchImage CGImage]);
        }
        // Cancel check
        if (self.isCancelled)
        {
            return;
        }
        xPos += xOffset;
        yPos += yOffset;
    }
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

- (void)drawPaperEdgeLine:(CGContextRef)contextRef
{
    CGContextSaveGState(contextRef);
    
    CGFloat lineWidth = 2.0f;
    CGContextSetStrokeColorWithColor(contextRef, [UIColor blackColor].CGColor);
    CGContextSetLineWidth(contextRef, lineWidth);
    
    float dashLine[] = { 6, 5 };
    CGContextSetLineDash(contextRef, 0, dashLine, 2);
    
    if(self.printDocument.previewSetting.finishingSide == kFinishingSideTop)
    {
        CGContextMoveToPoint(contextRef, 0, self.size.height - lineWidth);
        CGContextAddLineToPoint(contextRef, self.size.width, self.size.height - lineWidth);
    }
    else if(self.printDocument.previewSetting.finishingSide == kFinishingSideRight)
    {
        CGContextMoveToPoint(contextRef, self.size.width - lineWidth, 0);
        CGContextAddLineToPoint(contextRef, self.size.width - lineWidth, self.size.height);
    }
    else
    {
        CGContextMoveToPoint(contextRef, 0, 0);
        CGContextAddLineToPoint(contextRef, 0, self.size.height);
    }
    
    CGContextStrokePath(contextRef);
    
    CGContextRestoreGState(contextRef);
}

@end
