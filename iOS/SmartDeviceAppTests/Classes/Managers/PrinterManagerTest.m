//
//  PrinterManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"

const NSUInteger NUM_VALID_PRINTERS     = 4;
const NSUInteger NUM_INVALID_PRINTERS   = 2;
const NSUInteger NUM_TOTAL_PRINTERS = NUM_VALID_PRINTERS + NUM_INVALID_PRINTERS;
const NSUInteger MAX_PRINTERS = 10; //get from SmartDeviceApp-Settings.plist

const NSUInteger DEFAULT_1 = 2; //valid printer
const NSUInteger DEFAULT_2 = 4; //invalid printer

const NSString*  PRINTER_NAME       = @"UT-Printer";
const NSString*  VALID_PRINTER_IP   = @"192.168.0.19";
const NSString*  INVALID_PRINTER_IP = @"10.127.0.";

const float SEARCH_TIMEOUT = 10;

@interface PrinterManagerTest : GHTestCase <PrinterSearchDelegate>
{
    PrinterManager* printerManager;
    BOOL callbackSearchEndCalled;
    BOOL callbackFoundNewCalled;
    BOOL callbackFoundOldCalled;
}

@end

@implementation PrinterManagerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    GHAssertTrue(NUM_TOTAL_PRINTERS < MAX_PRINTERS, @"invalid test data");
    GHAssertTrue(DEFAULT_1 < NUM_TOTAL_PRINTERS, @"invalid test data");
    GHAssertTrue(DEFAULT_2 < NUM_TOTAL_PRINTERS, @"invalid test data");
    
    printerManager = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(printerManager, @"check initialization of PrinterManager");
    
    printerManager.searchDelegate = self;
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    while (printerManager.countSavedPrinters != 0)
        GHAssertTrue([printerManager deletePrinterAtIndex:0], @"printer should be deleted");
    
    printerManager = nil;
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

- (void)test001_Initialization
{
    GHTestLog(@"# CHECK: PM is properly initialized. #");
    
    GHAssertTrue(printerManager.countSavedPrinters == 0, @"printer count should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there should be no default printer");
}

- (void)test002_AddValidPrinters
{
    GHTestLog(@"# CHECK: PM can add Printers. #");
    
    GHTestLog(@"-- adding valid printers");
    int start = 0;
    int limit = NUM_VALID_PRINTERS;
    for (int i = start; i < limit; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        pd.ip = [NSString stringWithFormat:@"%@%d", VALID_PRINTER_IP, i+1];
        pd.port = [NSNumber numberWithInt:i%2];
        pd.enBooklet = YES;
        pd.enStaple = NO;
        pd.enFinisher23Holes = YES;
        pd.enFinisher24Holes = NO;
        pd.enTrayAutoStacking = YES;
        pd.enTrayFaceDown = NO;
        pd.enTrayStacking = YES;
        pd.enTrayTop = NO;
        pd.enLpr = YES;
        pd.enRaw = NO;
        GHTestLog(@"-- registering \"%@\",\"%@\"..", pd.name, pd.ip);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHAssertTrue([printerManager countSavedPrinters] == 0+NUM_VALID_PRINTERS, @"");
}

- (void)test003_AddInvalidPrinters
{
    GHTestLog(@"# CHECK: PM can add Printers. #");
    
    GHTestLog(@"-- adding invalid printers");
    int start = 0;
    int limit = NUM_INVALID_PRINTERS;
    for (int i = start; i < limit; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        //pd.name -- should be nil
        pd.ip = [NSString stringWithFormat:@"%@%d", INVALID_PRINTER_IP, i+1];
        pd.port = [NSNumber numberWithInt:i%2];
        pd.enBooklet = YES;
        pd.enStaple = YES;
        pd.enFinisher23Holes = NO;
        pd.enFinisher24Holes = YES;
        pd.enTrayAutoStacking = YES;
        pd.enTrayFaceDown = YES;
        pd.enTrayStacking = YES;
        pd.enTrayTop = YES;
        pd.enLpr = YES;
        pd.enRaw = YES;
        GHTestLog(@"-- registering \"%@\",\"%@\"..", pd.name, pd.ip);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHAssertTrue([printerManager countSavedPrinters] == NUM_VALID_PRINTERS+NUM_INVALID_PRINTERS, @"");
}

- (void)test004_GetValidPrinters
{
    GHTestLog(@"# CHECK: PM can retrieve Printers. #");
    
    GHTestLog(@"-- getting valid printers");
    int start = 0;
    int limit = NUM_VALID_PRINTERS;
    for (int i = start; i < limit; i++)
    {
        GHTestLog(@"-- getting printer i=%lu", (unsigned long)i);
        
        Printer* testPrinter = [printerManager getPrinterAtIndex:i];
        GHAssertNotNil(testPrinter, @"get printer should return a valid printer");
        
        NSString* expectedName = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        GHTestLog(@"--- name=[%@]", testPrinter.name);
        GHAssertEqualStrings(testPrinter.name, expectedName, @"");
        
        NSString* expectedIP = [NSString stringWithFormat:@"%@%d", VALID_PRINTER_IP, i+1];
        GHTestLog(@"--- ip=[%@]", testPrinter.ip_address);
        GHAssertEqualStrings(testPrinter.ip_address, expectedIP, @"");
        
        NSNumber* expectedPort = [NSNumber numberWithInt:i%2];
        GHAssertEquals([testPrinter.port intValue], [expectedPort intValue], @"");
        
        GHAssertTrue([testPrinter.enabled_lpr boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_raw boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_booklet boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_staple boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_finisher_2_3_holes boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_finisher_2_4_holes boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_tray_auto_stacking boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_tray_face_down boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_tray_stacking boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_tray_top boolValue], @"setting is different than expected");
        
        GHAssertFalse([printerManager isDefaultPrinter:testPrinter], @"there should be no default printer");
        
        GHAssertNil(testPrinter.defaultprinter, @"DefaultPrinter should be nil");
        GHAssertNotNil(testPrinter.printsetting, @"Printer should have a valid PrintSetting object");
        GHAssertNotNil(testPrinter.printjob, @"PrintJob should not be nil");
        GHAssertTrue([testPrinter.printjob count] == 0, @"there should be no print jobs");
    }
}

- (void)test005_GetInvalidPrinters
{
    GHTestLog(@"# CHECK: PM can retrieve Printers. #");
    
    GHTestLog(@"-- getting invalid printers");
    int start = NUM_VALID_PRINTERS;
    int limit = NUM_TOTAL_PRINTERS;
    for (int i = start; i < limit; i++)
    {
        GHTestLog(@"-- getting printer i=%lu", (unsigned long)i);
        
        Printer* testPrinter = [printerManager getPrinterAtIndex:i];
        GHAssertNotNil(testPrinter, @"get printer should return a valid printer");
        
        GHTestLog(@"--- name=[%@]", testPrinter.name);
        GHAssertNil(testPrinter.name, @"invalid printer should have a nil or @\"\" name");
        
        NSString* expectedIP = [NSString stringWithFormat:@"%@%d", INVALID_PRINTER_IP, (i-start)+1];
        GHTestLog(@"--- ip=[%@]", testPrinter.ip_address);
        GHAssertEqualStrings(testPrinter.ip_address, expectedIP, @"");
        
        NSNumber* expectedPort = [NSNumber numberWithInt:i%2];
        GHAssertEquals([testPrinter.port intValue], [expectedPort intValue], @"");
        
        GHAssertTrue([testPrinter.enabled_lpr boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_raw boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_booklet boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_staple boolValue], @"should have full capabilities");
        GHAssertFalse([testPrinter.enabled_finisher_2_3_holes boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_finisher_2_4_holes boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_tray_auto_stacking boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_tray_face_down boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_tray_stacking boolValue], @"should have full capabilities");
        GHAssertTrue([testPrinter.enabled_tray_top boolValue], @"should have full capabilities");
        
        GHAssertFalse([printerManager isDefaultPrinter:testPrinter], @"there should be no default printer");
        
        GHAssertNil(testPrinter.defaultprinter, @"DefaultPrinter should be nil");
        GHAssertNotNil(testPrinter.printsetting, @"Printer should have a valid PrintSetting object");
        GHAssertNotNil(testPrinter.printjob, @"PrintJob should not be nil");
        GHAssertTrue([testPrinter.printjob count] == 0, @"there should be no print jobs");
    }
}

- (void)test006_SetUnsetDefaultPrinter
{
    GHTestLog(@"# CHECK: PM knows which is the DefaultPrinter. #");
    
    GHTestLog(@"-- setting printer[%lu] to be the default printer", (unsigned long)DEFAULT_1);
    Printer* default1 = [printerManager getPrinterAtIndex:DEFAULT_1];
    BOOL setDefault1 = [printerManager registerDefaultPrinter:default1];
    GHAssertTrue(setDefault1, @"printer at index=%lu should be the default printer", (unsigned long)DEFAULT_1);
    GHAssertTrue([printerManager hasDefaultPrinter], @"there should be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_TOTAL_PRINTERS, @"printer count should remain the same");
    GHAssertTrue([printerManager isDefaultPrinter:default1], @"default1 should be the default printer");
    
    GHTestLog(@"-- setting printer[%lu] to be the default printer", (unsigned long)DEFAULT_2);
    Printer* default2 = [printerManager getPrinterAtIndex:DEFAULT_2];
    BOOL setDefault2 = [printerManager registerDefaultPrinter:default2];
    GHAssertTrue(setDefault2, @"printer at index=%lu should be the default printer", (unsigned long)DEFAULT_2);
    GHAssertTrue([printerManager hasDefaultPrinter], @"there should be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_TOTAL_PRINTERS, @"printer count should remain the same");
    GHAssertTrue([printerManager isDefaultPrinter:default2], @"default2 should be the default printer");
    GHAssertFalse([printerManager isDefaultPrinter:default1], @"default1 is not anymore the default printer");
    
    GHTestLog(@"-- unsetting printer[%lu] from being the default printer", (unsigned long)DEFAULT_2);
    GHAssertTrue([printerManager deleteDefaultPrinter], @"default printer should be removed");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there shouldn't be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_TOTAL_PRINTERS, @"printer count should remain the same");
    GHAssertFalse([printerManager isDefaultPrinter:default1], @"default1 is not anymore the default printer");
    GHAssertFalse([printerManager isDefaultPrinter:default2], @"default2 is not anymore the default printer");
}

- (void)test007_MaximumPrinters
{
    GHTestLog(@"# CHECK: PM can check for max printers. #");
    
    GHAssertFalse([printerManager isAtMaximumPrinters], @"count(%lu) < MAX_PRINTERS(%lu)",
                  (unsigned long)printerManager.countSavedPrinters,
                  (unsigned long)MAX_PRINTERS);
    
    // add until we reach maximum printers
    for (NSUInteger i = NUM_TOTAL_PRINTERS; i < MAX_PRINTERS; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        pd.ip = [NSString stringWithFormat:@"%@%d", VALID_PRINTER_IP, i+1];
        pd.port = [NSNumber numberWithInt:i%2];
        pd.enBooklet = YES;
        pd.enStaple = YES;
        pd.enFinisher23Holes = NO;
        pd.enFinisher24Holes = YES;
        pd.enTrayAutoStacking = YES;
        pd.enTrayFaceDown = YES;
        pd.enTrayStacking = YES;
        pd.enTrayTop = YES;
        pd.enLpr = YES;
        pd.enRaw = YES;
        GHTestLog(@"-- registering \"%@\"..", pd.name);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHAssertTrue([printerManager isAtMaximumPrinters], @"count(%lu) = MAX_PRINTERS(%lu)",
                  (unsigned long)printerManager.countSavedPrinters,
                  (unsigned long)MAX_PRINTERS);
}

- (void)test008_IsPrinterRegistered
{
    GHTestLog(@"# CHECK: PM can check IPs. #");
    
    NSString* oldIP = [NSString stringWithFormat:@"%@%d", VALID_PRINTER_IP, 4]; //192.168.1.4
    GHAssertTrue([printerManager isIPAlreadyRegistered:oldIP], @"IP=%@ should already be registered", oldIP);
    
    NSString* newIP = @"127.0.0.1";
    GHAssertFalse([printerManager isIPAlreadyRegistered:newIP], @"IP=%@ shouldn't be registered", newIP);
}

- (void)test009_DeleteDefaultPrinter
{
    GHTestLog(@"# CHECK: PM can delete default Printer. #");
    
    GHTestLog(@"-- setting printer[%lu] as the default", (unsigned long)DEFAULT_1);
    Printer* defaultPrinter = [printerManager getPrinterAtIndex:DEFAULT_1];
    GHAssertNotNil(defaultPrinter, @"");
    GHAssertTrue([printerManager registerDefaultPrinter:defaultPrinter], @"");
    
    GHTestLog(@"-- deleting printer[%lu]", (unsigned long)DEFAULT_1);
    NSUInteger countBeforeDelete = printerManager.countSavedPrinters;
    GHAssertTrue([printerManager deletePrinterAtIndex:DEFAULT_1], @"default printer can be deleted");
    GHAssertTrue(printerManager.countSavedPrinters == countBeforeDelete-1, @"");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there should be no more default printer");
}

- (void)test010_DeleteNonDefaultPrinters
{
    GHTestLog(@"# CHECK: PM can delete Printers. #");
    
    BOOL deleted = NO;
    while (printerManager.countSavedPrinters != 0)
    {
        GHTestLog(@"-- deleting next printer (remaining=%lu)", (unsigned long)printerManager.countSavedPrinters);
        
        Printer* printer = [printerManager getPrinterAtIndex:0];
        deleted = [printerManager deletePrinterAtIndex:0];
        if (!deleted)
        {
            GHFail(@"failed to delete printer=\"%@\"", printer.name);
            break;
        }
    }
    
    GHAssertTrue(printerManager.countSavedPrinters == 0, @"printers count should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there should be no default printer");
}

- (void)test011_SearchForOnePrinter
{
    GHTestLog(@"# CHECK: PM can handle search callbacks. #");
    
    GHTestLog(@"-- search for one printer");
    callbackSearchEndCalled = NO;
    [printerManager searchForPrinter:@"192.168.0.1"];
    
    NSString* msg = [NSString stringWithFormat:@"wait for %.2f seconds for manual search to end", SEARCH_TIMEOUT];
    [self waitForCompletion:SEARCH_TIMEOUT+1 withMessage:msg];
    
    GHTestLog(@"-- check if end callback was received");
    GHAssertTrue(callbackSearchEndCalled, @"");
}

//- (void)test012_SearchForOneThenStop
//{
//    GHTestLog(@"# CHECK: PM can stop search for one. #");
//    
//    GHTestLog(@"-- search for one printer");
//    callbackSearchEndCalled = NO;
//    [printerManager searchForPrinter:@"192.168.0.1"];
//    GHTestLog(@"-- canceling the search");
//    [printerManager stopSearching];
//    
//    [self waitForCompletion:SEARCH_TIMEOUT withMessage:
//            @"started search, then canceled, now wait some time after canceling the manual search"];
//    GHAssertFalse(callbackSearchEndCalled, @"callback should not have been called since the search was canceled");
//}

- (void)test013_SearchForAllPrinters
{
    GHTestLog(@"# CHECK: PM can handle search callbacks. #");
    
    GHTestLog(@"-- search for all printers");
    callbackSearchEndCalled = NO;
    [printerManager searchForAllPrinters];
    
    NSString* msg = [NSString stringWithFormat:@"wait for %.2f seconds for auto search to end", SEARCH_TIMEOUT];
    [self waitForCompletion:SEARCH_TIMEOUT+1 withMessage:msg];
    
    GHTestLog(@"-- check if end callback was received");
    GHAssertTrue(callbackSearchEndCalled, @"");
}

//- (void)test014_SearchForAllThenStop
//{
//    GHTestLog(@"# CHECK: PM can stop search for all. #");
//    
//    GHTestLog(@"-- search for all printers");
//    callbackSearchEndCalled = NO;
//    [printerManager searchForAllPrinters];
//    GHTestLog(@"-- canceling the search");
//    [printerManager stopSearching];
//    
//    [self waitForCompletion:SEARCH_TIMEOUT withMessage:
//            @"started search, then canceled, now wait some time after canceling the auto search"];
//    GHAssertFalse(callbackSearchEndCalled, @"callback should not have been called since the search was canceled");
//}

- (void)test015_Singleton
{
    GHTestLog(@"# CHECK: PM is indeed a singleton. #");
    
    PrinterManager* pmNew = [PrinterManager sharedPrinterManager];
    GHAssertEqualObjects(printerManager, pmNew, @"should return the same object");
}

#pragma mark - PrinterSearchDelegate Methods

- (void)searchEnded
{
    callbackSearchEndCalled = YES;
}

- (void)printerSearchDidFoundNewPrinter:(PrinterDetails*)printerDetails
{
    callbackFoundNewCalled = YES;
}

- (void)printerSearchDidFoundOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
    callbackFoundOldCalled = YES;
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Printer Manager Test"
                                                    message:msg
                                                   delegate:self
                                          cancelButtonTitle:@"HIDE"
                                          otherButtonTitles:nil];
    [alert show];
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    [alert dismissWithClickedButtonIndex:0 animated:YES];
    
    return done;
}

@end
