//
//  PDFRenderOperation.m
//  SmartDeviceApp
//
//  Created by Seph on 4/1/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintPreviewHelper.h"
#import "PDFFileManager.h"

#define FINISHING_MARGIN  	10.0f
//approximate staple and punch dimensions in points
#define STAPLE_TOP_WIDTH  	30.0f
#define STAPLE_SIDE_WIDTH   	5.0f
#define STAPLE_SIDE_HEIGHT  	42.4f //staple height when 
#define PUNCH_WIDTH  		18.0f 

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

- (void)drawPagesInRect:(CGRect)rect inContext:(CGContextRef)contextRef;
- (void)drawPage:(NSUInteger)pageNumber inRect:(CGRect)rect inContext:(CGContextRef)contextRef;
- (void)draw2In1InContext:(CGContextRef)contextRef;
- (void)draw4In1InContext:(CGContextRef)contextRef;
- (void)drawPagesInRects:(NSArray *)rectArray atStartPageNumber:(NSUInteger)pageNumber inContext:(CGContextRef)contextRef;
- (void)drawFinishing:(CGContextRef)contextRef;
- (void)drawStapleSingle:(CGContextRef)contextRef withStapleType:(kStapleType)stapleType atFinishingSide:(kFinishingSide)finishingSide;
- (void)drawStaple2Pos:(CGContextRef)contextRef atFinishingSide:(kFinishingSide)finishingSide;
- (void)drawPunch:(CGContextRef)contextRef withPunchType:(kPunchType)punchType atFinishingSide:(kFinishingSide)finishingSide;

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
        
        // Flip transform
        CGContextTranslateCTM(contextRef, 0.0f, self.size.height);
        CGContextScaleCTM(contextRef, 1.0f, -1.0f);
        
        // Render pages
        [self.pageIndices enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSNumber *index = obj;
            self.currentPage = [index unsignedIntegerValue];
            
            // Clear context with white fill
            CGContextSetRGBFillColor(contextRef, 1.0f, 1.0f, 1.0f, 1.0f);
            CGContextFillRect(contextRef, rect);
            
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
            
            // Create image
            CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
            UIImage *image = [UIImage imageWithCGImage:imageRef scale:1.0f orientation:UIImageOrientationDownMirrored];
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
    CGContextConcatCTM(contextRef, CGPDFPageGetDrawingTransform(pageRef, kCGPDFMediaBox, rect, 0, true));
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
    kFinishingSide finishingSide = (kFinishingSide) self.printDocument.previewSetting.finishingSide;
    
    kStapleType stapleType= (kStapleType)self.printDocument.previewSetting.staple;
    if(stapleType > kStapleTypeNone)
    {
        if(stapleType == kStapleType2Pos)
        {
            [self drawStaple2Pos:contextRef atFinishingSide:finishingSide];
        }
        else
        {
            [self drawStapleSingle:contextRef withStapleType:stapleType atFinishingSide:finishingSide];
        }
        return;
    }
    
    kPunchType punchType = (kPunchType)self.printDocument.previewSetting.punch;
    
    if(punchType > kPunchTypeNone)
    {
        [self drawPunch:contextRef withPunchType:punchType atFinishingSide: finishingSide];
    }
    
}

- (void)drawStapleSingle:(CGContextRef)contextRef withStapleType:(kStapleType)stapleType  atFinishingSide:(kFinishingSide)finishingSide
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

- (void)drawStaple2Pos:(CGContextRef)contextRef atFinishingSide:(kFinishingSide)finishingSide
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
        xPos = self.size.width - FINISHING_MARGIN;
        stapleImageName = @"img_staple_right";
    }
    else if ( finishingSide == kFinishingSideTop)
    {
        stapleRectWidth = STAPLE_SIDE_HEIGHT;
        stapleRectHeight = STAPLE_SIDE_WIDTH;
        xPos = (self.size.width * 0.25f) - (stapleRectWidth * 0.5f);
        yPos = self.size.height - FINISHING_MARGIN - stapleRectHeight;
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

    if(punchType == kPunchType3Holes)
    {
        punchDistance = PUNCH_3HOLE_DISTANCE;
        //center of the first hole is 1 punch distance from the center of the length of the finishing side
        startDistanceFromCenter = punchDistance + (PUNCH_WIDTH * 0.5f);
        numHoles = 3;
    }
    else if(punchType == kPunchType4Holes)
    {
        punchDistance = PUNCH_4HOLE_DISTANCE;
        //center of the first hole is 1 and half the punch distance from the center of the length of the finishing side
        startDistanceFromCenter = (punchDistance * 1.5f) + (PUNCH_WIDTH * 0.5f);
        numHoles = 4;
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

@end
