//
//  PDFPageView.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PreviewSetting.h"

@protocol PDFPageViewDelegate
- (CGPDFPageRef) getPage: (NSUInteger) pageNum;
@end;

@interface PDFPageView : UIView
@property (nonatomic, weak) id <PDFPageViewDelegate> delegate;
@property NSUInteger numPagesPerSheet;
@property NSUInteger colorMode;
@end
