//
//  PrintDocument.h
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PrintDocumentDelegate

@required
- (void)previewSettingDidChange;

@end

@class PreviewSetting;
@interface PrintDocument : NSObject

@property (nonatomic, readonly) CGPDFDocumentRef pdfDocument;
@property (nonatomic, readonly) NSInteger pageCount;
@property (nonatomic, strong) PreviewSetting *previewSetting;
@property (nonatomic, weak) id<PrintDocumentDelegate> delegate;


// TODO:
// Add selected printer

- (id)initWithURL:(NSURL *)url;

@end
