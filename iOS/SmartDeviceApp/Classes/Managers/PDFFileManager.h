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
    PDF_ERROR_ENCRYPTED = PDF_ERROR_NONE,
    PDF_ERROR_OPEN,
    PDF_ERROR_LOCKED,
    PDF_ERROR_PRINTING_NOT_ALLOWED
} T_PDF_ERROR;

@interface PDFFileManager : NSObject
@property (strong, nonatomic) NSURL *pdfURL;
@property  CGPDFDocumentRef pdfDocument;

+ (id)sharedManager;

- (int) setUpPDF:(NSURL *)fileURL;
- (void) cleanUp;


@end
