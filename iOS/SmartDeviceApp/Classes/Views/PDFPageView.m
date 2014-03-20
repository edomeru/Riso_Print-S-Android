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
    if(self.datasource == nil){
        NSLog(@"delegate is nil");
        return;
    }
    //detect first time to draw view
    if(self.isFirstDraw == YES)
    {
        self.isFirstDraw = NO;
        _initialFrameHeight = self.frame.size.height;
    }
    // Drawing code
    [self drawView:rect];
}

/*Draw the view of the PDFPageView*/
-(void) drawView: (CGRect) rect
{
    //Get the settings needed in the drawing of view
    //Number of pages per sheet
    NSUInteger numPagesPerSheet = [self.datasource getNumPages];
    //Color mode
    BOOL isGrayScale = [self.datasource isGrayScale];
    
    //compute the width and height to be occupied by 1 page in rect based on number of pages per sheet
    CGFloat pageWidth = [self getPageWidth: rect forNumOfPages:numPagesPerSheet];
    CGFloat pageHeight = [self getPageHeight: rect forNumOfPages:numPagesPerSheet];
    
    //get the current graphics context
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(ctx, [[UIColor whiteColor]CGColor]);
	CGContextFillRect(ctx, rect);
    
    //draw the pages incorporating imposition/pagination
    CGFloat xOffset = 0;
    CGFloat yOffset = 0;
    //drawing is from left to right, top to bottom
    for(int i = 0; i < numPagesPerSheet; i++)
    {
        CGRect pageRect = CGRectMake(xOffset, yOffset, pageWidth, pageHeight);
        [self drawPDFPage:ctx inRect:pageRect forPageNumber:i inGrayScale:isGrayScale];
        xOffset += pageWidth;
        if(fabsf(xOffset - rect.size.width) < pageWidth)//if the difference of the total width and accumulated width is 0 or less than a pageWidth, the row has been filled, go to the next row
        {
            xOffset = 0;
            yOffset += pageHeight;
        }
    }
    //TODO: incorporation of the following settings: Staple,Punch, Impostion order, Scale to Fit Off
}

/*Draw a PDF page in rect*/
-(void) drawPDFPage: (CGContextRef) graphicsCtx  inRect: (CGRect) rect forPageNumber:(NSUInteger) pageNumber inGrayScale: (BOOL) isGrayScale;
{
    CGPDFPageRef pdfPage = [self.datasource getPage:pageNumber];
    
    if(pdfPage == nil)
    {
        return;
    }
    
    //get the PDF box image
    CGRect mediaRect = CGPDFPageGetBoxRect(pdfPage, kCGPDFMediaBox);
  
    //create a bitmap context
    CGColorSpaceRef colorSpace = [self getColorSpace:isGrayScale];
    CGContextRef bitmapCtx = CGBitmapContextCreate(nil, mediaRect.size.width, mediaRect.size.height, 8,0,colorSpace,[self getBitmapInfo:isGrayScale]);
    
    //Fill with white because pdf white image is transparent
    CGContextSetFillColorWithColor(bitmapCtx, [[UIColor whiteColor]CGColor]);
	CGContextFillRect(bitmapCtx, mediaRect);
    
    //Flip the coordinate axis
    CGContextGetCTM(bitmapCtx);
	CGContextScaleCTM(bitmapCtx, 1, -1);
    CGContextTranslateCTM(bitmapCtx, 0, -mediaRect.size.height);
    
    //draw page in bitmap context
    CGContextDrawPDFPage(bitmapCtx, pdfPage);
    
    //transform to image
    CGImageRef imageRef = CGBitmapContextCreateImage(bitmapCtx);
    
    //release the color space and bitmap context
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(bitmapCtx);
    
    //create a rect where image will be drawn
    CGRect rectToDraw = [self createScaleToFitRectInDisplayRect:rect withPDFRect:mediaRect];
  
    //draw the image in current graphics context
    CGContextDrawImage(graphicsCtx, rectToDraw, imageRef);
    
    //release the image
    CGImageRelease(imageRef);
}

/*Create rect for scale to fit*/
-(CGRect) createScaleToFitRectInDisplayRect: (CGRect) displayRect withPDFRect:  (CGRect) pdfRect
{
    CGRect scaleToFitRect = displayRect;
    
    /*Scale to fit processing  - pdf page image will retain it's shape and is centered in rect*/
    //get the ratio of the width and height of display rect and of pdf rect
    CGFloat pdfWidthHeightRatio = pdfRect.size.width / pdfRect.size.height;
    CGFloat rectWidthHeightRatio= displayRect.size.width/displayRect.size.height;

    /*if the difference of the ratio of the display rect dimensions with the ratio of pdf dimensions significant,
     compute new rect that has the same dimension ratio as the pdf and centered in original rect*/
    if(fabsf(rectWidthHeightRatio - pdfWidthHeightRatio) > 0.09)
    {
        //compute adjustment of height and width based on pdf image width-height ratio
        //retain original width first and adjust height
        CGFloat targetRectWidth = displayRect.size.width;
        CGFloat targetRectHeight = targetRectWidth/pdfWidthHeightRatio;
        //if height is bigger than original display rect height, retain height instead and adjust width
        if(targetRectHeight > displayRect.size.height)
        {
            targetRectHeight = displayRect.size.height;
            targetRectWidth = targetRectHeight * pdfWidthHeightRatio;
        }
        //create new rect that is centered in paper
        CGFloat xPoint = displayRect.origin.x + (displayRect.size.width - targetRectWidth)/2;
        CGFloat yPoint = displayRect.origin.y + (displayRect.size.height - targetRectHeight)/2;
        scaleToFitRect  = CGRectMake(xPoint, yPoint, targetRectWidth, targetRectHeight);
    }
    return scaleToFitRect;
}

#pragma mark - private helper methods
/*Determine color space*/
-(CGColorSpaceRef) getColorSpace: (BOOL) isGrayScale
{
    if(isGrayScale == YES)
    {
        return CGColorSpaceCreateDeviceGray();
    }
    return CGColorSpaceCreateDeviceRGB();
}

/*Determine bitmap info to be used*/
-(CGBitmapInfo) getBitmapInfo: (BOOL) isGrayScale
{
    if(isGrayScale == YES)
    {
        return (CGBitmapInfo)kCGImageAlphaNone;
    }
    return (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
}

/*Determine width of space to be occupied by a single pdf page in rect based on number of pages per sheet*/
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

/*Determine height of space to be occupied by a single pdf page in rect based on number of pages per sheet*/
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
