//
//  AddPrinterViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "AddPrinterViewController.h"
#import "Printer.h"
#import "CXAlertView.h"
#import "PrinterDetails.h"
#import "Swizzler.h"
#import "SNMPManager.h"
#import "SNMPManagerMock.h"

@interface AddPrinterViewController (UnitTest)

// expose private properties
- (UITextField*)textIP;
- (UIButton*)saveButton;
- (UIActivityIndicatorView*)progressIndicator;

// expose private methods
- (void)addFullCapabilityPrinter:(NSString *)ipAddress;
- (IBAction)onSave:(UIButton*)sender;

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
    GHAssertFalse(controllerIphone.hasAddedPrinters, @"");
    
    NSString* controllerIpadName = @"AddPrinterIphoneViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
    GHAssertFalse(controllerIpad.hasAddedPrinters, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    storyboard = nil;
    controllerIphone = nil;
    controllerIpad = nil;
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
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
    GHAssertNil(controllerIphone.printersViewController, @"will only be non-nil on segue from Printers");
    
    GHAssertNotNil([controllerIpad textIP], @"");
    GHAssertNotNil([controllerIpad saveButton], @"");
    GHAssertNotNil([controllerIpad progressIndicator], @"");
    GHAssertNil(controllerIpad.printersViewController, @"will only be non-nil on segue from Printers");
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

- (void)test003_TextFieldActions
{
    GHTestLog(@"# CHECK: IP TextField does not accept spaces. #");
    
    UITextField* textFieldIphone = [controllerIphone textIP];
    UITextField* textFieldIpad = [controllerIpad textIP];
    BOOL willAcceptSpace = YES;
    BOOL willAcceptBackspace = YES;
    
    GHTestLog(@"-- entering a space (iPhone)");
    willAcceptSpace = [controllerIphone textField:textFieldIphone
                    shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");

    GHTestLog(@"-- entering a space (iPad)");
    willAcceptSpace = [controllerIpad textField:textFieldIpad
                  shouldChangeCharactersInRange:NSMakeRange(0, 1)
                              replacementString:@" "];
    GHAssertFalse(willAcceptSpace, @"");
    
    GHTestLog(@"-- checking backspace");
    textFieldIphone.text = @"aa";
    willAcceptBackspace = [controllerIphone textField:textFieldIphone
                        shouldChangeCharactersInRange:NSMakeRange(1, 1)
                                    replacementString:@""];
    GHAssertTrue(willAcceptBackspace, @"");
    willAcceptBackspace = [controllerIphone textField:textFieldIphone
                        shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                    replacementString:@""];
    GHAssertTrue(willAcceptBackspace, @"");
    textFieldIpad.text = @"aa";
    willAcceptBackspace = [controllerIphone textField:textFieldIpad
                        shouldChangeCharactersInRange:NSMakeRange(1, 1)
                                    replacementString:@""];
    GHAssertTrue(willAcceptBackspace, @"");
    willAcceptBackspace = [controllerIphone textField:textFieldIpad
                        shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                    replacementString:@""];
    GHAssertTrue(willAcceptBackspace, @"");
    
    GHTestLog(@"-- checking clear and return");
    GHAssertTrue([controllerIphone textFieldShouldClear:textFieldIphone], @"");
    GHAssertTrue([controllerIpad textFieldShouldClear:textFieldIpad], @"");
    GHAssertTrue([controllerIphone textFieldShouldReturn:textFieldIphone], @"");
    GHAssertTrue([controllerIpad textFieldShouldReturn:textFieldIpad], @"");
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
    GHAssertTrue(controllerIphone.hasAddedPrinters, @"");
    
    GHTestLog(@"-- checking capabilities=[%@]", invalidIP);
    Printer* fullCapPrinter = [pm getPrinterAtIndex:0];
    GHAssertNotNil(fullCapPrinter, @"");
    GHAssertNil(fullCapPrinter.name, @"");
    GHAssertEqualStrings(fullCapPrinter.ip_address, invalidIP, @"");
    GHAssertTrue([fullCapPrinter.port intValue] == 0, @"");
    GHAssertTrue([fullCapPrinter.enabled_booklet boolValue], @"");
    GHAssertFalse([fullCapPrinter.enabled_finisher_2_3_holes boolValue], @"");
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

- (void)test005_SaveOnlinePrinter
{
    GHTestLog(@"# CHECK: Save Online Printer. #");
    NSString* onlinePrinterIP = @"192.168.0.199";
    UITextField* inputText = [controllerIphone textIP];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- adding an online printer");
    inputText.text = onlinePrinterIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self waitForCompletion:10 withMessage:nil];
    [self removeResultAlert];
    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == 1, @"");
}

//- (void)test006_SaveOfflinePrinter
//{
//    GHTestLog(@"# CHECK: Save Offline Printer. #");
//    NSString* offlinePrinterIP = @"192.168.0.1";
//    UITextField* inputText = [controllerIphone textIP];
//    Swizzler* swizzler = [[Swizzler alloc] init];
//    
//    PrinterManager* pm = [PrinterManager sharedPrinterManager];
//    while (pm.countSavedPrinters != 0)
//        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
//    
//    GHTestLog(@"-- adding an offline printer");
//    inputText.text = offlinePrinterIP;
//    [swizzler swizzleInstanceMethod:[SNMPManager class] targetSelector:@selector(searchForPrinter:) swizzleClass:[SNMPManagerMock class] swizzleSelector:@selector(searchForPrinterFail:)];
//    [controllerIphone onSave:[controllerIphone saveButton]];
//    [swizzler deswizzle];
//    [controllerIphone searchEndedwithResult:NO];
//    [self removeResultAlert];
//    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == 1, @"");
//}

- (void)test007_SaveButAlreadySaved
{
    GHTestLog(@"# CHECK: Save Already Saved Printer. #");
    NSString* onlinePrinterIP = @"192.168.0.1";
    UITextField* inputText = [controllerIphone textIP];
    
    GHTestLog(@"-- registering one printer");
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.name = @"RISO Printer";
    pd.ip = onlinePrinterIP;
    GHAssertTrue([pm registerPrinter:pd], @"");
    
    GHTestLog(@"-- save the same printer");
    inputText.text = onlinePrinterIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self removeResultAlert];
    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == 1, @"");
}

- (void)test008_SaveButInvalidIP
{
    GHTestLog(@"# CHECK: Save Invalid IP. #");
    NSString* invalidPrinterIP1 = @"999.999.999.999";
    NSString* invalidPrinterIP2 = @"00000192.168.0.1";
    UITextField* inputText = [controllerIphone textIP];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- save invalid IP1");
    inputText.text = invalidPrinterIP1;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self removeResultAlert];
    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == 0, @"");
    
    GHTestLog(@"-- save invalid IP2");
    inputText.text = invalidPrinterIP2;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self removeResultAlert];
    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == 0, @"");
}

- (void)test009_SaveButMaximum
{
    GHTestLog(@"# CHECK: Save Already Saved Printer. #");
    NSString* printerIP = @"192.168.0.1";
    NSUInteger printerMax = 10;
    UITextField* inputText = [controllerIphone textIP];
    
    GHTestLog(@"-- registering maximum printers");
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != printerMax)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = @"RISO Printer";
        pd.ip = printerIP;
        GHAssertTrue([pm registerPrinter:pd], @"");
    }
    
    GHTestLog(@"-- save another printer");
    inputText.text = printerIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self removeResultAlert];
    GHAssertTrue([[PrinterManager sharedPrinterManager] countSavedPrinters] == printerMax, @"");
}

#pragma mark - Utilities

- (BOOL)waitForCompletion:(NSTimeInterval)timeoutSecs withMessage:(NSString*)msg
{
    NSDate* timeoutDate = [NSDate dateWithTimeIntervalSinceNow:timeoutSecs];
    UIAlertView* alert;
    
    if (msg != nil)
    {
        alert = [[UIAlertView alloc] initWithTitle:@"Add Printer Test"
                                           message:msg
                                          delegate:self
                                 cancelButtonTitle:@"HIDE"
                                 otherButtonTitles:nil];
        [alert show];
    }
    
    BOOL done = NO;
    do
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:timeoutDate];
        if ([timeoutDate timeIntervalSinceNow] < 0.0)
            break;
    } while (!done);
    
    if (msg != nil)
        [alert dismissWithClickedButtonIndex:0 animated:YES];
    
    return done;
}

- (void)removeResultAlert
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
                [self waitForCompletion:2 withMessage:nil];
                alert = nil;
            }
        }
    }
}

@end
