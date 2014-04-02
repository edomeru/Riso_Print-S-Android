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

@interface PDFRenderOperation()

@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic) CGSize size;
@property (nonatomic, strong) UIImage *image;

@end

@implementation PDFRenderOperation

- (id)initWithPageIndex:(NSInteger)pageIndex size:(CGSize)size delegate:(id<PDFRenderOperationDelegate>)delegate
{
    self = [super init];
    if (self)
    {
        _pageIndex = pageIndex;
        _size = size;
        _delegate = delegate;
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
        
        CGContextRef contextRef = CGBitmapContextCreate(nil, self.size.width, self.size.height, 8, 0, colorSpaceRef, bitmapInfo);
        CGColorSpaceRelease(colorSpaceRef);
        
        if (self.isCancelled)
        {
            CGContextRelease(contextRef);
            return;
        }
        
        CGContextSetRGBFillColor(contextRef, 1.0f, 1.0f, 1.0f, 1.0f);
        CGContextFillRect(contextRef, rect);
        
        [self drawPagesInRect:rect inContext:contextRef];
        
        // Cancel check
        if (self.isCancelled)
        {
            CGContextRelease(contextRef);
            return;
        }
        
        CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
        self.image = [UIImage imageWithCGImage:imageRef scale:1.0f orientation:UIImageOrientationDownMirrored];
        CGImageRelease(imageRef);
        CGContextRelease(contextRef);
        
        [(NSObject *)self.delegate performSelectorOnMainThread:@selector(renderDidDFinish:) withObject:self waitUntilDone:YES];
    }
}

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
            [self drawPage: self.pageIndex + 1 inRect:rect inContext:contextRef];
            break;
    }
}

-(void) drawPage:(NSUInteger)pageNumber inRect:(CGRect)rect inContext: (CGContextRef)contextRef
{
    CGPDFPageRef pageRef = CGPDFDocumentGetPage(self.printDocument.pdfDocument, pageNumber);
    // Cancel check
    if (self.isCancelled)
    {
        return;
    }
    
    CGContextSaveGState(contextRef);
    CGContextTranslateCTM(contextRef, 0.0f, self.size.height);
    CGContextScaleCTM(contextRef, 1.0f, -1.0f);
    CGContextConcatCTM(contextRef, CGPDFPageGetDrawingTransform(pageRef, kCGPDFMediaBox, rect, 0, true));
    CGContextDrawPDFPage(contextRef, pageRef);
    CGContextRestoreGState(contextRef);
}

- (void)draw2In1InContext:(CGContextRef)contextRef
{
    kImpositionOrder order = (kImpositionOrder)self.printDocument.previewSetting.impositionOrder;
    CGFloat rectWidth = self.size.width/2;
    CGFloat rectHeight = self.size.height;
    CGRect leftRect = CGRectMake(0, 0, rectWidth, rectHeight);
    CGRect rightRect = CGRectOffset(leftRect, rectWidth, 0);
    
    NSUInteger pageNumber = (self.pageIndex * 2) + 1;
    
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
    
    CGRect leftBottomRect =CGRectMake(0, 0, rectWidth,rectHeight);
    CGRect rightBottomRect = CGRectOffset(leftBottomRect, rectWidth, 0);
    CGRect leftTopRect = CGRectOffset(leftBottomRect, 0, rectHeight);
    CGRect rightTopRect = CGRectOffset(leftTopRect, rectWidth, 0);
    
    NSUInteger pageNumber = (self.pageIndex * 4) + 1;
    
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

- (void) drawPagesInRects:(NSArray *) rectArray atStartPageNumber:(NSUInteger)pageNumber inContext:(CGContextRef) contextRef
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

@end
