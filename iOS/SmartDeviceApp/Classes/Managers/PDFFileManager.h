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

@class PrintDocument;

@interface PDFFileManager : NSObject

@property (nonatomic) BOOL fileAvailableForLoad;
@property (nonatomic, readonly) BOOL fileAvailableForPreview;
@property (nonatomic, strong) NSURL *fileURL;
@property (nonatomic, weak, readonly) NSString *fileName;
@property (nonatomic, strong, readonly) PrintDocument *printDocument;

/**
 Returns the single instance of the PDFFileManager
 @return shared PDFFileManager instance
 **/
+ (id)sharedManager;

- (T_PDF_ERROR)loadFile;

@end
