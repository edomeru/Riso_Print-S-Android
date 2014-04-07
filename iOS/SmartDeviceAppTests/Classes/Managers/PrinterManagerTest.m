//
//  PrinterManagerTests.m
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
const NSString*  PRINTER_NAME = @"UT-Printer";
const NSString*  PRINTER_IP   = @"192.168.1.";

@interface PrinterManagerTest : GHTestCase
{
    PrinterManager* printerManager;
}

@end

@implementation PrinterManagerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    // The CoreData stack and the NSManagedObjectContext should be
    // created and set on the main thread.
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
    GHAssertEquals(printerManager.countSavedPrinters, (NSUInteger)0, @"printers in DB should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
}

- (void)test002_AddPrinters
{
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
        GHTestLog(@"registering \"%@\"..", pd.name);
        GHAssertTrue([printerManager registerPrinter:pd], @"failed to add printer=\"%@\"", pd.name);
    }
    
    GHAssertEquals(printerManager.countSavedPrinters, NUM_PRINTERS,
                   @"printers in DB should be %lu", (unsigned long)NUM_PRINTERS);
}

- (void)test003_GetPrinters
{
    for (NSUInteger i = 0; i < NUM_PRINTERS; i++)
    {
        GHTestLog(@"getting printer i=%lu", (unsigned long)i);
        
        Printer* testPrinter = [printerManager getPrinterAtIndex:i];
        GHAssertNotNil(testPrinter, @"get printer should return a valid printer");
        
        NSString* expectedName = [NSString stringWithFormat:@"%@%d", PRINTER_NAME, i+1];
        GHAssertEqualStrings(testPrinter.name, expectedName, @"");
        
        NSString* expectedIP = [NSString stringWithFormat:@"%@%d", PRINTER_IP, i+1];
        GHAssertEqualStrings(testPrinter.ip_address, expectedIP, @"");
    }
}

- (void)test004_DeletePrinters
{
    BOOL deleted = NO;
    while (printerManager.countSavedPrinters != 0)
    {
        Printer* printer = [printerManager getPrinterAtIndex:0];
        deleted = [printerManager deletePrinterAtIndex:0];
        if (!deleted)
        {
            GHFail(@"failed to delete printer=\"%@\"", printer.name);
            break;
        }
    }
    
    GHAssertEquals(printerManager.countSavedPrinters, (NSUInteger)0, @"printers n DB should be 0");
    GHAssertNil([printerManager getPrinterAtIndex:0], @"get printer should return nil");
}

@end
