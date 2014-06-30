//
//  PDFFileManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/** @file PDFFileManager.h */
/**
 * Types of errors when opening a PDF.
 */
typedef enum
{
    kPDFErrorNone,                  /**< No error */
    kPDFErrorOpen,                  /**< PDF cannot be opened */
    kPDFErrorLocked,                /**< PDF is locked or has an open password */
    kPDFErrorPrintingNotAllowed,    /**< PDF does not allow printing */
    kPDFErrorProcessingFailed       /**< PDF was not copied successfully */
} kPDFError;

@class PrintDocument;

/**
 * Handler for the PDF file.
 * If there are no errors ({@link kPDFErrorNone}) in setting-up the PDF ({@link setupDocument}),
 * this class will keep a reference to the PDF file stored in the {@link printDocument} 
 * property.\n
 * The PDF file is also copied to the Documents directory of the application.\n\n
 * This class is designed to be used as a singleton to keep a single reference
 * to the PDF file throughout the application and for its entire lifecycle.\n
 * It uses the native CGPDFDocument class of iOS.
 * 
 * @see https://developer.apple.com/library/mac/documentation/graphicsimaging/reference/CGPDFDocument/Reference/reference.html
 */
@interface PDFFileManager : NSObject

/**
 * Flag that indicates whether or not a PDF is available to be loaded into the application.
 * This flag is set to YES only when the application is launched via open-in. 
 * Otherwise, it is NO.
 */
@property (nonatomic) BOOL fileAvailableForLoad;

/**
 * Flag that indicates whether or not a PDF is available to be displayed in print preview.
 * This flag is set to YES only when the error status is ({@link kPDFErrorNone}).
 * Otherwise, it is NO.
 */
@property (nonatomic, readonly) BOOL fileAvailableForPreview;

/**
 * URL of the PDF file.
 */
@property (nonatomic, strong) NSURL *fileURL;

/**
 * Reference to the PDF file in the form of a PrintDocument object.
 */
@property (nonatomic, strong, readonly) PrintDocument *printDocument;

/**
 * Returns the singleton PDFFileManager object.
 * If the manager does not exist yet, then this method creates it.\n
 *
 * @return the single instance of PDFFileManager
 */
+ (id)sharedManager;

/**
 * Prepares the PDF file for preview.
 * The following steps are performed:
 *  - copy the PDF file to the Documents directory of the application
 *  - check if the PDF can be opened, previewed, and printed
 *  - if ({@link kPDFErrorNone}), then store a reference to the PDF in {@link printDocument} 
 * 
 * In addition, when the {@link printDocument} is set, the corresponding Printer
 * and PreviewSetting objects are also set and attached to the PrintDocument object.
 *
 * @return one of {@link kPDFError} values
 */
- (kPDFError)setupDocument;

@end
