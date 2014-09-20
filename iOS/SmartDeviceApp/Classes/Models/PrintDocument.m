//
//  PrintDocument.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "PrintPreviewHelper.h"
#import "Printer.h"

#define MAX_PAGE_CACHE 5

/**
 * Key-Value Observing Context
 */
static NSString *previewSettingContext = @"PreviewSettingContext";

@interface PrintDocument()

/**
 * Adds key-value observers to each preview setting.
 * This is used for the KVO style of monitoring if a preview setting
 * was changed in the "Print Settings" screen.
 * 
 * @see https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/KeyValueObserving/KeyValueObserving.html
 */
- (void)addObservers;

/**
 * Removes the key-value observers from each preview setting.
 * This is used for the KVO style of monitoring if a preview setting
 * was changed in the "Print Settings" screen.
 *
 * @see https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/KeyValueObserving/KeyValueObserving.html
 */
- (void)removeObservers;

#if GET_ORIENTATION_FROM_PDF_ENABLED
/**
 * Sets the preview setting for Orientation to use the orientation of the first page of the PDF.
 */
- (void)getOrientationFromPDF;
#endif

@end

@implementation PrintDocument

#pragma mark - Public Methods

- (id)initWithURL:(NSURL *)url name:(NSString *)name
{
    self = [super init];
    if (self)
    {
        _url = url;
        _name = name;
        _currentPage = 0;
        _disableContraints = NO;
    }
    return self;
}

- (void)dealloc
{
    [self removeObservers];
}

#pragma mark - Getter/Setter Methods

- (void)setPreviewSetting:(PreviewSetting *)previewSetting
{
    [self removeObservers];
    _previewSetting = previewSetting;
    [self addObservers];
}

- (void)setPrinter:(Printer *)printer
{
    _printer = printer;
    if(self.previewSetting != nil && printer != nil)
    {
        PreviewSetting *previewSetting = self.previewSetting;
        [PrintSettingsHelper copyPrintSettings:printer.printsetting toPreviewSetting:&previewSetting];
    }
    
#if GET_ORIENTATION_FROM_PDF_ENABLED
    if(self.previewSetting != nil)
    {
        [self getOrientationFromPDF];
    }
#endif
}

- (NSInteger)pageCount
{
    CGPDFDocumentRef pdfDocument = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.url);
    int pageCount =  CGPDFDocumentGetNumberOfPages(pdfDocument);
    CGPDFDocumentRelease(pdfDocument);
    return pageCount;
}

#pragma mark - Helper Methods

- (void)addObservers
{
    // Add self as observer of all the relevant properties in Preview Settings
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
#if GET_ORIENTATION_FROM_PDF_ENABLED
            if ([key isEqualToString:KEY_ORIENTATION])
            {
                // do not observe this setting
                // fix for the flicker in orientation when changing selected printer
                continue;
            }
#endif
            
            [self.previewSetting addObserver:self forKeyPath:key options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:&previewSettingContext];
        }
    }
}

- (void)removeObservers
{
    // Remove self as observer of all the relevant properties in Preview Settings
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
#if GET_ORIENTATION_FROM_PDF_ENABLED
            if ([key isEqualToString:KEY_ORIENTATION])
            {
                continue;
            }
#endif
            
            [self.previewSetting removeObserver:self forKeyPath:key context:&previewSettingContext];
        }
    }
}

#if GET_ORIENTATION_FROM_PDF_ENABLED
- (void)getOrientationFromPDF
{
    CGPDFDocumentRef docRef = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.url);
    CGPDFPageRef pageRef = CGPDFDocumentGetPage(docRef, 1);
    CGRect pageRect = CGPDFPageGetBoxRect(pageRef, kCGPDFMediaBox);
    int rotation = CGPDFPageGetRotationAngle(pageRef);
    if ((rotation % 180) > 0)
    {
        CGFloat temp = pageRect.size.width;
        pageRect.size.width = pageRect.size.height;
        pageRect.size.height = temp;
    }
    
    kOrientation orientation = kOrientationPortrait;
    if (pageRect.size.width > pageRect.size.height)
    {
        orientation = kOrientationLandscape;
    }
    self.previewSetting.orientation = orientation;
    
    CGPDFDocumentRelease(docRef);
}
#endif

#pragma mark - Key-Value Observing Methods

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    // Ignore changes outside of the current context
    if (context != &previewSettingContext)
    {
        return;
    }
    
    // Ignore changes besides NSKeyValueChangeSetting
    int changeKind = [[change objectForKey:NSKeyValueChangeKindKey] intValue];
    if (changeKind != NSKeyValueChangeSetting)
    {
        return;
    }
    
    // Ignore changes if actual value has not changed
    NSNumber *old = [change objectForKey:NSKeyValueChangeOldKey];
    NSNumber *new = [change objectForKey:NSKeyValueChangeNewKey];
    if ([old isEqualToNumber:new])
    {
        return;
    }
    
    // Notify delegate of the change
    [self.delegate previewSettingDidChange:keyPath];
}


@end
