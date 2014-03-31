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

@interface PrintDocument()

@property (nonatomic) CGPDFDocumentRef pdfDocument;
@property (nonatomic, strong) NSMutableDictionary *pageCache;
@property (nonatomic, strong) NSMutableArray *pageCacheIndex;

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
            NSString *key = [setting objectForKey:@"key"];
            [self.previewSetting addObserver:self forKeyPath:key options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:nil];
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
            NSString *key = [setting objectForKey:@"key"];
            [self.previewSetting removeObserver:self forKeyPath:key context:nil];
        }
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
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

- (UIImage *)imageForPage:(NSUInteger)page withRect:(CGRect)rect
{
    UIImage *cachedImage = [self.pageCache objectForKey:[NSNumber numberWithInt:page]];
    if (cachedImage != nil)
    {
        return cachedImage;
    }
    
    NSDate *start = [NSDate date];
    CGSize size = rect.size;
    CGPDFPageRef pageRef = CGPDFDocumentGetPage(self.pdfDocument, page);
    
    CGColorSpaceRef colorSpaceRef;
    CGBitmapInfo bitmapInfo;
    if ([PrintPreviewHelper isGrayScaleColorForColorModeSetting:self.previewSetting.colorMode])
    {
        colorSpaceRef = CGColorSpaceCreateDeviceGray();
        bitmapInfo = (CGBitmapInfo)kCGImageAlphaNone;
    }
    else
    {
        colorSpaceRef = CGColorSpaceCreateDeviceRGB();
        bitmapInfo = (CGBitmapInfo)kCGImageAlphaNoneSkipLast;
    }
    
    CGContextRef contextRef = CGBitmapContextCreate(nil, size.width, size.height, 8, 0, colorSpaceRef, bitmapInfo);
    CGColorSpaceRelease(colorSpaceRef);
    
    CGContextSetRGBFillColor(contextRef, 1.0f, 1.0f, 1.0f, 1.0f);
    CGContextFillRect(contextRef, rect);
    
    CGContextSaveGState(contextRef);
    
    CGContextTranslateCTM(contextRef, 0.0f, size.height);
    CGContextScaleCTM(contextRef, 1.0f, -1.0f);
    CGContextConcatCTM(contextRef, CGPDFPageGetDrawingTransform(pageRef, kCGPDFMediaBox, rect, 0, true));
    CGContextDrawPDFPage(contextRef, pageRef);
    
    CGContextRestoreGState(contextRef);
    CGImageRef imageRef = CGBitmapContextCreateImage(contextRef);
    UIImage *image = [UIImage imageWithCGImage:imageRef scale:1.0f orientation:UIImageOrientationDownMirrored];
    CGImageRelease(imageRef);
    CGContextRelease(contextRef);
    
    NSTimeInterval elapsed = [start timeIntervalSinceNow];
    NSLog(@"Load time: %f", elapsed);
    
    if ([self.pageCache count] == MAX_PAGE_CACHE)
    {
        NSNumber *index = [self.pageCacheIndex firstObject];
        [self.pageCache removeObjectForKey:index];
        [self.pageCacheIndex removeObjectAtIndex:[index integerValue]];
    }
    
    NSNumber *index = [NSNumber numberWithUnsignedInteger:page];
    [self.pageCacheIndex addObject:index];
    [self.pageCache setObject:image forKey:index];
    
    return image;
}

@end
