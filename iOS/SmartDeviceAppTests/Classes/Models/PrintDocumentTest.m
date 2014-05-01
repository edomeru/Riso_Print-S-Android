//
//  PrintDocumentTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import <QuartzCore/QuartzCore.h>
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "Printer.h"
#import "DatabaseManager.h"
#import "PrintSetting.h"

@interface PrintDocument(Test)
@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSURL *url;
@end

@interface PrintDocumentTest : GHTestCase <PrintDocumentDelegate>

@property (strong, nonatomic) PrintDocument *printDocument;
@property (strong, nonatomic) NSURL *testFileURL;
@property (strong, nonatomic) NSMutableSet *keyChangedSet;

@end

@implementation PrintDocumentTest
{
    NSArray *expectedKeys;
}

-(void)tearDown
{
    [self.keyChangedSet removeAllObjects];
}

- (void)setUpClass
{
    self.testFileURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    self.printDocument = [[PrintDocument alloc] initWithURL:self.testFileURL name:[self.testFileURL.path lastPathComponent]];
    self.printDocument.delegate = self;
    self.keyChangedSet = [[NSMutableSet alloc] init];
    
    expectedKeys = [NSArray arrayWithObjects:
                    @"colorMode",
                    @"orientation",
                    @"copies",
                    @"duplex",
                    @"paperSize",
                    @"scaleToFit",
                    @"paperType",
                    @"inputTray",
                    @"imposition",
                    @"impositionOrder",
                    @"sort",
                    @"booklet",
                    @"bookletFinish",
                    @"bookletLayout",
                    @"finishingSide",
                    @"staple",
                    @"punch",
                    @"outputTray"
                    , nil];
}

- (void)test001_initPrintDocument
{
    GHAssertNotNil(self.printDocument, @"");
    GHAssertNotNil(self.printDocument.url, @"");
    GHAssertNotNil(self.printDocument.name, @"");
    GHAssertEquals(self.testFileURL, self.printDocument.url, @"");
    GHAssertEqualCStrings([[self.testFileURL.path lastPathComponent] cStringUsingEncoding:NSUTF8StringEncoding],[self.printDocument.name cStringUsingEncoding:NSUTF8StringEncoding], @"");
}

- (void)test002_setPreviewSetting
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    [self.printDocument setPreviewSetting:previewSetting];
    GHAssertEqualObjects(self.printDocument.previewSetting, previewSetting, @"");
    
    //change values in preview setting
    for(NSString *key in expectedKeys)
    {
        NSInteger value = [[self.printDocument.previewSetting valueForKey:key] integerValue];
        [previewSetting setValue:[NSNumber numberWithInteger:++value] forKey:key];
    }
    
    //check if keys are observed
    GHAssertEquals(self.keyChangedSet.count, expectedKeys.count, @"");
    for(NSString *key in expectedKeys)
    {
        GHAssertTrue([self.keyChangedSet containsObject:key], [NSString stringWithFormat:@"test observed key %@", key]);
    }
    
    [self.keyChangedSet removeAllObjects];
    
    //change preview setting object
    PreviewSetting *newPreviewSetting = [[PreviewSetting alloc] init];
    [self.printDocument setPreviewSetting:newPreviewSetting];
    GHAssertNotEqualObjects(self.printDocument.previewSetting, previewSetting, @"");
    GHAssertEqualObjects(self.printDocument.previewSetting, newPreviewSetting, @"");
    
    //check that old preview setting is not observed
    for(NSString *key in expectedKeys)
    {
        NSInteger value = [[self.printDocument.previewSetting valueForKey:key] integerValue];
        [previewSetting setValue:[NSNumber numberWithInteger:++value] forKey:key];
    }
    
    GHAssertEquals((int)self.keyChangedSet.count, 0, @"");
    
    [self.keyChangedSet removeAllObjects];
    //check that new preview setting is observed
    for(NSString *key in expectedKeys)
    {
        NSInteger value = [[self.printDocument.previewSetting valueForKey:key] integerValue];
        [newPreviewSetting setValue:[NSNumber numberWithInteger:++value] forKey:key];
    }
    
    //check if keys are observed
    GHAssertEquals(self.keyChangedSet.count, expectedKeys.count, @"");
    for(NSString *key in expectedKeys)
    {
        GHAssertTrue([self.keyChangedSet containsObject:key], [NSString stringWithFormat:@"test observed key %@", key]);
    }
}

- (void)test003_pageCount
{
    CGPDFDocumentRef pdfDocumentRef = CGPDFDocumentCreateWithURL((__bridge CFURLRef)self.testFileURL);
    GHAssertEquals((int)self.printDocument.pageCount, (int)CGPDFDocumentGetNumberOfPages(pdfDocumentRef), @"");
    CGPDFDocumentRelease(pdfDocumentRef);
}

- (void) test004_setPrinter
{
    Printer *printer = (Printer*)[DatabaseManager addObject:E_PRINTER];
    PrintSetting *printSetting = (PrintSetting*)[DatabaseManager addObject: E_PRINTSETTING];
    
    for(NSString *key in expectedKeys)
    {
        [self.printDocument.previewSetting setValue:[NSNumber numberWithInt:0] forKey:key];
        [printSetting setValue:[NSNumber numberWithInt:arc4random()  % 2 + 1] forKey:key];
    }
    printer.printsetting = printSetting;
    [self.printDocument setPrinter:printer];
    
    GHAssertNotNil(self.printDocument.printer, @"");
    
    //check print setting values copied to preview
    for(NSString *key in expectedKeys)
    {
        GHAssertEquals([[self.printDocument.previewSetting valueForKey:key]integerValue], [[printSetting valueForKey:key]integerValue], @"");
    }
    
    [DatabaseManager discardChanges]; //remove the added printer and printSetting
}

- (void)test005_keyValueObservingValueNotChanged
{
    for(NSString *key in expectedKeys)
    {
        NSInteger value = [[self.printDocument.previewSetting valueForKey:key] integerValue];
        [self.printDocument.previewSetting setValue:[NSNumber numberWithInteger:value] forKey:key];
    }
    
    GHAssertEquals((int)self.keyChangedSet.count, 0, @"");
}

-(void) previewSettingDidChange:(NSString *)keyChanged
{
    [self.keyChangedSet addObject:keyChanged];
}
@end
