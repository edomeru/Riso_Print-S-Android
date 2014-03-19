//
//  PDFPageView.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFPageView.h"
@interface PDFPageView()
@property BOOL isFirstDraw;
@end
@implementation PDFPageView
{
    CGFloat _initialFrameHeight;
}
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

-(id) initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        self.isFirstDraw = TRUE;
    }
    return self;
}

-(void) layoutSubviews
{
    [super layoutSubviews];
    NSLog(@"layout subviews");
    
    if(self.isFirstDraw == NO)
    {
        /* redraw view if the view is initially drawn in a smaller frame (i.e the app is initally launched in landscape)
         to the adapt the image to the bigger frame so it will not pixilate*/
        if(_initialFrameHeight < self.frame.size.height)
        {
            //set the frame to the larger frame size so it will not redraw the view
            _initialFrameHeight = self.frame.size.height;
            [self setNeedsDisplay];
        }
    }
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
    if(self.isFirstDraw == YES)
    {
        self.isFirstDraw = NO;
        _initialFrameHeight = self.frame.size.height;
    }
    // Drawing code
    [self drawPage:rect];
}

-(void) drawPDFPage: (CGContextRef) graphicsCtx  inRect: (CGRect) rect forPageNumber:(NSUInteger) pageNumber inGrayScale: (BOOL) isGrayScale;
{
    CGPDFPageRef pdfPage = [self.delegate getPage:pageNumber];
    
    //get the PDF box image
    CGRect mediaRect = CGPDFPageGetBoxRect(pdfPage, kCGPDFMediaBox);
  
    //create a bitmap context
    CGColorSpaceRef colorSpace = [self getColorSpace:isGrayScale];
    CGContextRef ctx = CGBitmapContextCreate(nil, mediaRect.size.width, mediaRect.size.height, 8,0,colorSpace,[self getBitmapInfo:isGrayScale]);
    
    //Fill with white
    CGContextSetFillColorWithColor(ctx, [[UIColor whiteColor]CGColor]);
	CGContextFillRect(ctx, mediaRect);
    
    //Flip the coordinate axis
    CGContextGetCTM(ctx);
	CGContextScaleCTM(ctx, 1, -1);
    CGContextTranslateCTM(ctx, 0, -mediaRect.size.height);
    
    //draw page in context
    CGContextDrawPDFPage(ctx, pdfPage);
    
    //transform to image
    CGImageRef imageRef = CGBitmapContextCreateImage(ctx);
    
    //release the color space and bitmap context
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(ctx);
    
    //draw the image in rect
    CGContextDrawImage(graphicsCtx, rect, imageRef);
    
    //release the image
    CGImageRelease(imageRef);
}

-(void) drawPage: (CGRect) rect
{
    NSUInteger numPagesPerSheet = [self.delegate getNumPages];
    
    BOOL isGrayScale = [self.delegate isGrayScale];
    
    //compute the width and height to be occupied by 1 page in rect
    CGFloat pageWidth = [self getPageWidth: rect forNumOfPages:numPagesPerSheet];
    CGFloat pageHeight = [self getPageHeight: rect forNumOfPages:numPagesPerSheet];
    
    //get the graphics context
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(ctx, [[UIColor whiteColor]CGColor]);
	CGContextFillRect(ctx, rect);
    
    //draw the pages
    CGFloat xOffset = 0;
    CGFloat yOffset = 0;
    for(int i = 0; i < numPagesPerSheet; i++)
    {
        CGRect pageRect = CGRectMake(xOffset, yOffset, pageWidth, pageHeight);
        [self drawPDFPage:ctx inRect:pageRect forPageNumber:i inGrayScale:isGrayScale];
        xOffset += pageWidth;
        if(xOffset == rect.size.width)
        {
            xOffset = 0;
            yOffset += pageHeight;
        }
    }
}

-(CGColorSpaceRef) getColorSpace: (BOOL) isGrayScale
{
    if(isGrayScale == YES)
    {
        return CGColorSpaceCreateDeviceGray();
    }
    return CGColorSpaceCreateDeviceRGB();
}

-(CGBitmapInfo) getBitmapInfo: (BOOL) isGrayScale
{
    if(isGrayScale == YES)
    {
        return (CGBitmapInfo)kCGImageAlphaNone;
    }
    return (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
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
