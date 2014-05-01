//
//  DirectPrinterManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "DirectPrintManager.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "PrinterDetails.h"
#import "CXAlertView.h"

@interface DirectPrintManager (UnitTest)

// expose private properties
- (CXAlertView*)alertView;

@end

@interface DirectPrintManagerTest : GHTestCase <DirectPrintManagerDelegate>
{
    DirectPrintManager* dpm;
    BOOL documentDidFinishCallbackReceived;
}

@end

@implementation DirectPrintManagerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return NO;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    dpm = [[DirectPrintManager alloc] init];
    GHAssertNotNil(dpm, @"check initialization of DirectPrintManager");
    dpm.delegate = self;
    
    [self prepareForPrinting];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
}

// Run before each test method
- (void)setUp
{
}

// Run after each test method
- (void)tearDown
{
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)test001_PrintDocumentViaLPR
{
    GHTestLog(@"# CHECK: DPM can print via LPR. #");
    
    GHTestLog(@"-- printing the document");
    documentDidFinishCallbackReceived = NO;
    [dpm printDocumentViaLPR];
    [self waitForCompletion:5];
    GHTestLog(@"-- printing finished");

    GHAssertTrue(documentDidFinishCallbackReceived, @"");
}

- (void)test002_PrintDocumentViaRAW
{
    GHTestLog(@"# CHECK: DPM can print via RAW. #");
    
    GHTestLog(@"-- printing the document");
    documentDidFinishCallbackReceived = NO;
    [dpm printDocumentViaRaw];
    [self waitForCompletion:5];
    GHTestLog(@"-- printing finished");
    
    GHAssertTrue(documentDidFinishCallbackReceived, @"");
}

#pragma mark - DirectPrintManagerDelegate Methods

- (void)documentDidFinishPrinting:(BOOL)successful
{
    documentDidFinishCallbackReceived = YES;
}

#pragma mark - Utilities

- (void)prepareForPrinting
{
    //-- PDF
    
    PDFFileManager* pdfManager = [PDFFileManager sharedManager];
    GHAssertNotNil(pdfManager, @"check initialization of PrinterManager");
    
    NSURL* testPDFNoPassURL = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString* documentsDir = [documentPaths objectAtIndex:0];
    NSString* testFilePath = [documentsDir stringByAppendingString:
                              [NSString stringWithFormat:@"/%@",[testPDFNoPassURL.path lastPathComponent]]];
    NSURL* testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:
                                           NSUTF8StringEncoding]];
    GHAssertNotNil(testURL, @"");
    
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:testFilePath error:&error];
    [[NSFileManager defaultManager] copyItemAtPath:[testPDFNoPassURL path] toPath:testFilePath error:&error];
    
    pdfManager.fileURL = testURL;
    GHAssertTrue([pdfManager setupDocument] == kPDFErrorNone, @"");
    
    //-- Printer
    
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    GHAssertNotNil(pd, @"check initialization of PrinterDetails");
    pd.name = @"RISO Printer 1";
    pd.ip = @"192.168.0.199";
    pd.port = [NSNumber numberWithInt:0];
    pd.enBooklet = YES;
    pd.enStaple = YES;
    pd.enFinisher23Holes = YES;
    pd.enFinisher24Holes = YES;
    pd.enTrayAutoStacking = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
    pd.enLpr = YES;
    pd.enRaw = YES;
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(pm, @"check initialization of PrinterManager");
    GHAssertTrue(pm.countSavedPrinters == 0, @"should initially have no printers");
    GHAssertTrue([pm registerPrinter:pd], @"");
    Printer* testPrinter = [pm getPrinterAtIndex:0];
    GHAssertNotNil(testPrinter, @"");
    
    //-- Printer-PDF
    
    pdfManager.printDocument.printer = testPrinter;
}

/**
 @see http://www.infinite-loop.dk/blog/2011/04/unittesting-asynchronous-network-access/
 */
- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    return done;
}

@end
