//
//  PDFFileManager.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "XMLParser.h"
#import "PrintSettingsHelper.h"
#import "PrinterManager.h"

#define PREVIEW_FILENAME @"%@/SDAPreview.pdf"

@interface PDFFileManager()

/**
 Indicates whether or not a file ready for preview
 */
@property (nonatomic) BOOL fileAvailableForPreview;

/**
 Print document object
 */
@property (nonatomic, strong) PrintDocument *printDocument;

/**
 Moves the PDF File (from Open In to Documents)
 */
- (BOOL)moveFileToDocuments:(NSURL **)documentURL;

/**
 Verifies if the PDF file is valid
 */
- (kPDFError)verifyDocument:(NSURL *)documentURL;

@end

@implementation PDFFileManager

#pragma mark - Public Methods

+(id) sharedManager
{
    static PDFFileManager * pdfFileManager = nil;
    @synchronized(self)
    {
        if(pdfFileManager == nil)
        {
            pdfFileManager = [[self alloc] init];
        }
    }
    return pdfFileManager;
}

-(id) init
{
    self = [super init];
    if(self)
    {
        _fileAvailableForLoad = NO;
        _fileAvailableForPreview = NO;
    }
    return self;
}

- (kPDFError)setupDocument
{
    NSURL *documentURL;
    if (![self moveFileToDocuments:&documentURL])
    {
        return kPDFErrorProcessingFailed;
    }
    
    kPDFError result = [self verifyDocument:documentURL];
    if (result == kPDFErrorNone)
    {
        self.fileAvailableForPreview = YES;
        self.fileAvailableForLoad = NO;
        NSString *fileName = [self.fileURL lastPathComponent];
        self.printDocument = [[PrintDocument alloc] initWithURL:documentURL name:fileName];
        self.printDocument.previewSetting = [PrintSettingsHelper defaultPreviewSetting];
        self.printDocument.printer = [[PrinterManager sharedPrinterManager] getDefaultPrinter];
    }
    
    return result;
}

#pragma mark - Helper Methods

- (BOOL)moveFileToDocuments:(NSURL **)documentURL
{
    // Create new path in the directory
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *newPath = [NSString stringWithFormat:PREVIEW_FILENAME, documentsDirectory];
    NSError *error;
    
    // Remove existing file, if any
    [[NSFileManager defaultManager] removeItemAtPath:newPath error:&error];
    
    // Copy file to new path
    if ([[NSFileManager defaultManager] moveItemAtPath:[self.fileURL path] toPath:newPath error:&error] == NO)
    {
        (*documentURL) = nil;
        return NO;
    }
    
    (*documentURL) = [NSURL fileURLWithPath:newPath];
    return YES;
}

- (kPDFError)verifyDocument:(NSURL *)documentURL
{
    CGPDFDocumentRef document = CGPDFDocumentCreateWithURL((__bridge CFURLRef)documentURL);
    
    // Check if loaded
    if (document == nil)
    {
        return kPDFErrorOpen;
    }
    
    kPDFError error = kPDFErrorNone;
    
    // Check if PDF has open password
    if (CGPDFDocumentIsUnlocked(document) == false)
    {
        error = kPDFErrorLocked;
    }
    // Check if PDF has encryption
    else if (CGPDFDocumentIsEncrypted(document) == true)
    {
        if (CGPDFDocumentAllowsPrinting(document) == false)
        {
            error = kPDFErrorPrintingNotAllowed;
        }
    }
    
    CGPDFDocumentRelease(document);
    return error;
}

@end
