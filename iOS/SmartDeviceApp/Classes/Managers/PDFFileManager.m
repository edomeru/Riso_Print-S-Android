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

@interface PDFFileManager()

@property (nonatomic) BOOL fileAvailableForPreview;
@property (nonatomic, strong) NSURL *documentURL;
@property (nonatomic, strong) PrintDocument *printDocument;

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
    self = [super init];
    if(self)
    {
        _fileAvailableForLoad = NO;
        _fileAvailableForPreview = NO;
        _printDocument = [[PrintDocument alloc] init];
        _printDocument.previewSetting = [PrintSettingsHelper defaultPreviewSetting];
    }
    return self;
}

- (NSString *)fileName
{
    return [[NSFileManager defaultManager] displayNameAtPath:[self.fileURL path]];
}

- (BOOL)moveFileToDocuments
{
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *newPath = [NSString stringWithFormat:@"%@/SDAPreview.pdf", documentsDirectory];
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:newPath error:&error];
    if ([[NSFileManager defaultManager] moveItemAtPath:[self.fileURL path] toPath:newPath error:&error] == NO)
    {
        self.documentURL = nil;
        return NO;
    }
    
    self.documentURL = [NSURL fileURLWithPath:newPath];
    return YES;
}

- (T_PDF_ERROR)verifyDocument
{
    // Check if loaded
    if (self.printDocument.pdfDocument == nil)
    {
        return PDF_ERROR_OPEN;
    }
    
    if (CGPDFDocumentIsUnlocked(self.printDocument.pdfDocument) == false)
    {
        return PDF_ERROR_LOCKED;
    }
    
    if (CGPDFDocumentIsEncrypted(self.printDocument.pdfDocument) == true)
    {
        if (CGPDFDocumentAllowsPrinting(self.printDocument.pdfDocument) == false)
        {
            return PDF_ERROR_PRINTING_NOT_ALLOWED;
        }
    }
    
    return PDF_ERROR_NONE;
}

- (T_PDF_ERROR)loadFile
{
    if (![self moveFileToDocuments])
    {
        return PDF_ERROR_PROCESSING_FAILED;
    }
   
    self.printDocument = [[PrintDocument alloc] initWithURL:self.documentURL];
    T_PDF_ERROR result = [self verifyDocument];
    if (result == PDF_ERROR_NONE)
    {
        self.fileAvailableForPreview = YES;
        self.printDocument.previewSetting = [PrintSettingsHelper defaultPreviewSetting];
    }
    
    return result;
}

@end
