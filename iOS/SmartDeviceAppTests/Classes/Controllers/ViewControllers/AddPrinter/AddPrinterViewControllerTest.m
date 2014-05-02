//
//  AddPrinterViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AddPrinterViewController.h"
#import "Printer.h"

@interface AddPrinterViewController (UnitTest)

// expose private properties
- (UITextField*)textIP;
- (UIButton*)saveButton;
- (UIActivityIndicatorView*)progressIndicator;

// expose private methods
- (void)addFullCapabilityPrinter:(NSString *)ipAddress;

@end

@interface AddPrinterViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    AddPrinterViewController* controllerIphone;
    AddPrinterViewController* controllerIpad;
}

@end

@implementation AddPrinterViewControllerTest

#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerIphoneName = @"AddPrinterIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    
    NSString* controllerIpadName = @"AddPrinterIphoneViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
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

- (void)test001_IBOutletsBinding
{
    GHTestLog(@"# CHECK: IBOutlets Binding. #");
    
    GHAssertNotNil([controllerIphone textIP], @"");
    GHAssertNotNil([controllerIphone saveButton], @"");
    GHAssertNotNil([controllerIphone progressIndicator], @"");
    
    GHAssertNotNil([controllerIpad textIP], @"");
    GHAssertNotNil([controllerIpad saveButton], @"");
    GHAssertNotNil([controllerIpad progressIndicator], @"");
}

- (void)test002_IBActionsBinding
{
    GHTestLog(@"# CHECK: IBActions Binding. #");
    
    NSArray* ibActions;
    
    UIButton* saveButtonIphone = [controllerIphone saveButton];
    ibActions = [saveButtonIphone actionsForTarget:controllerIphone
                                   forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"onSave:"], @"");

    UIButton* saveButtonIpad = [controllerIpad saveButton];
    ibActions = [saveButtonIpad actionsForTarget:controllerIpad
                                 forControlEvent:UIControlEventTouchUpInside];
    GHAssertNotNil(ibActions, @"");
    GHAssertTrue([ibActions count] == 1, @"");
    GHAssertTrue([ibActions containsObject:@"onSave:"], @"");
}

- (void)test003_TextFieldInput
{
    GHTestLog(@"# CHECK: IP TextField does not accept spaces. #");
    
    BOOL willAcceptSpace = YES;
    
    GHTestLog(@"-- entering a space (iPhone)");
    willAcceptSpace = [controllerIphone textField:[controllerIphone textIP]
                    shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");

    GHTestLog(@"-- entering a space (iPad)");
    willAcceptSpace = [controllerIpad textField:[controllerIpad textIP]
                  shouldChangeCharactersInRange:NSMakeRange(0, 1)
                              replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");
}

- (void)test004_AddFullCapabilityPrinter
{
    GHTestLog(@"# CHECK: Adding a Full-Capability Printer. #");
    NSString* invalidIP = @"192.168.0.1";
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    // clear out the other printers
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- adding invalid printer=[%@]", invalidIP);
    [controllerIphone addFullCapabilityPrinter:invalidIP];
    GHAssertTrue(pm.countSavedPrinters == 1, @"1 printer should be added");
    
    GHTestLog(@"-- checking capabilities=[%@]", invalidIP);
    Printer* fullCapPrinter = [pm getPrinterAtIndex:0];
    GHAssertNotNil(fullCapPrinter, @"");
    GHAssertNil(fullCapPrinter.name, @"");
    GHAssertEqualStrings(fullCapPrinter.ip_address, invalidIP, @"");
    GHAssertTrue([fullCapPrinter.port intValue] == 0, @"");
    GHAssertTrue([fullCapPrinter.enabled_booklet boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_finisher_2_3_holes boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_finisher_2_4_holes boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_staple boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_auto_stacking boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_face_down boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_stacking boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_top boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_lpr boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_raw boolValue], @"");
    GHAssertTrue([fullCapPrinter.onlineStatus boolValue], @"");
    GHAssertNil(fullCapPrinter.defaultprinter, @"");
    GHAssertNotNil(fullCapPrinter.printjob, @"");
    GHAssertTrue([fullCapPrinter.printjob count] == 0, @"");
    GHAssertNotNil(fullCapPrinter.printsetting, @"");
    
    // clear out the other printers
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    fullCapPrinter = nil;
}

@end
