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
    
    [self checkGet:E_PRINTER expecting:0];
    [self checkAdd:E_PRINTER usingTag:@"newPrinter1"];
    [self checkDiscard:@"newPrinter1"];
    [self checkGet:E_PRINTER expecting:0];
}

- (void)test002_PrinterAddWithoutPrintSetting
{
    GHTestLog(@"# CHECK: DBM saving an invalid Printer. #");
    
    [self checkGet:E_PRINTER expecting:0];
    [self checkAdd:E_PRINTER usingTag:@"newPrinter2"];
    [self checkSave:@"newPrinter2" expectingSuccess:NO errorMsg:@"save should fail, PrintSetting object is nil"];
    [self checkDiscard:@"newPrinter2"];
    [self checkGet:E_PRINTER expecting:0];
}

- (void)test003_PrinterAddWithPrintSetting
{
    GHTestLog(@"# CHECK: DBM saving a valid Printer. #");
    
    [self checkGet:E_PRINTER expecting:0];
    Printer* newPrinter3 = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter3"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    GHTestLog(@"-- attaching newPrintSetting to newPrinter3");
    newPrinter3.printsetting = newPrintSetting;
    [self checkSave:@"newPrinter3" expectingSuccess:YES errorMsg:@"save should be successful"];
    [self checkGet:E_PRINTER expecting:1];
    [self checkGet:E_PRINTSETTING expecting:1];
}

- (void)test004_PrinterDelete
{
    GHTestLog(@"# CHECK: DBM can delete a Printer. #");
    
    NSArray* results = [self checkGet:E_PRINTER expecting:1];
    Printer* retrievedPrinter1 = (Printer*)[results firstObject];
    GHAssertNotNil(retrievedPrinter1, @"retrievedPrinter1 should not be nil");
    GHAssertNotNil(retrievedPrinter1.printsetting, @"PrintSetting object should not be nil");
    
    [self checkDelete:retrievedPrinter1 usingTag:@"retrievedPrinter1"];
    [self checkGet:E_PRINTER expecting:0];
    [self checkGet:E_PRINTSETTING expecting:0];
}

- (void)test005_DefaultPrinterAddNoSave
{
    GHTestLog(@"# CHECK: DBM can discard unsaved DefaultPrinter. #");
    
    [self checkGet:E_DEFAULTPRINTER expecting:0];
    [self checkAdd:E_DEFAULTPRINTER usingTag:@"defaultPrinter1"];
    [self checkDiscard:@"defaultPrinter1"];
    [self checkGet:E_DEFAULTPRINTER expecting:0];
}

- (void)test006_DefaultPrinterAddWithoutPrinter
{
    GHTestLog(@"# CHECK: DBM saving an invalid DefaultPrinter. #");
    
    [self checkGet:E_DEFAULTPRINTER expecting:0];
    [self checkAdd:E_DEFAULTPRINTER usingTag:@"defaultPrinter2"];
    [self checkSave:@"defaultPrinter2" expectingSuccess:NO errorMsg:@"save should fail, Printer object is nil"];
    [self checkDiscard:@"defaultPrinter2"];
    [self checkGet:E_DEFAULTPRINTER expecting:0];
}

- (void)test007_DefaultPrinterAddWithPrinter
{
    GHTestLog(@"# CHECK: DBM saving a valid DefaultPrinter. #");
    
    [self checkGet:E_DEFAULTPRINTER expecting:0];
    DefaultPrinter* defaultPrinter3 = (DefaultPrinter*)[self checkAdd:E_DEFAULTPRINTER
                                                             usingTag:@"defaultPrinter3"];
    Printer* newPrinter = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    GHTestLog(@"-- attaching newPrintSetting to newPrinter");
    newPrinter.printsetting = newPrintSetting;
    GHTestLog(@"-- attaching newPrinter to defaultPrinter3");
    defaultPrinter3.printer = newPrinter;
    [self checkSave:@"defaultPrinter3" expectingSuccess:YES errorMsg:@"save should be successful"];
    [self checkGet:E_DEFAULTPRINTER expecting:1];
    [self checkGet:E_PRINTER expecting:1];
    [self checkGet:E_PRINTSETTING expecting:1];
}

- (void)test008_DefaultPrinterDelete
{
    GHTestLog(@"# CHECK: DBM can delete a DefaultPrinter. #");
    
    NSArray* results1 = [self checkGet:E_DEFAULTPRINTER expecting:1];
    DefaultPrinter* retrievedDefaultPrinter = (DefaultPrinter*)[results1 firstObject];
    GHAssertNotNil(retrievedDefaultPrinter, @"retrievedDefaultPrinter should not be nil");
    GHAssertNotNil(retrievedDefaultPrinter.printer, @"Printer object should not be nil");
    
    [self checkDelete:retrievedDefaultPrinter usingTag:@"retrievedDefaultPrinter"];
    [self checkGet:E_DEFAULTPRINTER expecting:0];
    [self checkGet:E_PRINTER expecting:1];
    [self checkGet:E_PRINTSETTING expecting:1];
    
    NSArray* results2 = [self checkGet:E_PRINTER expecting:1];
    Printer* attachedPrinter = (Printer*)[results2 firstObject];
    [self checkDelete:attachedPrinter usingTag:@"attachedPrinter"];
    [self checkGet:E_PRINTER expecting:0];
    [self checkGet:E_PRINTSETTING expecting:0];
}

- (void)test009_PrintJobAddNoSave
{
    GHTestLog(@"# CHECK: DBM can discard unsaved PrintJobs. #");
    
    [self checkGet:E_PRINTJOB expecting:0];
    [self checkAdd:E_PRINTJOB usingTag:@"newPrintJob1"];
    [self checkDiscard:@"newPrintJob1"];
    [self checkGet:E_PRINTER expecting:0];
}

- (void)test010_PrintJobAddWithoutPrinter
{
    GHTestLog(@"# CHECK: DBM saving an invalid PrintJob. #");
    
    [self checkGet:E_PRINTJOB expecting:0];
    [self checkAdd:E_PRINTJOB usingTag:@"newPrintJob2"];
    [self checkSave:@"newPrintJob2" expectingSuccess:NO errorMsg:@"save should fail, Printer object is nil"];
    [self checkDiscard:@"newPrintJob2"];
    [self checkGet:E_PRINTJOB expecting:0];
}

- (void)test011_PrintJobAddWithPrinter
{
    GHTestLog(@"# CHECK: DBM saving a valid PrintJob. #");
    
    [self checkGet:E_PRINTJOB expecting:0];
    PrintJob* newPrintJob3 = (PrintJob*)[self checkAdd:E_PRINTJOB usingTag:@"newPrintJob3"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    Printer* newPrinter = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter"];
    GHTestLog(@"-- attaching newPrinter to newPrintJob3");
    newPrintJob3.printer = newPrinter;
    newPrinter.printsetting = newPrintSetting;
    [self checkSave:@"newPrintJob3" expectingSuccess:YES errorMsg:@"save should be successful"];
    [self checkGet:E_PRINTJOB expecting:1];
    [self checkGet:E_PRINTER expecting:1];
}

- (void)test012_PrintJobDeleteJob
{
    GHTestLog(@"# CHECK: DBM can delete PrintJobs. #");
    
    NSArray* results1 = [self checkGet:E_PRINTJOB expecting:1];
    PrintJob* retrievedPrintJob1 = (PrintJob*)[results1 firstObject];
    GHAssertNotNil(retrievedPrintJob1, @"retrievedPrintJob1 should not be nil");
    GHAssertNotNil(retrievedPrintJob1.printer, @"Printer object should not be nil");
    
    [self checkDelete:retrievedPrintJob1 usingTag:@"retrievedPrintJob1"];
    [self checkGet:E_PRINTJOB expecting:0];
    [self checkGet:E_PRINTER expecting:1]; //the parent Printer is not deleted
    
    NSArray* results2 = [self checkGet:E_PRINTER expecting:1];
    Printer* attachedPrinter = (Printer*)[results2 firstObject];
    [self checkDelete:attachedPrinter usingTag:@"attachedPrinter"];
    [self checkGet:E_PRINTER expecting:0];
}

- (void)test013_PrintJobDeletePrinter
{
    GHTestLog(@"# CHECK: DBM can delete PrintJobs. #");
    
    [self checkGet:E_PRINTJOB expecting:0];
    PrintJob* newPrintJob4 = (PrintJob*)[self checkAdd:E_PRINTJOB usingTag:@"newPrintJob4"];
    PrintSetting* newPrintSetting = (PrintSetting*)[self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    Printer* newPrinter = (Printer*)[self checkAdd:E_PRINTER usingTag:@"newPrinter"];
    GHTestLog(@"-- attaching newPrinter to newPrintJob4");
    newPrintJob4.printer = newPrinter;
    newPrinter.printsetting = newPrintSetting;
    [self checkSave:@"newPrintJob4" expectingSuccess:YES errorMsg:@"save should be successful"];
    [self checkGet:E_PRINTJOB expecting:1];
    [self checkGet:E_PRINTER expecting:1];
    
    [self checkDelete:newPrinter usingTag:@"newPrinter"];
    [self checkGet:E_PRINTER expecting:0];
    [self checkGet:E_PRINTJOB expecting:0]; //the attached PrintJob is also deleted
}

- (void)test014_PrintSettingAddNoSave
{
    GHTestLog(@"# CHECK: DBM can discard unsaved PrintSettings. #");
    
    [self checkGet:E_PRINTSETTING expecting:0];
    [self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    [self checkDiscard:@"newPrintSetting"];
    [self checkGet:E_PRINTSETTING expecting:0];
}

- (void)test015_PrintSettingAddWithSave
{
    GHTestLog(@"# CHECK: DBM saving PrintSetting. #");
    
    [self checkGet:E_PRINTSETTING expecting:0];
    [self checkAdd:E_PRINTSETTING usingTag:@"newPrintSetting"];
    [self checkSave:@"newPrintSetting" expectingSuccess:YES errorMsg:@"save should be successful"];
    [self checkGet:E_PRINTSETTING expecting:1];
}

- (void)test016_PrintSettingGetThenDelete
{
    GHTestLog(@"# CHECK: DBM can delete a PrintSetting. #");
    
    NSArray* results = [self checkGet:E_PRINTSETTING expecting:1];
    PrintSetting* retrievedPrintSetting = (PrintSetting*)[results firstObject];
    GHAssertNotNil(retrievedPrintSetting, @"retrievedPrintSetting should not be nil");
    
    [self checkDelete:retrievedPrintSetting usingTag:@"retrievedPrintSetting"];
    [self checkGet:E_PRINTSETTING expecting:0];
}

#pragma mark - Utility Methods

- (NSArray*)checkGet:(NSString*)entityName expecting:(NSUInteger)expectedCount
{
    GHTestLog(@"-- check that there are %lu %@ objects", (unsigned long)expectedCount, entityName);
    NSArray* fetchResults = [DatabaseManager getObjects:entityName];
    GHAssertNotNil(fetchResults, @"fetch for %@ should not return nil", entityName);
    GHAssertTrue([fetchResults count] == expectedCount, @"there should be %lu %@ object/s",
                 (unsigned long)expectedCount, entityName);
    
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

- (void)checkSave:(NSString*)tag expectingSuccess:(BOOL)expectingSuccess errorMsg:(NSString*)msg
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
