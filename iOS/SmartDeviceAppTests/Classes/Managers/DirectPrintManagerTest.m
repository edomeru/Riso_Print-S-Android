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
#import "Swizzler.h"
#import "DirectPrintManagerMock.h"

static NSString* TEST_PRINTER_IP_SUCCESS = @"192.168.0.198";
static NSString* TEST_PRINTER_IP_FAILED = @"192.168.0.1";

@interface DirectPrintManager (UnitTest)

// expose private variables
- (CXAlertView*)alertView;

@end

@interface DirectPrintManagerTest : GHTestCase <DirectPrintManagerDelegate>
{
    DirectPrintManager* dpm;
    BOOL documentDidFinishCallbackReceived;
    BOOL documentDidPrintSuccessfully;
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
    PDFFileManager* pdfManager = [PDFFileManager sharedManager];
    pdfManager.printDocument.printer = nil;
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    [self removeErrorDialogIfPresent];
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
    
    //attach the correct printer
    PDFFileManager* pdfm = [PDFFileManager sharedManager];
    pdfm.printDocument.printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:0];
    
    GHTestLog(@"-- printing the document");
    documentDidFinishCallbackReceived = NO;
    Swizzler *swizzler = [[Swizzler alloc] init];
    [swizzler swizzleInstanceMethod:[DirectPrintManager class] targetSelector:@selector(printDocumentViaLPR) swizzleClass:[DirectPrintManagerMock class] swizzleSelector:@selector(printDocumentViaLPR)];
    [dpm printDocumentViaLPR];
    [self waitForCompletion:10];
    [swizzler deswizzle];
    GHTestLog(@"-- printing finished");

    [self removeErrorDialogIfPresent];
    
    GHAssertTrue(documentDidFinishCallbackReceived,
                 [NSString stringWithFormat:@"Check if callbak is receieved"]);
    GHAssertTrue(documentDidPrintSuccessfully,
                 [NSString stringWithFormat:@"check if printed successfullly"]);
}

- (void)test002_PrintDocumentViaRAW
{
    GHTestLog(@"# CHECK: DPM can print via RAW. #");
    
    //attach the correct printer
    PDFFileManager* pdfm = [PDFFileManager sharedManager];
    pdfm.printDocument.printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:0];
    
    GHTestLog(@"-- printing the document");
    documentDidFinishCallbackReceived = NO;
    Swizzler *swizzler = [[Swizzler alloc] init];
    [swizzler swizzleInstanceMethod:[DirectPrintManager class] targetSelector:@selector(printDocumentViaRaw) swizzleClass:[DirectPrintManagerMock class] swizzleSelector:@selector(printDocumentViaRaw)];
    [dpm printDocumentViaRaw];
    [self waitForCompletion:10];
    [swizzler deswizzle];
    GHTestLog(@"-- printing finished");
    
    [self removeErrorDialogIfPresent];
    
    GHAssertTrue(documentDidFinishCallbackReceived,
                 [NSString stringWithFormat:@"Check if callbak is receieved"]);
    GHAssertTrue(documentDidPrintSuccessfully,
                 [NSString stringWithFormat:@"check if printed successfullly"]);
}

- (void)test003_PrintDocumentError
{
    GHTestLog(@"# CHECK: DPM can handle print error. #");
    
    //attach the incorrect printer
    PDFFileManager* pdfm = [PDFFileManager sharedManager];
    pdfm.printDocument.printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:1];
    
    GHTestLog(@"-- printing the document");
    documentDidFinishCallbackReceived = NO;
    [dpm printDocumentViaLPR];
    [self waitForCompletion:10];
    GHTestLog(@"-- printing finished");
    
    [self removeErrorDialogIfPresent];
    
    GHAssertTrue(documentDidFinishCallbackReceived,
                 [NSString stringWithFormat:@"Check if callbak is receieved"]);
    GHAssertFalse(documentDidPrintSuccessfully,
                 [NSString stringWithFormat:@"check if printing failed"]);
}

#pragma mark - DirectPrintManagerDelegate Methods

- (void)documentDidFinishPrinting:(BOOL)successful
{
    documentDidFinishCallbackReceived = YES;
    documentDidPrintSuccessfully = successful;
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
    
    PrinterDetails* pd1 = [[PrinterDetails alloc] init];
    GHAssertNotNil(pd1, @"check initialization of PrinterDetails");
    pd1.name = @"RISO Printer 1";
    pd1.ip = TEST_PRINTER_IP_SUCCESS;
    pd1.port = [NSNumber numberWithInt:0];
    pd1.enBooklet = YES;
    pd1.enStaple = YES;
    pd1.enFinisher23Holes = NO;
    pd1.enFinisher24Holes = YES;
    pd1.enTrayAutoStacking = YES;
    pd1.enTrayFaceDown = YES;
    pd1.enTrayStacking = YES;
    pd1.enTrayTop = YES;
    pd1.enLpr = YES;
    pd1.enRaw = YES;
    
    PrinterDetails* pd2 = [[PrinterDetails alloc] init];
    GHAssertNotNil(pd2, @"check initialization of PrinterDetails");
    pd2.name = @"RISO Printer 1";
    pd2.ip = TEST_PRINTER_IP_FAILED;
    pd2.port = [NSNumber numberWithInt:0];
    pd2.enBooklet = YES;
    pd2.enStaple = YES;
    pd2.enFinisher23Holes = NO;
    pd2.enFinisher24Holes = YES;
    pd2.enTrayAutoStacking = YES;
    pd2.enTrayFaceDown = YES;
    pd2.enTrayStacking = YES;
    pd2.enTrayTop = YES;
    pd2.enLpr = YES;
    pd2.enRaw = YES;
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(pm, @"check initialization of PrinterManager");
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    GHAssertTrue([pm registerPrinter:pd1], @"");
    GHAssertTrue([pm registerPrinter:pd2], @"");
}

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

- (void)removeErrorDialogIfPresent
{
    for (UIWindow* window in [UIApplication sharedApplication].windows)
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                [alert dismiss];
                [self waitForCompletion:2];
                alert = nil;
            }
        }
    }
}

@end
