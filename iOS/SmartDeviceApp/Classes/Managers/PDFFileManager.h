//
//  PDFFileManager.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 PDF Error types
 */
typedef enum
{
    /**
     No error
     */
    kPDFErrorNone,
    
    /**
     PDF File cannot be opened
     */
    kPDFErrorOpen,
    
    /**
     PDF File is locked/has open password
     */
    kPDFErrorLocked,
    
    /**
     PDF File does not allow printing
     */
    kPDFErrorPrintingNotAllowed,
    
    /**
     PDF File was not copied succesfully
     */
    kPDFErrorProcessingFailed
} kPDFError;

@class PrintDocument;

@interface PDFFileManager : NSObject

/**
 Indicates whether or not a file is available for loading (Open In...)
 */
@property (nonatomic) BOOL fileAvailableForLoad;

/**
 Indicates whether or not a file ready for preview
 */
@property (nonatomic, readonly) BOOL fileAvailableForPreview;

/**
 URL of the PDF File
 */
@property (nonatomic, strong) NSURL *fileURL;

/**
 Print Document object
 */
@property (nonatomic, strong, readonly) PrintDocument *printDocument;

/**
 Returns the single instance of the PDFFileManager
 @return shared PDFFileManager instance
 **/
+ (id)sharedManager;

/**
 Prepares the document for preview
 */
- (kPDFError)setupDocument;

@end
