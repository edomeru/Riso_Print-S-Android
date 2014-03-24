//
//  PDFPageViewController.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFPageView.h"

@protocol PDFPageContentViewControllerDatasource
/**
 Get the actual PDF Page based on the page index and the offset
 @param pageIndex - index of the page in the page view controller
 @param pageOffset - the Nth PDF page to show in the view
 
 @return PDF page
 **/
-(CGPDFPageRef) getPDFPage: (NSUInteger) pageIndex withPageOffset:(NSUInteger) pageOffset;
/**
 Get the PreviewSetting object
 @return PreviewSetting object
 **/
-(PreviewSetting *) getPreviewSetting;
@end

@interface PDFPageContentViewController : UIViewController <PDFPageViewDatasource>
@property(weak, nonatomic) id <PDFPageContentViewControllerDatasource> datasource;
@property (nonatomic, assign) NSUInteger pageIndex;
@end
