//
//  PDFPageView.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFPageView.h"

@implementation PDFPageView
{
    
}
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}




// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    NSLog(@"Draw page view");
    if(self.delegate == nil){
        NSLog(@"delegate is nil");
        return;
    }
    
    // Drawing code
    BOOL isGrayScale = [self.delegate isGrayScale]; //TODO colormode
    if(isGrayScale == YES)
    {
        [self drawInGrayScale:rect];
    }
    else
    {
        CGContextRef ctx = UIGraphicsGetCurrentContext();
        [self drawPDFInCtx:ctx inRect:rect];
    }
}

-(void) drawInGrayScale: (CGRect) rect
{
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
    CGContextRef ctx = CGBitmapContextCreate(nil, rect.size.width, rect.size.height, 8,0 ,colorSpace,(CGBitmapInfo)kCGImageAlphaNone);

    [self drawPDFInCtx:ctx inRect:rect];
    
    CGImageRef imageRef = CGBitmapContextCreateImage(ctx);
    
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(ctx);
    CGContextRef graphicsCtx  = UIGraphicsGetCurrentContext();
    CGContextDrawImage(graphicsCtx, rect, imageRef);
}


-(void) drawPDFInCtx: (CGContextRef) ctx inRect: (CGRect) rect
{
    //get number of pages to draw in sheet
    NSUInteger numPagesPerSheet = [self.delegate getNumPages];
    
    //compute the width and height to be occupied by 1 page in rect
    CGFloat pageWidth = [self getPageWidth: rect forNumOfPages:numPagesPerSheet];
    CGFloat pageHeight = [self getPageHeight: rect forNumOfPages:numPagesPerSheet];
    
    //get first page
    CGPDFPageRef pdfPage = [self.delegate getPage:0];
    
    if(pdfPage == nil){
        NSLog(@"pdf is nil");
    }
    
	// get the rectangle of the pdf page
    CGRect mediaRect = CGPDFPageGetBoxRect(pdfPage, kCGPDFMediaBox);
    
    //get the scale of the original width and height to the width and height to be occuppied by page
    CGFloat widthScaleFactor = pageWidth/mediaRect.size.width;
    CGFloat heightScaleFactor = pageHeight/mediaRect.size.height;
    
    // PDF might be transparent, assume white paper
    CGContextSetFillColorWithColor(ctx, [[UIColor whiteColor]CGColor]);
	CGContextFillRect(ctx, rect);
    
    // Flip coordinates
	CGContextGetCTM(ctx);
	CGContextScaleCTM(ctx, 1, -1);
	CGContextTranslateCTM(ctx, 0, -rect.size.height);
    CGContextSaveGState(ctx);
    
    CGContextScaleCTM(ctx, widthScaleFactor, heightScaleFactor);
    CGContextSaveGState(ctx);
    
    CGFloat xOffset = 0;
    CGFloat yOffset = mediaRect.size.height * ((rect.size.height/pageHeight) - 1);
    
    CGFloat totalWidth = 0;
    for(int i = 0; i < numPagesPerSheet; i++)
    {
        CGContextTranslateCTM(ctx, 0 + xOffset , 0 + yOffset);
        CGContextDrawPDFPage(ctx, pdfPage);
        CGContextRestoreGState(ctx);
        CGContextSaveGState(ctx);
        totalWidth += pageWidth;
        xOffset += mediaRect.size.width;
        if(totalWidth == rect.size.width)
        {
            xOffset = 0;
            totalWidth = 0;
            yOffset -= mediaRect.size.height;
        }
        if((i+1) < numPagesPerSheet)
        {
            pdfPage = [self.delegate getPage:(i+1)];
            if(pdfPage == nil){
                NSLog(@"no more pages");
                break;
            }
        }
    }
}

-(CGFloat) getPageWidth:(CGRect) origRect forNumOfPages: (NSUInteger) numPagesPerSheet
{
    CGFloat pageWidth = origRect.size.width;

    switch(numPagesPerSheet)
    {
        case 2:
        case 4:
            pageWidth /= 2;
            break;
        case 6:
        case 9:
            pageWidth /= 3;
            break;
        case 16:
            pageWidth /= 4;
            break;
    }
    return pageWidth;
}

-(CGFloat) getPageHeight:(CGRect) origRect forNumOfPages: (NSUInteger) numPagesPerSheet
{
    CGFloat pageHeight = origRect.size.height;
    
    switch(numPagesPerSheet)
    {
        case 6:
        case 4:
            pageHeight /= 2;
            break;
        case 9:
            pageHeight /= 3;
            break;
        case 16:
            pageHeight/= 4;
            break;
    }
    return pageHeight;
}

@end
