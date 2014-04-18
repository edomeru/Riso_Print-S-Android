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
 Key-Value Observing Context
 */
static NSString *previewSettingContext = @"PreviewSettingContext";

@interface PrintDocument()

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSURL *url;

/**
 Add self as observer of Preview Settings object
 */
- (void)addObservers;

/**
 Remove self as observer of Preview Settings object
 */
- (void)removeObservers;

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
    if(self.previewSetting != nil)
    {
        PreviewSetting *previewSetting = self.previewSetting;
        [PrintSettingsHelper copyPrintSettings:printer.printsetting toPreviewSetting:&previewSetting];
    }
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
            [self.previewSetting removeObserver:self forKeyPath:key context:&previewSettingContext];
        }
    }
}

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
    [self.delegate previewSettingDidChange];
}


@end
