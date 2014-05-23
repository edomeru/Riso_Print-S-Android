//
//  PrintDocumentTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "Printer.h"
#import "DatabaseManager.h"
#import "PrintSetting.h"
#import "PrintSettingsHelper.h"
#import "CGPDFMock.h"

static NSString *context = @"PrintDocumentTestContext";

@interface PrintDocument(Test)
@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSURL *url;
@end

@interface PrintDocumentTest : GHTestCase<PrintDocumentDelegate>

@property (strong, nonatomic) PrintDocument *printDocument;
@property (strong, nonatomic) NSURL *testFileURL;
@property (strong, nonatomic) NSMutableSet *keyChangedSet;
@property (strong, nonatomic) NSArray *expectedKeys;
@property (strong, nonatomic) NSArray *properties;

@end

@implementation PrintDocumentTest
{
}

- (void)setUp
{
    [MagicalRecord setDefaultModelFromClass:[self class]];
    [MagicalRecord setupCoreDataStackWithInMemoryStore];
    
    RESET_FAKE(CGPDFDocumentGetNumberOfPages);
    FFF_RESET_HISTORY();
}

- (void)tearDown
{
    [self.keyChangedSet removeAllObjects];
    [MagicalRecord cleanUp];
}

- (void)setUpClass
{
    self.keyChangedSet = [[NSMutableSet alloc] init];
    self.expectedKeys = @[
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
                          ];
    self.properties = @[
                        @"name",
                        @"url",
                        @"previewSetting",
                        @"printer",
                        @"delegate",
                        @"pageCount",
                        @"currentPage"
                        ];
}

- (void)tearDownClass
{
}

- (BOOL)previewSettingDidChange:(NSString *)keyChanged
{
    [self.keyChangedSet addObject:keyChanged];
    return YES;
}

- (void)testProperties
{
    // SUT + Verification
    PrintDocument *printDocument = [[PrintDocument alloc] init];
    for (NSString *property in self.properties)
    {
        BOOL responds = [printDocument respondsToSelector:NSSelectorFromString(property)];
        GHAssertEquals(responds, YES, @"Print document must respond to: %@", property);
    }
    GHAssertNil(printDocument.name, @"name must initially be nil.");
    GHAssertNil(printDocument.url, @"url must initially be nil.");
    GHAssertNil(printDocument.previewSetting, @"previewSetting must initially be nil.");
    GHAssertNil(printDocument.printer, @"printer must initially be nil.");
    GHAssertNil(printDocument.delegate, @"delegate must initially be nil.");
    GHAssertEquals(printDocument.pageCount, (NSInteger)0, @"pageCount must be 0.");
    GHAssertEquals(printDocument.currentPage, (NSInteger)0, @"currentPage must be 0.");
}

- (void)testInitWithUrl
{
    // SUT
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    
    // Verification
    GHAssertEqualObjects(printDocument.url, url, @"URL should match");
    GHAssertEqualStrings(printDocument.name, name, @"Name should match");
}

- (void)testPreviewSetting
{
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    printDocument.delegate = self;
    [printDocument setPreviewSetting:previewSetting];
    for (NSString *key in self.expectedKeys)
    {
        NSInteger value = [[self.printDocument.previewSetting valueForKey:key] integerValue];
        [previewSetting setValue:[NSNumber numberWithInteger:++value] forKey:key];
    }
    
    // Verification
    GHAssertEqualObjects(printDocument.previewSetting, previewSetting, @"Preview setting should match.");
    for (NSString *key in self.expectedKeys)
    {
        BOOL changed = [self.keyChangedSet containsObject:key];
        GHAssertTrue(changed, @"%@ should be observed", key);
    }
}

- (void)testPrinter
{
    // Mock
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[mockPrintSettingsHelper expect] copyPrintSettings:OCMOCK_ANY toPreviewSetting:[OCMArg anyObjectRef]];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    Printer *printer = [Printer MR_createEntity];
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    [printDocument setPreviewSetting:previewSetting];
    [printDocument setPrinter:printer];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
}

- (void)testPrinter_Nil
{
    // Mock
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[mockPrintSettingsHelper expect] copyPrintSettings:OCMOCK_ANY toPreviewSetting:[OCMArg anyObjectRef]];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    [printDocument setPreviewSetting:previewSetting];
    [printDocument setPrinter:nil];
    
    // Verification
    GHAssertThrows([mockPrintSettingsHelper verify], @"");
}

- (void)testPrinter_PreviewSetting_Nil
{
    // Mock
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[mockPrintSettingsHelper expect] copyPrintSettings:OCMOCK_ANY toPreviewSetting:[OCMArg anyObjectRef]];
    
    // SUT
    Printer *printer = [Printer MR_createEntity];
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    [printDocument setPreviewSetting:nil];
    [printDocument setPrinter:printer];
    
    // Verification
    GHAssertThrows([mockPrintSettingsHelper verify], @"");
}

- (void)testPageCount
{
    // Mock
    CGPDFDocumentGetNumberOfPages_fake.return_val = 10;
    
    // SUT
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    NSInteger pageCount = [printDocument pageCount];
    
    // Verification
    GHAssertEquals((int)CGPDFDocumentGetNumberOfPages_fake.call_count, 1, @"CGPDFDocumentGetNumberOfPages must be called 1 time.");
    GHAssertEquals(pageCount, (NSInteger)CGPDFDocumentGetNumberOfPages_fake.return_val, @"Page count should match");
}

- (void)testKVO_NoChange
{
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSURL *url = [NSURL URLWithString:@"http://192.168.1.1"];
    NSString *name = @"Document";
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:url name:name];
    [printDocument setPreviewSetting:previewSetting];
    previewSetting.copies = previewSetting.copies;
    
    // Verification
    BOOL changed = [self.keyChangedSet containsObject:@"copies"];
    GHAssertFalse(changed, @"");
}

/*- (void)test001_initPrintDocument
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

-(BOOL) previewSettingDidChange:(NSString *)keyChanged
{
    [self.keyChangedSet addObject:keyChanged];
    return YES;
}*/
 
@end
