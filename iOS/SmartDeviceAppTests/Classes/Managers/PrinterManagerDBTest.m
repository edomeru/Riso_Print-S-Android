//
//  PrinterManagerDBTest.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "Printer.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"

const NSUInteger NUM_PRINTERS = 5;
const NSUInteger MAX_PRINTERS = 20; //get from SmartDeviceApp-Settings.plist
const NSUInteger DEFAULT_1    = 2;
const NSUInteger DEFAULT_2    = 4;
const NSString*  PRINTER_NAME = @"UT-Printer";
const NSString*  PRINTER_IP   = @"192.168.1.";

@interface PrinterManagerDBTest : GHTestCase
{
    PrinterManager* printerManager;
}

@end

@implementation PrinterManagerDBTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    printerManager = [PrinterManager sharedPrinterManager];
    GHAssertNotNil(printerManager, nil);
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

- (void)test001_Initialization
{
    GHTestLog(@"# CHECK: PM is properly initialized. #");
    
    GHAssertEquals(printerManager.countSavedPrinters, (NSUInteger)0, @"printer count should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there should be no default printer");
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test002_AddPrinters
{
    GHTestLog(@"# CHECK: PM can add Printers. #");
    
    for (int i = 0; i < NUM_PRINTERS; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        pd.ip = [NSString stringWithFormat:@"%@%d", PRINTER_IP, i+1];
        pd.port = [NSNumber numberWithInt:(i+1)*100];
        pd.enLPR = YES;
        pd.enRAW = NO;
        pd.enBind = YES;
        pd.enBookletBind = YES;
        pd.enDuplex = YES;
        pd.enPagination = YES;
        pd.enStaple = YES;
        GHTestLog(@"-- registering \"%@\"..", pd.name);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test003_GetPrinters
{
    GHTestLog(@"# CHECK: PM can retrieve Printers. #");
    
    for (NSUInteger i = 0; i < NUM_PRINTERS; i++)
    {
        GHTestLog(@"-- getting printer i=%lu", (unsigned long)i);
        
        Printer* testPrinter = [printerManager getPrinterAtIndex:i];
        GHAssertNotNil(testPrinter, @"get printer should return a valid printer");
        
        NSString* expectedName = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        GHAssertEqualStrings(testPrinter.name, expectedName, @"");
        
        NSString* expectedIP = [NSString stringWithFormat:@"%@%d", PRINTER_IP, i+1];
        GHAssertEqualStrings(testPrinter.ip_address, expectedIP, @"");
        
        NSNumber* expectedPort = [NSNumber numberWithInt:(i+1)*100];
        GHAssertEquals([testPrinter.port intValue], [expectedPort intValue], @"");
        
        GHAssertTrue([testPrinter.enabled_lpr boolValue], @"setting is different than expected");
        GHAssertFalse([testPrinter.enabled_raw boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_bind boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_booklet_binding boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_duplex boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_pagination boolValue], @"setting is different than expected");
        GHAssertTrue([testPrinter.enabled_staple boolValue], @"setting is different than expected");
        
        GHAssertFalse([printerManager isDefaultPrinter:testPrinter], @"there should be no default printer");
        
        GHAssertNotNil(testPrinter.printsetting, @"Printer should have a valid PrintSetting object");
    }
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test004_SetUnsetDefaultPrinter
{
    GHTestLog(@"# CHECK: PM knows which is the DefaultPrinter. #");
    
    GHTestLog(@"-- setting printer[%lu] to be the default printer", (unsigned long)DEFAULT_1);
    Printer* default1 = [printerManager getPrinterAtIndex:DEFAULT_1];
    BOOL setDefault1 = [printerManager registerDefaultPrinter:default1];
    GHAssertTrue(setDefault1, @"printer at index=%lu should be the default printer", (unsigned long)DEFAULT_1);
    GHAssertTrue([printerManager hasDefaultPrinter], @"there should be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_PRINTERS, @"printer count should remain the same");
    GHAssertTrue([printerManager isDefaultPrinter:default1], @"default1 should be the default printer");
    
    GHTestLog(@"-- setting printer[%lu] to be the default printer", (unsigned long)DEFAULT_2);
    Printer* default2 = [printerManager getPrinterAtIndex:DEFAULT_2];
    BOOL setDefault2 = [printerManager registerDefaultPrinter:default2];
    GHAssertTrue(setDefault2, @"printer at index=%lu should be the default printer", (unsigned long)DEFAULT_2);
    GHAssertTrue([printerManager hasDefaultPrinter], @"there should be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_PRINTERS, @"printer count should remain the same");
    GHAssertTrue([printerManager isDefaultPrinter:default2], @"default2 should be the default printer");
    
    GHAssertFalse([printerManager isDefaultPrinter:default1], @"default1 is not anymore the default printer");
    
    GHTestLog(@"-- unsetting printer[%lu] from being the default printer", (unsigned long)DEFAULT_2);
    GHAssertTrue([printerManager deleteDefaultPrinter], @"default printer should be removed");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there shouldn't be a default printer");
    GHAssertEquals(printerManager.countSavedPrinters, NUM_PRINTERS, @"printer count should remain the same");
    GHAssertFalse([printerManager isDefaultPrinter:default1], @"default1 is not anymore the default printer");
    GHAssertFalse([printerManager isDefaultPrinter:default2], @"default2 is not anymore the default printer");
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test005_MaximumPrinters
{
    GHTestLog(@"# CHECK: PM can check for max printers. #");
    
    GHAssertFalse([printerManager isAtMaximumPrinters], @"count(%lu) < MAX_PRINTERS(%lu)",
                  (unsigned long)printerManager.countSavedPrinters,
                  (unsigned long)MAX_PRINTERS);
    
    // add until we reach maximum printers
    for (NSUInteger i = NUM_PRINTERS; i < MAX_PRINTERS; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        pd.ip = [NSString stringWithFormat:@"%@%d", PRINTER_IP, i+1];
        pd.port = [NSNumber numberWithInt:(i+1)*100];
        pd.enLPR = YES;
        pd.enRAW = NO;
        pd.enBind = YES;
        pd.enBookletBind = YES;
        pd.enDuplex = YES;
        pd.enPagination = YES;
        pd.enStaple = YES;
        GHTestLog(@"-- registering \"%@\"..", pd.name);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHAssertTrue([printerManager isAtMaximumPrinters], @"count(%lu) = MAX_PRINTERS(%lu)",
                  (unsigned long)printerManager.countSavedPrinters,
                  (unsigned long)MAX_PRINTERS);
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test006_DuplicatePrinter
{
    GHTestLog(@"# CHECK: PM can check IPs. #");
    
    NSString* oldIP = [NSString stringWithFormat:@"%@%d", PRINTER_IP, 4]; //192.168.1.4
    GHAssertTrue([printerManager isIPAlreadyRegistered:oldIP], @"IP=%@ should already be registered", oldIP);
    
    NSString* newIP = @"127.0.0.1";
    GHAssertFalse([printerManager isIPAlreadyRegistered:newIP], @"IP=%@ shouldn't be registered", newIP);
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

- (void)test007_DeletePrinters
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
    
    GHAssertEquals(printerManager.countSavedPrinters, (NSUInteger)0, @"printers count should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
    GHAssertFalse([printerManager hasDefaultPrinter], @"there should be no default printer");
    
    GHTestLog(@"# CHECK: END. NO ISSUES. #");
}

@end
