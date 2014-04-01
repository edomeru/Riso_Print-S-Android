//
//  PDFRenderOperation.h
//  SmartDeviceApp
//
//  Created by Seph on 4/1/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PDFRenderOperationDelegate;

@interface PDFRenderOperation : NSOperation

@property (nonatomic) NSInteger pageIndex;
@property (nonatomic, weak) id<PDFRenderOperationDelegate> delegate;
@property (nonatomic, strong, readonly) UIImage *image;

- (id)initWithPageIndex:(NSInteger)pageIndex size:(CGSize)size delegate:(id<PDFRenderOperationDelegate>)delegate;

@end


@protocol PDFRenderOperationDelegate <NSObject>

@required
- (void)renderDidDFinish:(PDFRenderOperation *)pdfRenderOperation;

@end
