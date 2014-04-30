//
//  PDFFileManagerTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PDFFileManager.h"
#import "PrintDocument.h"

#define PDFFILE_MANAGER_TEST 1
#if PDFFILE_MANAGER_TEST

@interface PDFFileManager(Test)
- (kPDFError)verifyDocument:(NSURL *)documentURL;
- (BOOL)moveFileToDocuments:(NSURL **)documentURL;
@end

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
}
- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    self.manager = [PDFFileManager sharedManager];
    
    testPDFNoPassURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    testPDFWithPassURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(test123-None)" withExtension:@"pdf"];
    testPDFEncryptedPrintAllowedURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(None-123)-PrintingAllowed" withExtension:@"pdf"];
    testPDFEncryptedPrintNotAllowedURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_Pass(None-123)-PrintingNotAllowed" withExtension:@"pdf"];
    
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    
    applicationPDFURL = [NSURL URLWithString:[[documentsDir stringByAppendingString:@"/SDAPreview.pdf"] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
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
    [self.manager setFileURL:nil];
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:applicationPDFURL.path error:&error];
}

- (void)test001_sharedManager
{
    GHAssertNotNil(self.manager, @"");
    //Test singleton
    GHAssertEqualObjects(self.manager, [PDFFileManager sharedManager], @"");
}

- (void)test002_verifyDocument
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

}
@end
#endif
