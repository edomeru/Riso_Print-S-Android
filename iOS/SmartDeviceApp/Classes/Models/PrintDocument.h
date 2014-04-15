//
//  PrintDocument.h
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PrintDocumentDelegate;
@class PreviewSetting;
@class Printer;

@interface PrintDocument : NSObject

/**
 URL of the PDF File
 */
@property (nonatomic, readonly) NSURL *url;

/**
 Current Preview Setting
 */
@property (nonatomic, strong) PreviewSetting *previewSetting;

/**
 Current Printer Setting
 */
@property (nonatomic, strong) Printer *printer;

/**
 Delegate that will handle Preview Setting changes
 */
@property (nonatomic, weak) id<PrintDocumentDelegate> delegate;

/**
 Page count of the PDF File
 */
@property (nonatomic, readonly) NSInteger pageCount;

/**
 Initialize Print Document with URL of the PDF File
 @param url
        URL of the PDF File
 */
- (id)initWithURL:(NSURL *)url;

@end

@protocol PrintDocumentDelegate <NSObject>

@required
/**
 Notifies the delegate when a preview setting has changed
 */
- (void)previewSettingDidChange;

@end