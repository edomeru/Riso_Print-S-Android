//
//  AlertHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "AlertHelper.h"
#import "CXAlertView.h"

@interface AlertHelperTest : GHTestCase

@property (nonatomic, strong) NSDictionary *titleDictionary;
@property (nonatomic, strong) NSDictionary *resultDictionary;
@property (nonatomic, strong) NSDictionary *confirmTitleDictionary;
@property (nonatomic, strong) NSDictionary *confirmMsgDictionary;

@end

@implementation AlertHelperTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    self.titleDictionary = @{[NSNumber numberWithInt:kAlertTitleDefault]: NSLocalizedString(IDS_APP_NAME, @""),
                             [NSNumber numberWithInt:kAlertTitlePrinters]: NSLocalizedString(IDS_LBL_PRINTER, @""),
                             [NSNumber numberWithInt:kAlertTitlePrintersAdd]: NSLocalizedString(IDS_LBL_ADD_PRINTER, @""),
                             [NSNumber numberWithInt:kAlertTitlePrintersSearch]: NSLocalizedString(IDS_LBL_SEARCH_PRINTERS, @""),
                             [NSNumber numberWithInt:kAlertTitlePrintJobHistory]: NSLocalizedString(IDS_LBL_PRINT_JOB_HISTORY, @""),
                             [NSNumber numberWithInt:100]: NSLocalizedString(IDS_APP_NAME, @"")
                             };
    
    self.resultDictionary = @{[NSNumber numberWithInt:kAlertResultErrDefault]: NSLocalizedString(IDS_ERR_MSG_DB_FAILURE, @""),
                              [NSNumber numberWithInt:kAlertResultInfoPrinterAdded]: NSLocalizedString(IDS_INFO_MSG_PRINTER_ADD_SUCCESSFUL, @""),
                              [NSNumber numberWithInt:kAlertResultErrNoNetwork]: NSLocalizedString(IDS_ERR_MSG_NETWORK_ERROR, @""),
                              [NSNumber numberWithInt:kAlertResultErrInvalidIP]: NSLocalizedString(IDS_ERR_MSG_INVALID_IP_ADDRESS, @""),
                              [NSNumber numberWithInt:kAlertResultErrMaxPrinters]: NSLocalizedString(IDS_ERR_MSG_MAX_PRINTER_COUNT, @""),
                              [NSNumber numberWithInt:kAlertResultErrPrinterNotFound]: NSLocalizedString(IDS_INFO_MSG_WARNING_CANNOT_FIND_PRINTER, @""),
                              [NSNumber numberWithInt:kAlertResultErrPrinterDuplicate]: NSLocalizedString(IDS_ERR_MSG_CANNOT_ADD_PRINTER, @""),
                              [NSNumber numberWithInt:kAlertResultErrDB]: NSLocalizedString(IDS_ERR_MSG_DB_FAILURE, @""),
                              [NSNumber numberWithInt:kAlertResultErrDB]: NSLocalizedString(IDS_ERR_MSG_DB_FAILURE, @""),
                              [NSNumber numberWithInt:kAlertResultFileCannotBeOpened]: NSLocalizedString(IDS_ERR_MSG_OPEN_FAILED, @""),
                              [NSNumber numberWithInt:kAlertResultPrintSuccessful]: NSLocalizedString(IDS_INFO_MSG_PRINT_JOB_SUCCESSFUL, @""),
                              [NSNumber numberWithInt:kAlertResultPrintFailed]: NSLocalizedString(IDS_INFO_MSG_PRINT_JOB_FAILED, @""),
                              [NSNumber numberWithInt:100]: NSLocalizedString(IDS_ERR_MSG_DB_FAILURE, @"")
                              };
    
    self.confirmTitleDictionary = @{[NSNumber numberWithInt:kAlertConfirmationDeleteAllJobs]: NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS_TITLE, @""),
                                    [NSNumber numberWithInt:kAlertConfirmationDeleteJob]: NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS_TITLE, @""),
                                    [NSNumber numberWithInt:kAlertConfirmationDeletePrinter]: NSLocalizedString(IDS_LBL_PRINTERS, @"")
                                    };
    
    self.confirmMsgDictionary = @{[NSNumber numberWithInt:kAlertConfirmationDeleteAllJobs]: NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @""),
                                    [NSNumber numberWithInt:kAlertConfirmationDeleteJob]: NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @""),
                                    [NSNumber numberWithInt:kAlertConfirmationDeletePrinter]: NSLocalizedString(IDS_INFO_MSG_DELETE_JOBS, @"")
                                    };
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
}

#pragma mark - Test Cases

/* TEST CASES ARE EXECUTED IN ALPHABETICAL ORDER */
/* use a naming scheme for defining the execution order of your test cases */

- (void)testDisplayResult_Title
{
    // Mock
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    [[mockCXAlertView stub] setDidDismissHandler:OCMOCK_ANY];
    [[mockCXAlertView stub] show];
    for (NSNumber *key in [self.titleDictionary allKeys])
    {
        NSString *title = [self.titleDictionary objectForKey:key];
        (void)[[[mockCXAlertView expect] andReturn:mockCXAlertView] initWithTitle:title message:OCMOCK_ANY cancelButtonTitle:OCMOCK_ANY];
    }
    
    // SUT
    for (NSNumber *key in [self.titleDictionary allKeys])
    {
        kAlertTitle title = [key intValue];
        [AlertHelper displayResult:kAlertResultErrDefault withTitle:title withDetails:nil];
    }
    
    // Verification
    GHAssertNoThrow([mockCXAlertView verify], @"");
    [mockCXAlertView stopMocking];
}

- (void)testDisplayResult_Result
{
    // Mock
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    [[mockCXAlertView stub] setDidDismissHandler:OCMOCK_ANY];
    [[mockCXAlertView stub] show];
    for (NSNumber *key in [self.resultDictionary allKeys])
    {
        NSString *result = [self.resultDictionary objectForKey:key];
        (void)[[[mockCXAlertView expect] andReturn:mockCXAlertView] initWithTitle:OCMOCK_ANY message:result cancelButtonTitle:OCMOCK_ANY];
    }
    
    // SUT
    for (NSNumber *key in [self.resultDictionary allKeys])
    {
        kAlertResult result = [key intValue];
        [AlertHelper displayResult:result withTitle:kAlertTitleDefault withDetails:nil];
    }
    
    // Verification
    GHAssertNoThrow([mockCXAlertView verify], @"");
    [mockCXAlertView stopMocking];
}

- (void)testDisplayConfirm
{
    // Mock
    id mockCXAlertView = [OCMockObject mockForClass:[CXAlertView class]];
    [[[mockCXAlertView stub] andReturn:mockCXAlertView] alloc];
    [[mockCXAlertView stub] addButtonWithTitle:OCMOCK_ANY type:CXAlertViewButtonTypeDefault handler:OCMOCK_ANY];
    [[mockCXAlertView stub] show];
    for (NSNumber *key in [self.confirmTitleDictionary allKeys])
    {
        NSString *title = [self.confirmTitleDictionary objectForKey:key];
        NSString *msg = [self.confirmMsgDictionary objectForKey:key];
        (void)[[[mockCXAlertView expect] andReturn:mockCXAlertView] initWithTitle:title message:msg cancelButtonTitle:nil];
    }
    
    // SUT
    for (NSNumber *key in [self.confirmTitleDictionary allKeys])
    {
        kAlertConfirmation confirmation = [key intValue];
        [AlertHelper displayConfirmation:confirmation withCancelHandler:nil withConfirmHandler:nil];
    }
    
    // Verification
    GHAssertNoThrow([mockCXAlertView verify], @"");
    [mockCXAlertView stopMocking];
}

/*- (void)test001_DisplayResult
{
    GHTestLog(@"# CHECK: AlertHelper can display result/info. #");
    
    GHTestLog(@"-- setting different alert messages");
    [self displayAlertWithResult:kAlertResultInfoPrinterAdded];
    [self displayAlertWithResult:kAlertResultErrNoNetwork];
    [self displayAlertWithResult:kAlertResultErrInvalidIP];
    [self displayAlertWithResult:kAlertResultErrMaxPrinters];
    [self displayAlertWithResult:kAlertResultErrPrinterNotFound];
    [self displayAlertWithResult:kAlertResultErrPrinterDuplicate];
    [self displayAlertWithResult:kAlertResultErrPrinterCannotBeAdded];
    [self displayAlertWithResult:kAlertResultErrDefault];
    
    GHTestLog(@"-- setting different alert titles");
    [self displayAlertWithTitle:kAlertTitlePrinters];
    [self displayAlertWithTitle:kAlertTitlePrintersAdd];
    [self displayAlertWithTitle:kAlertTitlePrintersSearch];
    [self displayAlertWithTitle:kAlertTitlePrintJobHistory];
    [self displayAlertWithTitle:kAlertTitleDefault];
}

- (void)test002_DisplayConfirmation
{
    GHTestLog(@"# CHECK: AlertHelper can display confirmation.");
    
    GHTestLog(@"-- setting different confirmations");
    [self displayAlertWithConfirmation:kAlertConfirmationDeleteAllJobs];
}

#pragma mark - Utilities

- (void)displayAlertWithResult:(kAlertResult)result
{
    GHTestLog(@"-- result type=%d", result);
    [AlertHelper displayResult:result withTitle:kAlertTitleDefault withDetails:nil];
    [self checkAndDismissResultAlert];
}

- (void)displayAlertWithTitle:(kAlertTitle)title
{
    GHTestLog(@"-- title type=%d", title);
    [AlertHelper displayResult:kAlertResultErrDefault withTitle:title withDetails:nil];
    [self checkAndDismissResultAlert];
}

- (void)displayAlertWithConfirmation:(kAlertConfirmation)confirmation
{
    GHTestLog(@"-- confirmation type=%d", confirmation);
    
    void (^dismiss)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];
    };
    
    [AlertHelper displayConfirmation:confirmation withCancelHandler:dismiss withConfirmHandler:dismiss];
    [self checkAndDismissConfirmationAlert];
}

- (void)checkAndDismissResultAlert
{
    [self waitForCompletion:1.5];
    
    for (UIWindow* window in [[UIApplication sharedApplication] windows])
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                
                GHAssertNotNil(alert, @"");
                GHAssertNotNil(alert.title, @"");
                GHAssertNotNil(alert.contentView, @"");
                GHAssertTrue(![alert.title isEqualToString:@""], @"");
                GHAssertTrue([alert.buttons count] == 1, @"");
                
                [alert cleanAllPenddingAlert];
                [alert dismiss];
                [self waitForCompletion:1.5];
                alert = nil;
                
                return;
            }
        }
    }
    
    GHFail(@"alert was not displayed");
}

- (void)checkAndDismissConfirmationAlert
{
    [self waitForCompletion:1.5];
    
    for (UIWindow* window in [[UIApplication sharedApplication] windows])
    {
        NSArray* subViews = window.subviews;
        if ([subViews count] > 0)
        {
            UIView* view = [subViews objectAtIndex:0];
            if ([view isKindOfClass:[CXAlertView class]])
            {
                CXAlertView* alert = (CXAlertView*)view;
                
                GHAssertNotNil(alert, @"");
                GHAssertNotNil(alert.title, @"");
                GHAssertTrue(![alert.title isEqualToString:@""], @"");
                
                [alert cleanAllPenddingAlert];
                [alert dismiss];
                [self waitForCompletion:1.5];
                alert = nil;
                
                return;
            }
        }
    }
    
    GHFail(@"alert was not displayed");
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
}*/

@end
