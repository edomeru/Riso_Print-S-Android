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

#include "fff.h"
DEFINE_FFF_GLOBALS;

FAKE_VALUE_FUNC(CGPDFDocumentRef, CGPDFDocumentCreateWithURL);
FAKE_VOID_FUNC(CGPDFDocumentRelease);
FAKE_VALUE_FUNC(bool, CGPDFDocumentIsUnlocked);
FAKE_VALUE_FUNC(bool, CGPDFDocumentIsEncrypted);
FAKE_VALUE_FUNC(bool, CGPDFDocumentAllowsPrinting);

/*@interface PDFFileManager(Test)
@property (nonatomic) BOOL fileAvailableForPreview;
- (kPDFError)verifyDocument:(NSURL *)documentURL;
- (BOOL)moveFileToDocuments:(NSURL **)documentURL;
@end*/

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

/*- (void)test002_verifyDocument
{
    kPDFError status = [self.manager verifyDocument:testPDFNoPassURL];
    GHAssertEquals(status, kPDFErrorNone, @"");
    
    status = [self.manager verifyDocument:testPDFWithPassURL];
    GHAssertEquals(status, kPDFErrorLocked, @"");
    
    status = [self.manager verifyDocument:testPDFEncryptedPrintNotAllowedURL];
    GHAssertEquals(status, kPDFErrorPrintingNotAllowed, @"");
    
    status = [self.manager verifyDocument:testPDFEncryptedPrintAllowedURL];
    GHAssertEquals(status, kPDFErrorNone, @"");
}

- (void)test003_moveFileToDocuments
{
    BOOL  retVal;
    //Put dummy file in document
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [
                              NSString stringWithFormat:@"/%@",[testPDFNoPassURL.path lastPathComponent]]];

     NSURL *testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    NSURL *newDocumentURL = nil;
    
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:testFilePath error:&error];
    
    //test URL when file does not exist
    [self.manager setFileURL:testURL];
    retVal = [self.manager moveFileToDocuments:&newDocumentURL];
    GHAssertFalse(retVal,@"");
    GHAssertNil(newDocumentURL,@"");
    
    [[NSFileManager defaultManager] copyItemAtPath:[testPDFNoPassURL path] toPath: testFilePath error:&error];
    if([[NSFileManager defaultManager] fileExistsAtPath:testFilePath] == NO)
    {
        NSLog(@"not successful copy");
        return;
    }
     NSLog(@"successful copy");
    

    //test with URL item
    [self.manager setFileURL:testURL];
    retVal = [self.manager moveFileToDocuments:&newDocumentURL];
    GHAssertTrue(retVal, @"");
    GHAssertNotNil(newDocumentURL,@"");
    GHAssertEqualStrings(newDocumentURL.path, applicationPDFURL.path, @"");
    GHAssertTrue([[NSFileManager defaultManager] fileExistsAtPath:newDocumentURL.path], @"");
    GHAssertFalse([[NSFileManager defaultManager] fileExistsAtPath:testFilePath], @"");
    
}

- (void)test004_setupDocument
{
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[testPDFNoPassURL.path lastPathComponent]]];
    
    NSURL *testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:testFilePath error:&error];
    
    kPDFError retVal;
    [self.manager setFileURL:testURL];
    retVal = [self.manager setupDocument];
    GHAssertEquals(retVal, kPDFErrorProcessingFailed, @"");
    GHAssertFalse(self.manager.fileAvailableForPreview, @"");
    GHAssertNil(self.manager.printDocument, @"");
    
   
    [[NSFileManager defaultManager] copyItemAtPath:[testPDFNoPassURL path] toPath: testFilePath error:&error];
    if([[NSFileManager defaultManager] fileExistsAtPath:testFilePath] == NO)
    {
        return;
    }
    
    retVal = [self.manager setupDocument];
    GHAssertEquals(retVal, kPDFErrorNone, @"");
    GHAssertTrue(self.manager.fileAvailableForPreview, @"");
    GHAssertNotNil(self.manager.printDocument, @"");
    GHAssertNotNil(self.manager.printDocument.previewSetting, @"");

}*/
@end
