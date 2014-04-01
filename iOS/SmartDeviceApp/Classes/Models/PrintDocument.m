//
//  PrintDocument.m
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "PrintPreviewHelper.h"

#define MAX_PAGE_CACHE 5

static NSString *previewSettingContext = @"PreviewSettingContext";

@interface PrintDocument()

@property (nonatomic) CGPDFDocumentRef pdfDocument;
@property (strong) NSMutableDictionary *pageCache;
@property (strong) NSMutableArray *pageCacheIndex;

- (void)addObservers;
- (void)removeObservers;

@end

@implementation PrintDocument

- (id)initWithURL:(NSURL *)url
{
    self = [super init];
    if (self)
    {
        _pdfDocument = CGPDFDocumentCreateWithURL((__bridge CFURLRef)url);
        _pageCache = [[NSMutableDictionary alloc] init];
        _pageCacheIndex = [[NSMutableArray alloc] init];
        
    }
    return self;
}

- (void)dealloc
{
    [self removeObservers];
    CGPDFDocumentRelease(_pdfDocument);
}

- (void)addObservers
{
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

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context != &previewSettingContext)
    {
        return;
    }
    
    int changeKind = [[change objectForKey:NSKeyValueChangeKindKey] intValue];
    if (changeKind != NSKeyValueChangeSetting)
    {
        return;
    }
    
    // Compare if value is changed
    NSNumber *old = [change objectForKey:NSKeyValueChangeOldKey];
    NSNumber *new = [change objectForKey:NSKeyValueChangeNewKey];
    if ([old isEqualToNumber:new])
    {
        return;
    }
    
    NSLog(@"Settings changed");
    [self.pageCache removeAllObjects];
    [self.delegate previewSettingDidChange];
}

- (void)setPreviewSetting:(PreviewSetting *)previewSetting
{
    [self removeObservers];
    _previewSetting = previewSetting;
    [self addObservers];
}

- (NSInteger)pageCount
{
    if (self.pdfDocument == nil)
    {
        return 0;
    }
    
    return CGPDFDocumentGetNumberOfPages(self.pdfDocument);
}

@end
