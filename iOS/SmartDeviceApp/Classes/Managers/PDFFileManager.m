//
//  PDFFileManager.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFFileManager.h"

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
        _pdfDocument = nil;
        _pdfFileAvailable = NO;
    }
    return self;
}

- (int) setUpPDF:(NSURL *)fileURL
{
    CGPDFDocumentRef pdfDocument = CGPDFDocumentCreateWithURL((__bridge CFURLRef)fileURL);
    int statusCode = [self checkPDF:pdfDocument];
    
    if(statusCode == 0)
    {
        _pdfURL = fileURL;
        _pdfDocument = CGPDFDocumentRetain(pdfDocument);
        _pdfFileAvailable = YES;
    }
    else
    {
        _pdfFileAvailable = NO;
    }
    CGPDFDocumentRelease(pdfDocument);
    
    return statusCode;
}

- (void) cleanUp
{
    if(_pdfDocument != nil)
    {
        CGPDFDocumentRelease(_pdfDocument);
    }
    NSLog(@"URL:%@", [_pdfURL path]);
    if(_pdfURL != nil)
    {
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSError *error = [[NSError alloc] init];
        [fileManager removeItemAtURL:_pdfURL error:&error];
        _pdfURL = nil;
    }
    _pdfFileAvailable = NO;
}


-(int) checkPDF:(CGPDFDocumentRef)pdfDocument
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

@end
