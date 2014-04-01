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
        CGPDFPageRef pageRef = CGPDFDocumentGetPage(self.printDocument.pdfDocument, self.pageIndex + 1);
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
        
        CGContextRef contextRef = CGBitmapContextCreate(nil, self.size.width, self.size.height, 8, 0, colorSpaceRef, bitmapInfo);
        CGColorSpaceRelease(colorSpaceRef);
        
        CGContextSetRGBFillColor(contextRef, 1.0f, 1.0f, 1.0f, 1.0f);
        CGContextFillRect(contextRef, rect);
        
        CGContextSaveGState(contextRef);
        
        CGContextTranslateCTM(contextRef, 0.0f, self.size.height);
        CGContextScaleCTM(contextRef, 1.0f, -1.0f);
        CGContextConcatCTM(contextRef, CGPDFPageGetDrawingTransform(pageRef, kCGPDFMediaBox, rect, 0, true));
        CGContextDrawPDFPage(contextRef, pageRef);
        
        CGContextRestoreGState(contextRef);
        CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
        self.image = [UIImage imageWithCGImage:imageRef scale:1.0f orientation:UIImageOrientationDownMirrored];
        CGImageRelease(imageRef);
        CGContextRelease(contextRef);
        
        [(NSObject *)self.delegate performSelectorOnMainThread:@selector(renderDidDFinish:) withObject:self waitUntilDone:YES];
    }
}


@end
