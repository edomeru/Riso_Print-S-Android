//
//  DirectPrinterManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "DirectPrintManager.h"
#import "PreviewSetting.h"
#import "Printer.h"
#import "PrintDocument.h"
#import "PDFFileManager.h"
#import "PrintJobHistoryHelper.h"
#import "CXAlertView.h"
#include "common.h"

#include "fff.h"
DEFINE_FFF_GLOBALS;

FAKE_VALUE_FUNC(int, directprint_job_lpr_print);
FAKE_VALUE_FUNC(int, directprint_job_raw_print);
FAKE_VOID_FUNC(directprint_job_cancel);
FAKE_VALUE_FUNC(directprint_job *, directprint_job_new);
FAKE_VOID_FUNC(directprint_job_free);
FAKE_VALUE_FUNC(void *, directprint_job_get_caller_data);
FAKE_VOID_FUNC(directprint_job_set_caller_data, directprint_job *, void *);

extern void printProgressCallback(void *job, int status, float progress);
void *caller_data_obj;

int print_OK()
{
    printProgressCallback(0, kJobStatusSent, 100.0f);
    return 1;
}

int print_NG()
{
    printProgressCallback(0, kJobStatusErrorSending, 20.0f);
    return 1;
}

int print_NA()
{
    printProgressCallback(0, kJobStatusSending, 20.0f);
    return 1;
}

void set_caller_data(directprint_job *job, void *data)
{
    caller_data_obj = data;
}

void *get_caller_data()
{
    return caller_data_obj;
}

@interface DirectPrintManagerTest : GHAsyncTestCase <DirectPrintManagerDelegate>

@property (atomic, assign) NSInteger alertCount;

@end

@implementation DirectPrintManagerTest

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUp
{
    [MagicalRecord setDefaultModelFromClass:[self class]];
    [MagicalRecord setupCoreDataStackWithInMemoryStore];
    self.alertCount = 0;
    
    RESET_FAKE(directprint_job_lpr_print);
    RESET_FAKE(directprint_job_raw_print);
    RESET_FAKE(directprint_job_cancel);
    RESET_FAKE(directprint_job_new);
    RESET_FAKE(directprint_job_free);
    RESET_FAKE(directprint_job_get_caller_data);
    RESET_FAKE(directprint_job_set_caller_data);
    
    FFF_RESET_HISTORY();
    
    directprint_job_new_fake.return_val = (directprint_job *)1;
}

- (void)tearDown
{
    [MagicalRecord cleanUp];
}

#pragma mark - DirectPrintManagerDelegate

- (void)documentDidFinishPrinting:(BOOL)successful
{
    if (successful)
    {
        [self notify:kGHUnitWaitStatusSuccess];
    }
    else
    {
        [self notify:kGHUnitWaitStatusFailure];
    }
}

#pragma mark - Test Cases

- (void)testLprPrintOK
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock PrintJobHistoryHelper
    id mockPrintJobHistoryHelper = [OCMockObject mockForClass:[PrintJobHistoryHelper class]];
    __block int added_result = -1;
    [[[mockPrintJobHistoryHelper stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&added_result atIndex:3];
    }] createPrintJobFromDocument:[OCMArg any] withResult:1];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    [[mockCXAlertView stub] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    __block CXAlertViewHandler dismissHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&dismissHandler atIndex:2];
    }] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        self.alertCount++;
        if (self.alertCount == 2)
        {
            CXAlertView *alertView = invocation.target;
            [alertView dismiss];
        }
    }] show];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        CXAlertView *alertView = invocation.target;
        if (dismissHandler)
        {
            dismissHandler(alertView);
        }
    }] dismiss];
    
    // Mock DirectPrint
    directprint_job_lpr_print_fake.custom_fake = print_OK;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaLPR];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:20.0f];
    GHAssertEquals(added_result, 1, @"PrintJobHistory result should be 1");
    [mockPDFFileManager stopMocking];
}

- (void)testLprPrintNG
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock PrintJobHistoryHelper
    id mockPrintJobHistoryHelper = [OCMockObject mockForClass:[PrintJobHistoryHelper class]];
    __block int added_result = -1;
    [[[mockPrintJobHistoryHelper stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&added_result atIndex:3];
    }] createPrintJobFromDocument:[OCMArg any] withResult:0];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    [[mockCXAlertView stub] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    __block CXAlertViewHandler dismissHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&dismissHandler atIndex:2];
    }] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        self.alertCount++;
        if (self.alertCount == 2)
        {
            CXAlertView *alertView = invocation.target;
            [alertView dismiss];
        }
    }] show];
    [[mockCXAlertView stub] dismiss];
    
    // Mock DirectPrint
    directprint_job_lpr_print_fake.custom_fake = print_NG;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaLPR];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusFailure timeout:20.0f];
    GHAssertEquals(added_result, 0, @"PrintJobHistory result should be 0");
    [mockPDFFileManager stopMocking];
}

- (void)testLprPrintCancel
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    __block CXAlertButtonHandler buttonHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&buttonHandler atIndex:4];
    }] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    [[mockCXAlertView stub] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        CXAlertView *alertView = invocation.target;
        buttonHandler(alertView, nil);
        [self notify:kGHUnitWaitStatusCancelled];
    }] show];
    [[mockCXAlertView stub] dismiss];
    
    // Mock DirectPrint
    directprint_job_lpr_print_fake.custom_fake = print_NA;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaLPR];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusCancelled timeout:20.0f];
    [mockPDFFileManager stopMocking];
}

- (void)testRawPrintOK
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock PrintJobHistoryHelper
    id mockPrintJobHistoryHelper = [OCMockObject mockForClass:[PrintJobHistoryHelper class]];
    __block int added_result = -1;
    [[[mockPrintJobHistoryHelper stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&added_result atIndex:3];
    }] createPrintJobFromDocument:[OCMArg any] withResult:1];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    [[mockCXAlertView stub] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    __block CXAlertViewHandler dismissHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&dismissHandler atIndex:2];
    }] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        self.alertCount++;
        if (self.alertCount == 2)
        {
            CXAlertView *alertView = invocation.target;
            [alertView dismiss];
        }
    }] show];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        CXAlertView *alertView = invocation.target;
        if (dismissHandler)
        {
            dismissHandler(alertView);
        }
    }] dismiss];
    
    // Mock DirectPrint
    directprint_job_raw_print_fake.custom_fake = print_OK;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaRaw];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:20.0f];
    GHAssertEquals(added_result, 1, @"PrintJobHistory result should be 1");
    [mockPDFFileManager stopMocking];
}

- (void)testRawPrintNG
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock PrintJobHistoryHelper
    id mockPrintJobHistoryHelper = [OCMockObject mockForClass:[PrintJobHistoryHelper class]];
    __block int added_result = -1;
    [[[mockPrintJobHistoryHelper stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&added_result atIndex:3];
    }] createPrintJobFromDocument:[OCMArg any] withResult:0];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    [[mockCXAlertView stub] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    __block CXAlertViewHandler dismissHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&dismissHandler atIndex:2];
    }] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        self.alertCount++;
        if (self.alertCount == 2)
        {
            CXAlertView *alertView = invocation.target;
            [alertView dismiss];
        }
    }] show];
    [[mockCXAlertView stub] dismiss];
    
    // Mock DirectPrint
    directprint_job_raw_print_fake.custom_fake = print_NG;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaRaw];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusFailure timeout:20.0f];
    GHAssertEquals(added_result, 0, @"PrintJobHistory result should be 0");
    [mockPDFFileManager stopMocking];
}

- (void)testRawPrintCancel
{
    [self prepare];
    
    // Mock Data
    Printer *printer = [Printer MR_createEntity];
    printer.ip_address = @"192.168.1.1";
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    PrintDocument *document = [[PrintDocument alloc] initWithURL:url name:@"TestPDF_3Pages_NoPass.pdf"];
    document.printer = printer;
    document.previewSetting = [[PreviewSetting alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:document] printDocument];
    
    // Mock CXAlertView
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    (void)[[[mockCXAlertView stub] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY contentView:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    __block CXAlertButtonHandler buttonHandler;
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        [invocation getArgument:&buttonHandler atIndex:4];
    }] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeCancel handler:OCMOCK_ANY];
    [[mockCXAlertView stub] setDidDismissHandler:OCMOCK_ANY];
    [[[mockCXAlertView stub] andDo:^(NSInvocation *invocation){
        CXAlertView *alertView = invocation.target;
        buttonHandler(alertView, nil);
        [self notify:kGHUnitWaitStatusCancelled];
    }] show];
    [[mockCXAlertView stub] dismiss];
    
    // Mock DirectPrint
    directprint_job_raw_print_fake.custom_fake = print_NA;
    directprint_job_set_caller_data_fake.custom_fake = set_caller_data;
    directprint_job_get_caller_data_fake.custom_fake = get_caller_data;
    
    // SUT
    DirectPrintManager *manager = [[DirectPrintManager alloc] init];
    manager.delegate = self;
    [manager printDocumentViaRaw];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusCancelled timeout:20.0f];
    [mockPDFFileManager stopMocking];
}

@end
