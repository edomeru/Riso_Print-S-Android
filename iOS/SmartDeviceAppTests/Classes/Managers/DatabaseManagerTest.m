//
//  DatabaseManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "DatabaseManager.h"
#import "Printer.h"
#import "PrintJob.h"
#import "PrintSetting.h"
#import "DefaultPrinter.h"

@interface DatabaseManagerTest : GHTestCase
{
}

@end

@implementation DatabaseManagerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
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

- (void)test001_PrinterAddNoSave
{
    GHTestLog(@"# CHECK: DBM can discard unsaved Printers. #");
    
    [self checkDBShouldBeEmpty:E_PRINTER];
    [self checkAdd:E_PRINTER usingTag:@"newPrinter1"];
    [self checkDiscard:@"newPrinter1"];
    [self checkDBShouldBeEmpty:E_PRINTER];
}

- (void)test002_PrinterAddWithoutPrintSetting
{
    GHTestLog(@"# CHECK: DBM saving an invalid Printer. #");
    
    [self checkDBShouldBeEmpty:E_PRINTER];
    [self checkAdd:E_PRINTER usingTag:@"newPrinter2"];
    [self checkSave:@"newPrinter2" expectingResult:NO errorMsg:@"save should fail, PrintSetting object is nil"];
    [self checkDiscard:@"newPrinter2"];
    [self checkDBShouldBeEmpty:E_PRINTER];
}

- (void)test003_PrinterAddWithPrintSetting
{
    GHTestLog(@"# CHECK: DBM saving a valid Printer. #");
    
    [self checkDBShouldBeEmpty:E_PRINTER];
    Printer* newPrinter3 = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter3"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    GHTestLog(@"-- attaching newPrintSetting to newPrinter3");
    newPrinter3.printsetting = newPrintSetting;
    [self checkSave:@"newPrinter3" expectingResult:YES errorMsg:@"save should be successful"];
    [self checkDBShouldNotBeEmpty:E_PRINTER expecting:1];
    [self checkDBShouldNotBeEmpty:E_PRINTSETTING expecting:1];
}

- (void)test004_PrinterRetrieveThenDelete
{
    GHTestLog(@"# CHECK: DBM retrieve and delete Printers. #");
    
    [self checkDBShouldNotBeEmpty:E_PRINTER expecting:1];
    Printer* retrievedPrinter1 = (Printer*)[[self checkGet:E_PRINTER] firstObject];
    GHAssertNotNil(retrievedPrinter1, @"retrievedPrinter1 should not be nil");
    GHAssertNotNil(retrievedPrinter1.printsetting, @"PrintSetting object should not be nil");
    
    [self checkDelete:retrievedPrinter1 usingTag:@"retrievedPrinter1"];
    [self checkDBShouldBeEmpty:E_PRINTER];
    [self checkDBShouldBeEmpty:E_PRINTSETTING];
}

- (void)test005_DefaultPrinterAddNoSave
{
    GHTestLog(@"# CHECK: DBM can discard unsaved DefaultPrinter. #");
    
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
    [self checkAdd:E_DEFAULTPRINTER usingTag:@"defaultPrinter1"];
    [self checkDiscard:@"defaultPrinter1"];
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
}

- (void)test006_DefaultPrinterAddWithoutPrinter
{
    GHTestLog(@"# CHECK: DBM saving an invalid DefaultPrinter. #");
    
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
    [self checkAdd:E_DEFAULTPRINTER usingTag:@"defaultPrinter2"];
    [self checkSave:@"defaultPrinter2" expectingResult:NO errorMsg:@"save should fail, Printer object is nil"];
    [self checkDiscard:@"defaultPrinter2"];
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
}

- (void)test007_DefaultPrinterAddWithPrinter
{
    GHTestLog(@"# CHECK: DBM saving a valid DefaultPrinter. #");
    
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
    DefaultPrinter* defaultPrinter3 = (DefaultPrinter*)[self checkAdd:E_DEFAULTPRINTER
                                                             usingTag:@"defaultPrinter3"];
    Printer* newPrinter = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    GHTestLog(@"-- attaching newPrintSetting to newPrinter");
    newPrinter.printsetting = newPrintSetting;
    GHTestLog(@"-- attaching newPrinter to defaultPrinter3");
    defaultPrinter3.printer = newPrinter;
    [self checkSave:@"defaultPrinter3" expectingResult:YES errorMsg:@"save should be successful"];
    [self checkDBShouldNotBeEmpty:E_DEFAULTPRINTER expecting:1];
    [self checkDBShouldNotBeEmpty:E_PRINTER expecting:1];
    [self checkDBShouldNotBeEmpty:E_PRINTSETTING expecting:1];
}

- (void)test008_DefaultPrinterRetrieveThenDelete
{
    GHTestLog(@"# CHECK: DBM retrieve and delete DefaultPrinter. #");
    
    [self checkDBShouldNotBeEmpty:E_DEFAULTPRINTER expecting:1];
    DefaultPrinter* retrievedDefaultPrinter = (DefaultPrinter*)[[self checkGet:E_DEFAULTPRINTER] firstObject];
    GHAssertNotNil(retrievedDefaultPrinter, @"retrievedDefaultPrinter should not be nil");
    GHAssertNotNil(retrievedDefaultPrinter.printer, @"Printer object should not be nil");
    
    [self checkDelete:retrievedDefaultPrinter usingTag:@"retrievedDefaultPrinter"];
    [self checkDBShouldBeEmpty:E_DEFAULTPRINTER];
    [self checkDBShouldNotBeEmpty:E_PRINTER expecting:1];
    [self checkDBShouldNotBeEmpty:E_PRINTSETTING expecting:1];
    
    Printer* attachedPrinter = (Printer*)[[self checkGet:E_PRINTER] firstObject];
    [self checkDelete:attachedPrinter usingTag:@"attachedPrinter"];
    [self checkDBShouldBeEmpty:E_PRINTER];
    [self checkDBShouldBeEmpty:E_PRINTSETTING];
}

#pragma mark - Utility Methods

- (NSArray*)checkDBShouldBeEmpty:(NSString*)entityName
{
    GHTestLog(@"-- check if there are zero %@ objects", entityName);
    NSArray* fetchResults = [DatabaseManager getObjects:entityName];
    GHAssertNotNil(fetchResults, @"fetch for %@ should not return nil", entityName);
    GHAssertTrue([fetchResults count] == 0, @"fetch for %@ should return an empty array", entityName);
    
    return fetchResults;
}

- (NSArray*)checkDBShouldNotBeEmpty:(NSString*)entityName expecting:(NSUInteger)expectedCount
{
    GHTestLog(@"-- check that %@ object count=%lu", entityName, (unsigned long)expectedCount);
    NSArray* fetchResults = [DatabaseManager getObjects:entityName];
    GHAssertNotNil(fetchResults, @"fetch for %@ should not return nil", entityName);
    GHAssertTrue([fetchResults count] == expectedCount, @"fetch for %@ should %lu object/s",
                 entityName, (unsigned long)expectedCount);
    
    return fetchResults;
}

- (NSArray*)checkGet:(NSString*)entityName
{
    GHTestLog(@"-- get the saved objects");
    NSArray* fetchResults = [DatabaseManager getObjects:entityName];
    GHAssertNotNil(fetchResults, @"fetch for %@ should not return nil", entityName);
    
    return fetchResults;
}

- (NSManagedObject*)checkAdd:(NSString*)entityName usingTag:(NSString*)tag
{
    GHTestLog(@"-- adding a %@ object (%@)", entityName, tag);
    NSManagedObject* addedObject = [DatabaseManager addObject:entityName];
    GHAssertNotNil(addedObject, @"%@ should not be nil", tag);
    
    return addedObject;
}

- (void)checkDelete:(NSManagedObject*)object usingTag:(NSString*)tag
{
    GHTestLog(@"-- delete (%@)", tag);
    GHAssertTrue([DatabaseManager deleteObject:object], @"delete should be successful");
}

- (void)checkSave:(NSString*)tag expectingResult:(BOOL)expectingSuccess errorMsg:(NSString*)msg
{
    GHTestLog(@"-- attempting to save (%@)", tag);
    if (expectingSuccess)
        GHAssertTrue([DatabaseManager saveChanges], msg);
    else
        GHAssertFalse([DatabaseManager saveChanges], msg);
}

- (void)checkDiscard:(NSString*)tag
{
    GHTestLog(@"-- discarding (%@)", tag);
    [DatabaseManager discardChanges];
}

@end
