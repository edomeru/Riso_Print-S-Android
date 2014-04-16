//
//  PDFFileManager.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    PDF_ERROR_NONE,
    PDF_ERROR_ENCRYPTED,
    PDF_ERROR_OPEN,
    PDF_ERROR_LOCKED,
    PDF_ERROR_PRINTING_NOT_ALLOWED,
    PDF_ERROR_PROCESSING_FAILED
} T_PDF_ERROR;

@interface PDFFileManager : NSObject
@property (strong, nonatomic) NSURL *pdfURL;
@property (nonatomic, assign) CGPDFDocumentRef pdfDocument;
@property (nonatomic, assign) BOOL pdfFileAvailable;
/**
 Returns the single instance of the PDFFileManager
 @return shared PDFFileManager instance
 **/
+ (id)sharedManager;
/**
 Set up the PDF for preview
 @return T_PDF_ERROR
 **/
- (T_PDF_ERROR) setUpPDF:(NSURL *)fileURL;
/**
 Clean up PDF for preview
 @return PDF_ERROR_NONE if no error. Error ID if there is Error
 **/
- (void) cleanUp;


@end
