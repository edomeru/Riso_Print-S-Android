//
//  PrinterManagerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "DatabaseManager.h"
#import "PrintSetting.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "PrinterManager.h"
#import "NotificationNames.h"
#import "PrinterDetails.h"
#import "SNMPManager.h"

@interface PrinterManagerTest : GHAsyncTestCase<PrinterSearchDelegate>

@property (nonatomic, strong) PrinterDetails *testPrinterDetails;
@property (nonatomic, strong) PrintSetting *testPrintSetting;
@property (nonatomic, strong) Printer *testPrinter;
@property (nonatomic, strong) DefaultPrinter *testDefaultPrinter;
@property (nonatomic, strong) NSMutableArray *testFoundPrinters;
@property (nonatomic, assign) BOOL didFoundOldPrinter;

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
    [MagicalRecord setDefaultModelFromClass:[self class]];
    [MagicalRecord setupCoreDataStackWithInMemoryStore];
    
    self.testPrinterDetails = [[PrinterDetails alloc] init];
    self.testPrintSetting = [PrintSetting MR_createEntity];
    self.testPrinter = [Printer MR_createEntity];
    self.testDefaultPrinter = [DefaultPrinter MR_createEntity];
    
    self.testFoundPrinters = [[NSMutableArray alloc] init];
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [MagicalRecord cleanUp];
}

// Run before each test method
- (void)setUp
{
    // Default details
    self.testPrinterDetails.name = @"Default Printer Name";
    self.testPrinterDetails.ip = @"192.168.1.1";
    self.testPrinterDetails.port = [NSNumber numberWithInt:0];
    self.testPrinterDetails.enBookletFinishing = YES;
    self.testPrinterDetails.enFinisher23Holes = YES;
    self.testPrinterDetails.enFinisher24Holes = NO;
    self.testPrinterDetails.enLpr = YES;
    self.testPrinterDetails.enRaw = NO;
    self.testPrinterDetails.enStaple = YES;
    self.testPrinterDetails.enTrayAutoStacking = YES;
    self.testPrinterDetails.enTrayFaceDown = YES;
    self.testPrinterDetails.enTrayStacking = YES;
    self.testPrinterDetails.enTrayTop = YES;
    
    self.testPrintSetting.colorMode = [NSNumber numberWithInt:0];
    self.testPrintSetting.orientation = [NSNumber numberWithInt:0];
    self.testPrintSetting.copies = [NSNumber numberWithInt:1];
    self.testPrintSetting.duplex = [NSNumber numberWithInt:0];
    self.testPrintSetting.paperSize = [NSNumber numberWithInt:2];
    self.testPrintSetting.scaleToFit = [NSNumber numberWithBool:YES];
    self.testPrintSetting.paperType = [NSNumber numberWithInt:0];
    self.testPrintSetting.inputTray = [NSNumber numberWithInt:0];
    self.testPrintSetting.imposition = [NSNumber numberWithInt:0];
    self.testPrintSetting.impositionOrder = [NSNumber numberWithInt:0];
    self.testPrintSetting.sort = [NSNumber numberWithInt:0];
    self.testPrintSetting.booklet = [NSNumber numberWithBool:NO];
    self.testPrintSetting.bookletFinish = [NSNumber numberWithInt:0];
    self.testPrintSetting.bookletLayout = [NSNumber numberWithInt:0];
    self.testPrintSetting.finishingSide = [NSNumber numberWithInt:0];
    self.testPrintSetting.staple = [NSNumber numberWithInt:0];
    self.testPrintSetting.punch = [NSNumber numberWithInt:0];
    self.testPrintSetting.outputTray = [NSNumber numberWithInt:0];
    
    self.testDefaultPrinter.printer = nil;
    
    self.didFoundOldPrinter = NO;
    
    [self prepare];
}

// Run after each test method
- (void)tearDown
{
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    while (sharedPrinterManager.countSavedPrinters != 0)
    {
        [sharedPrinterManager deletePrinterAtIndex:0];
    }
    
    sharedPrinterManager.searchDelegate = nil;
    
    [self.testFoundPrinters removeAllObjects];
}

#pragma mark - PrinterSearchDelegate

- (void)printerSearchEndedwithResult:(BOOL)printerFound
{
    if (printerFound == YES)
    {
        [self notify:kGHUnitWaitStatusSuccess];
    }
    else
    {
        [self notify:kGHUnitWaitStatusFailure];
     }
}

- (void)printerSearchDidFoundNewPrinter:(PrinterDetails *)printerDetails
{
    [self.testFoundPrinters addObject:printerDetails];
}

- (void)printerSearchDidFoundOldPrinter:(NSString *)printerIP withName:(NSString *)printerName
{
    self.didFoundOldPrinter = YES;
}

#pragma mark - Test Cases

- (void)testSharedManager
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    
    // Verification
    GHAssertNotNil(sharedPrinterManager, @"[PrinterManager sharedPrinterManager] should not be nil");
}

- (void)testRegisterPrinter_CannotCreateDefaultPrintSettings
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:nil] addObject:E_PRINTSETTING];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    
    // Verification
    GHAssertEquals(result, NO, @"Register printers should fail.");
}

- (void)testRegisterPrinter_CannotCreatePrinterObject
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:nil] addObject:E_PRINTER];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    
    // Verification
    GHAssertEquals(result, NO, @"Register printers should fail.");
}

- (void)testRegisterPrinter_CannotSavePrinterObject
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    
    // Verification
    GHAssertEquals(result, NO, @"Register printers should fail.");
}

- (void)testRegisterPrinter_OK
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    Printer *printer = [sharedPrinterManager getPrinterAtIndex:0];
    
    // Verification
    GHAssertEquals(result, YES, @"Register printers should be successful.");
    GHAssertEqualStrings(printer.name, self.testPrinter.name, @"Printer.name should match.");
}

- (void)testRegisterDefaultPrinter_CannotCreateDefaultPrinter
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturn:nil] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    Printer *printer = [sharedPrinterManager getPrinterAtIndex:0];
    BOOL result = [sharedPrinterManager registerDefaultPrinter:printer];
    
    // Verification
    GHAssertEquals(result, NO, @"Register default printer should fail.");
}

- (void)testRegisterDefaultPrinter_CannotSaveDefaultPrinter
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    
    // Verification
    GHAssertEquals(result, NO, @"Register default printer should fail.");
}

- (void)testRegisterDefaultPrinter_OK
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    Printer *printer = [sharedPrinterManager getDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, YES, @"Register default printer should fail.");
    GHAssertEquals(printer, self.testPrinter, @"Printer should match.");
}

- (void)testGetPrinterAtIndex_IndexOverflow
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    Printer* printer = [sharedPrinterManager getPrinterAtIndex:1];
    
    // Verification
    GHAssertNil(printer, @"Printer should be nil.");
}

/*- (void)testDeleteDefaultPrinter_CannotDeleteDefaultPrinter
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager deleteDefaultPrinter];
    Printer *defaultPrinter = [sharedPrinterManager getDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, NO, @"Delete default printer should fail.");
    GHAssertNil(defaultPrinter, @"Default printer should be nil.");
}

- (void)testDeleteDefaultPrinter_NoDefaultPrinter
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager deleteDefaultPrinter];
    Printer *defaultPrinter = [sharedPrinterManager getDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, YES, @"Delete default printer should succeed.");
    GHAssertNil(defaultPrinter, @"Default printer should be nil.");
}

- (void)testDeleteDefaultPrinter_OK
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager deleteDefaultPrinter];
    Printer *defaultPrinter = [sharedPrinterManager getDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, YES, @"Delete default printer should succeed.");
    GHAssertNil(defaultPrinter, @"Default printer should be nil.");
}*/

- (void)testDeletePrinterAtIndex_IndexOverflow
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager deletePrinterAtIndex:1];
    
    // Verification
    GHAssertEquals(result, NO, @"Delete printer should fail.");
}

- (void)testDeletePrinterAtIndex_DefaultPrinter_NG
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager deletePrinterAtIndex:0];
    
    // Verification
    GHAssertEquals(result, NO, @"Delete printer should fail.");
}

- (void)testDeletePrinterAtIndex_DefaultPrinter_OK
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager deletePrinterAtIndex:0];
    
    // Verification
    GHAssertEquals(result, YES, @"Delete printer should succeeed.");
}

- (void)testDeletePrinterAtIndex_NG
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] deleteObject:self.testPrinter];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    BOOL result = [sharedPrinterManager deletePrinterAtIndex:0];
    
    // Verification
    GHAssertEquals(result, NO, @"Delete printer should fail.");
}

- (void)testDeletePrinterAtIndex_OK
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] deleteObject:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    BOOL result = [sharedPrinterManager deletePrinterAtIndex:0];
    
    // Verification
    GHAssertEquals(result, YES, @"Delete printer should succeed.");
}

- (void)testHasDefaultPrinter_NO
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager hasDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, NO, @"Result must be NO.");
}

- (void)testHasDefaultPrinter_YES
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager hasDefaultPrinter];
    
    // Verification
    GHAssertEquals(result, YES, @"Result must be YES.");
}

- (void)testIsDefaultPrinter_NoDefault
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager isDefaultPrinter:self.testPrinter];
    
    // Verification
    GHAssertEquals(result, NO, @"Result must be NO.");
}

- (void)testIsDefaultPrinter_IsDefault
{
    // Mock
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturn:self.testPrintSetting] addObject:E_PRINTSETTING];
    [[[mockDatabaseManager stub] andReturn:self.testPrinter] addObject:E_PRINTER];
    [[[mockDatabaseManager stub] andReturn:self.testDefaultPrinter] addObject:E_DEFAULTPRINTER];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    [sharedPrinterManager registerDefaultPrinter:self.testPrinter];
    BOOL result = [sharedPrinterManager isDefaultPrinter:self.testPrinter];
    
    // Verification
    GHAssertEquals(result, YES, @"Result must be YES.");
}

- (void)testPrinterChanges_OK
{
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(YES)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager savePrinterChanges];
    
    // Verification
    GHAssertEquals(result, YES, @"Result must be YES.");
}

- (void)testPrinterChanges_NG
{
    id mockDatabaseManager = [OCMockObject mockForClass:[DatabaseManager class]];
    [[[mockDatabaseManager stub] andReturnValue:OCMOCK_VALUE(NO)] saveChanges];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    BOOL result = [sharedPrinterManager savePrinterChanges];
    
    // Verification
    GHAssertEquals(result, NO, @"Result must be NO.");
}

- (void)testPrinterSearchForPrinter_NotFound
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[[mockSNMPManager stub] andDo:^(NSInvocation *invocation) {
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END object:mockSNMPManager userInfo:@{@"result": [NSNumber numberWithBool:NO]}];
    }] searchForPrinter:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager searchForPrinter:@"192.168.1.1"];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusFailure timeout:15.0f];
    [mockSNMPManager stopMocking];
}

- (void)testPrinterSearchForPrinter_Found
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[[mockSNMPManager stub] andDo:^(NSInvocation *invocation) {
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD object:mockSNMPManager userInfo:@{@"printerDetails": self.testPrinterDetails}];
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END object:mockSNMPManager userInfo:@{@"result": [NSNumber numberWithBool:YES]}];
    }] searchForPrinter:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager searchForPrinter:@"192.168.1.1"];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:15.0f];
    [mockSNMPManager stopMocking];
}

- (void)testPrinterSearchForPrinter_FoundAlreadyRegistered
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[[mockSNMPManager stub] andDo:^(NSInvocation *invocation) {
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD object:mockSNMPManager userInfo:@{@"printerDetails": self.testPrinterDetails}];
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END object:mockSNMPManager userInfo:@{@"result": [NSNumber numberWithBool:YES]}];
    }] searchForPrinter:OCMOCK_ANY];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager searchForPrinter:@"192.168.1.1"];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:15.0f];
    GHAssertEquals(self.didFoundOldPrinter, YES, @"Found printer must be already registered.");
    [mockSNMPManager stopMocking];
}

- (void)testPrinterSearchForAllPrinters_NotFound
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[[mockSNMPManager stub] andDo:^(NSInvocation *invocation) {
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END object:mockSNMPManager userInfo:@{@"result": [NSNumber numberWithBool:NO]}];
    }] searchForAvailablePrinters];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager searchForAllPrinters];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusFailure timeout:15.0f];
    [mockSNMPManager stopMocking];
}

- (void)testPrinterSearchForAllPrinters_Found
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[[mockSNMPManager stub] andDo:^(NSInvocation *invocation) {
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_ADD object:mockSNMPManager userInfo:@{@"printerDetails": self.testPrinterDetails}];
        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIF_SNMP_END object:mockSNMPManager userInfo:@{@"result": [NSNumber numberWithBool:YES]}];
    }] searchForAvailablePrinters];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager searchForAllPrinters];
    
    // Verification
    [self waitForStatus:kGHUnitWaitStatusSuccess timeout:15.0f];
    [mockSNMPManager stopMocking];
}

- (void)testStopSearching
{
    // Mock
    id mockSNMPManager = [OCMockObject partialMockForObject:[SNMPManager sharedSNMPManager]];
    [[mockSNMPManager expect] cancelSearch];
    
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    sharedPrinterManager.searchDelegate = self;
    [sharedPrinterManager stopSearching];
    
    // Verification
    GHAssertNoThrow([mockSNMPManager verify], @"");
    [mockSNMPManager stopMocking];
}

- (void)testIsAtMaximumPrinters_YES
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    NSString *ipFormat = @"192.168.1.%d";
    for (int i = 0; i < 10; i++)
    {
        self.testPrinterDetails.ip = [NSString stringWithFormat:ipFormat, i+1];
        [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    }
    
    // Verification
    BOOL result = [sharedPrinterManager isAtMaximumPrinters];
    GHAssertEquals(result, YES, @"Result should be YES");
}

- (void)testIsAtMaximumPrinters_NO
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    NSString *ipFormat = @"192.168.1.%d";
    for (int i = 0; i < 9; i++)
    {
        self.testPrinterDetails.ip = [NSString stringWithFormat:ipFormat, i+1];
        [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    }
    
    // Verification
    BOOL result = [sharedPrinterManager isAtMaximumPrinters];
    GHAssertEquals(result, NO, @"Result should be NO");
}

- (void)testIsIPAlreadyRegistered_YES
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    
    // Verification
    BOOL result = [sharedPrinterManager isIPAlreadyRegistered:self.testPrinterDetails.ip];
    GHAssertEquals(result, YES, @"Result should be YES");
}
                   
- (void)testIsIPAlreadyRegistered_NO
{
    // SUT
    PrinterManager *sharedPrinterManager = [PrinterManager sharedPrinterManager];
    [sharedPrinterManager registerPrinter:self.testPrinterDetails];
    
    // Verification
    BOOL result = [sharedPrinterManager isIPAlreadyRegistered:@"192.168.1.2"];
    GHAssertEquals(result, NO, @"Result should be NO");
}

@end
