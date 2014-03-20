//
//  PDFPageView.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PreviewSetting.h"

@protocol PDFPageViewDatasource
/**
 Get a PDF page to be drawn in the PDFPageView
 @param pageNum - Nth page to be drawn in the PDFPageView
 @return PDF page to be drawn
 **/
- (CGPDFPageRef) getPage: (NSUInteger) pageNum;
/**
 Get the total number of pages to be drawn in the PDFPageView
 @return Number of pages to be drawn in the PDFPageView
 **/
- (NSUInteger) getNumPages;
/**
Checks if drawing in PDFPageView is in GrayScale or Monochrome
 @return YES if drawing is in Grayscale. NO otherwise
 **/
- (BOOL) isGrayScale;
@end;

@interface PDFPageView : UIView
@property (nonatomic, weak) id <PDFPageViewDatasource> datasource;
@end
