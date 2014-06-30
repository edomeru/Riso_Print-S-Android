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

/**
 * Model for the PDF document object.
 * This is used by PDFFileManager to represent the actual PDF file
 * and by the "Print Preview" screen classes to apply the preview 
 * settings.\n
 * It uses the native CGPDFDocument class of iOS.
 *
 * @see https://developer.apple.com/library/mac/documentation/graphicsimaging/reference/CGPDFDocument/Reference/reference.html
 */
@interface PrintDocument : NSObject

/**
 * File name of the PDF file.
 */
@property (nonatomic, readonly) NSString *name;

/**
 * URL of the PDF file.
 */
@property (nonatomic, readonly) NSURL *url;

/**
 * Current set of preview settings applied to the document.
 */
@property (nonatomic, strong) PreviewSetting *previewSetting;

/**
 * Currently selected printer (for "Print Preview"-"Print Settings").
 */
@property (nonatomic, weak) Printer *printer;

/**
 * Reference to the delegate that will handle the preview setting changes.
 */
@property (nonatomic, weak) id<PrintDocumentDelegate> delegate;

/**
 * Number of pages of the PDF file.
 */
@property (nonatomic, readonly) NSInteger pageCount;

/**
 * Stores the most recent page accessed.
 */
@property (nonatomic) NSInteger currentPage;

/**
 * Returns an instance of a PrintDocument object.
 *
 * @param url URL of the PDF file
 * @param name filename of the PDF file
 * @return an instance of a PrintDocument object
 */
- (id)initWithURL:(NSURL *)url name:(NSString *)name;

@end

/**
 * Allows the PrintDocument object to coordinate changes made
 * to the document to its user.
 * Classes that use PrintDocument should conform to this protocol.
 */
@protocol PrintDocumentDelegate <NSObject>

@required

/**
 * Notifies the delegate when a preview setting has changed.
 * 
 * @param keyChanged the string name of the preview setting modified
 * @return YES if the preview setting can be displayed, NO otherwise
 */
- (BOOL)previewSettingDidChange:(NSString *)keyChanged;

@end
