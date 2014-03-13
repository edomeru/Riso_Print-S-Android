//
//  PDFPageViewController.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFPageView.h"

@protocol PDFPageViewContentDelegate
-(CGPDFPageRef) getPDFPage: (NSUInteger) pageIndex withPageOffset:(NSUInteger) pageOffset;
-(PreviewSetting *) getPreviewSetting;
@end

@interface PDFPageContentViewController : UIViewController <PDFPageViewDelegate>
@property (strong, nonatomic) IBOutlet PDFPageView *pageView;
@property(weak, nonatomic) id <PDFPageViewContentDelegate> delegate;
@property NSUInteger pageIndex;
@end
