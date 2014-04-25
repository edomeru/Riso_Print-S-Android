//
//  PrintDocument.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PrintDocumentDelegate;
@class PreviewSetting;
@class Printer;

@interface PrintDocument : NSObject

/**
 File name of the PDF file
 */
@property (nonatomic, readonly) NSString *name;

/**
 URL of the PDF File
 */
@property (nonatomic, readonly) NSURL *url;

/**
 Current Preview Setting
 */
@property (nonatomic, strong) PreviewSetting *previewSetting;

/**
 Current Printer
 */
@property (nonatomic, weak) Printer *printer;

/**
 Delegate that will handle Preview Setting changes
 */
@property (nonatomic, weak) id<PrintDocumentDelegate> delegate;

/**
 Page count of the PDF File
 */
@property (nonatomic, readonly) NSInteger pageCount;


/**
 Current page
 */
@property (nonatomic) NSInteger currentPage;

/**
 Initialize Print Document with URL of the PDF File
 @param url
        URL of the PDF File
 */
- (id)initWithURL:(NSURL *)url name:(NSString *)name;

@end

@protocol PrintDocumentDelegate <NSObject>

@required
/**
 Notifies the delegate when a preview setting has changed
 */
- (void)previewSettingDidChange;

@end