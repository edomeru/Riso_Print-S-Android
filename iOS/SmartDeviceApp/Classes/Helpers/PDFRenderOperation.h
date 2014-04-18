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

/**
 Indices of the pages to be rendered
 */
@property (nonatomic, readonly) NSArray *pageIndices;
/**
 Delegate to handle render events
 */
@property (nonatomic, weak) id<PDFRenderOperationDelegate> delegate;

/**
 Rendered images
 */
@property (nonatomic, readonly) NSMutableDictionary *images;

/**
 Initializes the render operation
 @param pageIndices
        Indices of the pages to be rendered
 @param size
        Dimensions of the output image
 @param delegate
        Delegate that will be notified of render events
 */
- (id)initWithPageIndexSet:(NSArray *)pageIndices size:(CGSize)size delegate:(id<PDFRenderOperationDelegate>)delegate;

@end

@protocol PDFRenderOperationDelegate <NSObject>

@required

/**
 Notifies the delegate that a page has finished rendering
 */
- (void)renderOperation:(PDFRenderOperation *)pdfRenderOperation didFinishRenderingImageForPage:(NSNumber *)pageIndex;

/**
 Notifies the delegate that the render operation has finished rendering all of the pages
 */
- (void)renderDidDFinish:(PDFRenderOperation *)pdfRenderOperation;

@end
