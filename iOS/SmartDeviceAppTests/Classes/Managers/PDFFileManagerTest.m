//
//  PDFFileManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"

#import "CGPDFMock.h"

@interface PDFFileManagerTest : GHTestCase
@property (nonatomic, strong) PDFFileManager *manager;
@end

@implementation PDFFileManagerTest
{
    NSURL *applicationPDFURL;
    NSURL *testPDFNoPassURL;
    NSURL *testPDFWithPassURL;
    NSURL *testPDFEncryptedPrintAllowedURL;
    NSURL *testPDFEncryptedPrintNotAllowedURL;
    NSURL *pdfURL;
}
- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    self.manager = [PDFFileManager sharedManager];
    
    pdfURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    testPDFNoPassURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    testPDFWithPassURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(test123-None)" withExtension:@"pdf"];
    testPDFEncryptedPrintAllowedURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(None-123)-PrintingAllowed" withExtension:@"pdf"];
    testPDFEncryptedPrintNotAllowedURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(None-123)-PrintingNotAllowed" withExtension:@"pdf"];
    
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    
    applicationPDFURL = [NSURL URLWithString:[[documentsDir stringByAppendingString:@"/SDAPreview.pdf"] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    RESET_FAKE(CGPDFDocumentCreateWithURL);
    
    FFF_RESET_HISTORY();
    
    CGPDFDocumentCreateWithURL_fake.return_val = (CGPDFDocumentRef)1;
    CGPDFDocumentIsUnlocked_fake.return_val = true;
    CGPDFDocumentIsEncrypted_fake.return_val = false;
    CGPDFDocumentAllowsPrinting_fake.return_val = true;
}

// Run at end of all tests in the class
- (void)tearDownClass
{
}

// Run before each test method
- (void)setUp
{
}

// Run after each test method
- (void)tearDown
{
    self.manager.fileURL = nil;
    self.manager.fileAvailableForLoad = NO;
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:applicationPDFURL.path error:&error];
}

- (void)testSharedManager
{
    // SUT
    PDFFileManager *manager = [PDFFileManager sharedManager];
    
    // Verification
    GHAssertNotNil(manager, @"");
    GHAssertEqualObjects(manager, [PDFFileManager sharedManager], @"");
}

- (void)testSetupDocument_moveFileToDocuments_NG
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(NO)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorProcessingFailed, @"Result must be kPDFErrorProcessingFailed.");
    GHAssertEquals(self.manager.fileAvailableForPreview, NO, @"File should not be available for preview.");
    [mockFileManager stopMocking];
}

- (void)testSetupDocument_PDFCannotBeLoaded
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(YES)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    CGPDFDocumentCreateWithURL_fake.return_val = 0;
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorOpen, @"Result must be kPDFErrorOpen.");
    [mockFileManager stopMocking];
}

- (void)testSetupDocument_PDFIsLocked
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(YES)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    CGPDFDocumentCreateWithURL_fake.return_val = (CGPDFDocumentRef)1;
    CGPDFDocumentIsUnlocked_fake.return_val = false;
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorLocked, @"Result must be kPDFErrorIsLocked.");
    [mockFileManager stopMocking];
}

- (void)testSetupDocument_PDFDoesNotAllowPrinting
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(YES)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    CGPDFDocumentCreateWithURL_fake.return_val = (CGPDFDocumentRef)1;
    CGPDFDocumentIsUnlocked_fake.return_val = true;
    CGPDFDocumentIsEncrypted_fake.return_val = true;
    CGPDFDocumentAllowsPrinting_fake.return_val = false;
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorPrintingNotAllowed, @"Result must be kPDFErrorPrintingNotAllowed.");
    [mockFileManager stopMocking];
}

- (void)testSetupDocument_PDFAllowsPrinting
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(YES)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    CGPDFDocumentCreateWithURL_fake.return_val = (CGPDFDocumentRef)1;
    CGPDFDocumentIsUnlocked_fake.return_val = true;
    CGPDFDocumentIsEncrypted_fake.return_val = true;
    CGPDFDocumentAllowsPrinting_fake.return_val = true;
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorNone, @"Result must be kPDFErrorNone.");
    [mockFileManager stopMocking];
}

- (void)testSetupDocument_PDFIsNotEncrypted
{
    // Mock
    NSFileManager *fileManager = [NSFileManager defaultManager];
    id mockFileManager = [OCMockObject partialMockForObject:fileManager];
    [[[[mockFileManager stub] andForwardToRealObject] ignoringNonObjectArgs] stringWithFileSystemRepresentation:"~/Documents" length:strlen("~/Documents")];
    [[mockFileManager stub] removeItemAtPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    [[[mockFileManager stub] andReturnValue:OCMOCK_VALUE(YES)] moveItemAtPath:OCMOCK_ANY toPath:OCMOCK_ANY error:(NSError * __autoreleasing *)[OCMArg anyObjectRef]];
    
    CGPDFDocumentCreateWithURL_fake.return_val = (CGPDFDocumentRef)1;
    CGPDFDocumentIsUnlocked_fake.return_val = true;
    CGPDFDocumentIsEncrypted_fake.return_val = false;
    
    // SUT
    self.manager.fileAvailableForLoad = YES;
    self.manager.fileURL = pdfURL;
    kPDFError result = [self.manager setupDocument];
    
    // Verfication
    GHAssertEquals(result, kPDFErrorNone, @"Result must be kPDFErrorNone.");
    [mockFileManager stopMocking];
}

@end
