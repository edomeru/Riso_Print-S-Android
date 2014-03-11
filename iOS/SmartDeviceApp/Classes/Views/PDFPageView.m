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
    }
    // Drawing code
    CGPDFPageRef pdfPage = [self.delegate getPage:0];
    
    if(pdfPage == nil){
        NSLog(@"pdf is nil");
    }
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    
	// PDF might be transparent, assume white paper
	[[UIColor whiteColor] set];
	CGContextFillRect(ctx, rect);
    
	// Flip coordinates
	CGContextGetCTM(ctx);
	CGContextScaleCTM(ctx, 1, -1);
	CGContextTranslateCTM(ctx, 0, -rect.size.height);
    
    
	// get the rectangle of the cropped inside
	CGRect mediaRect = CGPDFPageGetBoxRect(pdfPage, kCGPDFCropBox);
	CGContextScaleCTM(ctx, rect.size.width / mediaRect.size.width,
                      rect.size.height / mediaRect.size.height);
	CGContextTranslateCTM(ctx, -mediaRect.origin.x, -mediaRect.origin.y);
    
	// draw it
	CGContextDrawPDFPage(ctx, pdfPage);
    
}


@end
