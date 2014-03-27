//
//  PDFFileManager.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFFileManager.h"
@interface PDFFileManager()
@property (strong, nonatomic) NSURL *previewURL;
/**
 Init the internal PDF URL used for preview
 **/
-(void) initPreviewURL;
/**
 Check PDF for unsupported of error case
 @param CGPDFDocumentObjectRef pdfdocument object
 @return T_PDF_ERROR
 **/
-(T_PDF_ERROR) checkPDF:(CGPDFDocumentRef)pdfDocument;
/**
 Rename PDF for preview to the internal PDF URL used for preview
 @return YES if successfully renamed; NO otherwise
 **/
-(BOOL) renamePDFFileToPreviewURL;
/**
 Cleanup the current internal PDF file for preview
 **/
-(void) cleanupPreviewPDF;
@end
@implementation PDFFileManager

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
    if(self == [super init])
    {
        self.pdfDocument = nil;
        self.pdfFileAvailable = NO;
        [self initPreviewURL];
    }
    return self;
}

- (T_PDF_ERROR) setUpPDF:(NSURL *)fileURL
{
    CGPDFDocumentRef pdfDocument = CGPDFDocumentCreateWithURL((__bridge CFURLRef)fileURL);
    int statusCode = [self checkPDF:pdfDocument];
    CGPDFDocumentRelease(pdfDocument);
    if(statusCode == PDF_ERROR_NONE)
    {
        self.pdfURL = fileURL; //keep original url
        /*rename the file - 
         handling for when the same file is opened in the next open-in while in background,
         The previously opened file is still in sandbox when the same file is copied automatically,
         to sandbox by the system, The systems renames the new file to <file name> - 1. pdf because the previous file has the same file name
         To prevent this, always rename the file for preview to a temp file for preview so when the same file is opened for preview*/
        if([self renamePDFFileToPreviewURL] == NO)
        {
            return PDF_ERROR_PROCESSING_FAILED;
        }
        self.pdfDocument = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.previewURL);
        
        if(self.pdfDocument == nil)
        {
            return PDF_ERROR_PROCESSING_FAILED;
        }
        self.pdfFileAvailable = YES;
    }
    else
    {
        self.pdfFileAvailable = NO;
    }

    return statusCode;
}

- (void) cleanUp
{
    if(self.pdfDocument != nil)
    {
        CGPDFDocumentRelease(self.pdfDocument);
    }

    if(self.pdfURL != nil)
    {
        self.pdfURL = nil;
    }
    [self cleanupPreviewPDF];
    self.pdfFileAvailable = NO;
}

#pragma mark - Private Class Methods

-(void) initPreviewURL
{
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *tempString = [NSString stringWithFormat:@"%@/SDAPreview.pdf",documentsDirectory];
    self.previewURL = [NSURL fileURLWithPath:tempString];
}

-(T_PDF_ERROR) checkPDF:(CGPDFDocumentRef)pdfDocument
{
    if(pdfDocument == nil)
    {
        return PDF_ERROR_OPEN; //error opening
    }
    
    if(CGPDFDocumentIsUnlocked(pdfDocument) == false)
    {
        return PDF_ERROR_LOCKED; //unsupported, password protected
    }
    
    if(CGPDFDocumentIsEncrypted(pdfDocument) == true)
    {
        if(CGPDFDocumentAllowsPrinting(pdfDocument) == false)
        {
            return PDF_ERROR_PRINTING_NOT_ALLOWED; // does not allow printing
        }
    }

    return PDF_ERROR_NONE;
}

-(BOOL) renamePDFFileToPreviewURL
{
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    NSError *error = nil;
    if([fileMgr moveItemAtPath:[self.pdfURL path] toPath:[self.previewURL path] error:&error] == NO)
    {
#if DEBUG_LOG_PDF_MANAGER
        NSLog(@"Failed to rename file");
#endif
        return NO;
    }
    return YES;
}

-(void) cleanupPreviewPDF
{
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    if([fileMgr fileExistsAtPath:[self.previewURL path]] == YES)
    {
        NSError *error = nil;
        [fileMgr removeItemAtPath:[self.previewURL path] error:&error];
    }
}

@end
