//
//  PDFFileManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "XMLParser.h"
#import "PrintSettingsHelper.h"
#import "PrinterManager.h"

#define PREVIEW_FILENAME @"%@/SDAPreview.pdf"

@interface PDFFileManager()

/**
 * Flag that indicates whether or not a PDF is available to be displayed in print preview.
 * This flag is set to YES only when the error status is ({@link kPDFErrorNone}).
 * Otherwise, it is NO.
 */
@property (nonatomic) BOOL fileAvailableForPreview;

/**
 * Reference to the PDF file in the form of a PrintDocument object.
 */
@property (nonatomic, strong) PrintDocument *printDocument;

/**
 * Copies the PDF file to the application's Documents directory.
 *
 * @param documentURL output parameter that will contain the path to the
 *                    PDF file in the application's Documents directory\n
 *                    (this will be nil if an error occurs)
 * @result YES if successful, NO otherwise
 */
- (BOOL)moveFileToDocuments:(NSURL **)documentURL;

/**
 * Check if the PDF can be opened, previewed, and printed.
 * The following checks are performed:
 *  - Is the document locked with an open password?
 *  - Is the document encrypted?
 *  - Is the document's printing allowed permission set?
 *
 * @return one of {@link kPDFError} values
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
    
    //revert flags and variables before setting up new document
    self.fileAvailableForPreview = NO;
    self.fileAvailableForLoad = NO;
    self.printDocument = nil;
    
    if (![self moveFileToDocuments:&documentURL])
    {
        return kPDFErrorProcessingFailed;
    }
    
    kPDFError result = [self verifyDocument:documentURL];
    if (result == kPDFErrorNone)
    {
        self.fileAvailableForPreview = YES;
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
