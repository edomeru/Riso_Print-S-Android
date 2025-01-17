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
#import "AppSettingsHelper.h"
#import "OCMock.h"

@interface AddPrinterViewController (UnitTest)

// expose private properties
- (UITextField*)textIP;
- (UIButton*)saveButton;
- (UIActivityIndicatorView*)progressIndicator;
- (UILabel*)communityNameDisplay;
- (NSLayoutConstraint *)viewTopConstraint;
- (UIView *)inputView;

// expose private methods
- (void)addFullCapabilityPrinter:(NSString *)ipAddress;
- (IBAction)onSave:(UIButton*)sender;
- (void)moveViewUpWithOffset:(CGFloat)offset;
- (void)moveViewDownToNormal;
- (void)keyboardDidShow:(NSNotification *)notif;
- (void)keyboardDidHide:(NSNotification *)notif;
- (void)stopSearch;
- (void)deviceLockEventDidNotify;

@end

@interface AddPrinterViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    AddPrinterViewController* controllerIphone;
    AddPrinterViewController* controllerIpad;
    NSString *testCommunityName;
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
    testCommunityName = @"testname";
    id mockAppSettingsHelper = OCMClassMock([AppSettingsHelper class]);
    [[[mockAppSettingsHelper stub] andReturn:testCommunityName] getSNMPCommunityName];

    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerIphoneName = @"AddPrinterIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    GHAssertFalse(controllerIphone.hasAddedPrinters, @"");
    
    NSString* controllerIpadName = @"AddPrinterIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
    GHAssertFalse(controllerIpad.hasAddedPrinters, @"");
    
    [controllerIpad setValue:@(YES) forKey:@"isIpad"];
    
    [mockAppSettingsHelper stopMocking];
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
    GHAssertNotNil([controllerIphone communityNameDisplay], @"");
    GHAssertEqualStrings([[controllerIphone communityNameDisplay] text] , testCommunityName, @"");
    GHAssertNotNil([controllerIphone viewTopConstraint], @"");
    GHAssertNil(controllerIphone.printersViewController, @"will only be non-nil on segue from Printers");
    
    GHAssertNotNil([controllerIpad textIP], @"");
    GHAssertNotNil([controllerIpad saveButton], @"");
    GHAssertNotNil([controllerIpad progressIndicator], @"");
    GHAssertNotNil([controllerIpad communityNameDisplay], @"");
    GHAssertEqualStrings([[controllerIphone communityNameDisplay] text] , testCommunityName, @"");
    GHAssertNil([controllerIpad viewTopConstraint], @""); //constraint only binded for iphone
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
    //GHAssertTrue(willAcceptBackspace, @"");
    willAcceptBackspace = [controllerIphone textField:textFieldIphone
                        shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                    replacementString:@""];
    //GHAssertTrue(willAcceptBackspace, @"");
    textFieldIpad.text = @"aa";
    willAcceptBackspace = [controllerIphone textField:textFieldIpad
                        shouldChangeCharactersInRange:NSMakeRange(1, 1)
                                    replacementString:@""];
    //GHAssertTrue(willAcceptBackspace, @"");
    willAcceptBackspace = [controllerIphone textField:textFieldIpad
                        shouldChangeCharactersInRange:NSMakeRange(0, 1)
                                    replacementString:@""];
    //GHAssertTrue(willAcceptBackspace, @"");
    
    GHTestLog(@"-- checking clear and return");
    //GHAssertTrue([controllerIphone textFieldShouldClear:textFieldIphone], @"");
    //GHAssertTrue([controllerIpad textFieldShouldClear:textFieldIpad], @"");
    GHAssertTrue([controllerIphone textFieldShouldReturn:textFieldIphone], @"");
    GHAssertTrue([controllerIpad textFieldShouldReturn:textFieldIpad], @"");
}

- (void)test004_SaveOnlinePrinter
{
    GHTestLog(@"# CHECK: Save Online Printer. #");

   /*NSString* printerIP = @"192.168.0.1";
    UITextField* inputText = [controllerIphone textIP];
    Swizzler* swizzler = [[Swizzler alloc] init];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- adding an online printer");
   inputText.text = printerIP;
    [swizzler swizzleInstanceMethod:[PrinterManager class] targetSelector:@selector(searchForPrinter:) swizzleClass:[PrinterManagerMock class] swizzleSelector:@selector(searchForPrinterSuccessful:)];
    [controllerIphone onSave:[controllerIphone saveButton]];
    [swizzler deswizzle];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
    [self removeResultAlert];
    GHAssertTrue(pm.countSavedPrinters == 1, @"");*/
}

- (void)test005_SaveOfflinePrinter
{
    GHTestLog(@"# CHECK: Save Offline Printer. #");
/*
    NSString* printerIP = @"192.168.0.1";
    UITextField* inputText = [controllerIphone textIP];
    Swizzler* swizzler = [[Swizzler alloc] init];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- adding an offline printer");
    inputText.text = printerIP;
    [swizzler swizzleInstanceMethod:[PrinterManager class] targetSelector:@selector(searchForPrinter:) swizzleClass:[PrinterManagerMock class] swizzleSelector:@selector(searchForPrinterFail:)];
    [controllerIphone onSave:[controllerIphone saveButton]];
    [swizzler deswizzle];
    [self waitForCompletion:5 withMessage:nil]; //delay, gives time for the callbacks to process
    [self removeResultAlert];
    GHAssertTrue(pm.countSavedPrinters == 1, @"");*/
}

- (void)test006_SaveButAlreadySaved
{
    GHTestLog(@"# CHECK: Save Already Saved Printer. #");
    NSString* printerIP = @"192.168.0.1";
    UITextField* inputText = [controllerIphone textIP];
    
    GHTestLog(@"-- registering one printer");
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    PrinterDetails* pd = [[PrinterDetails alloc] init];
    pd.name = @"RISO Printer";
    pd.ip = printerIP;
    GHAssertTrue([pm registerPrinter:pd], @"");
    
    GHTestLog(@"-- save the same printer");
    inputText.text = printerIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self waitForCompletion:2 withMessage:nil]; //to see the alert
    [self removeResultAlert];
    GHAssertTrue(pm.countSavedPrinters == 1, @"");
}

- (void)test007_SaveButInvalidIP
{
    GHTestLog(@"# CHECK: Save Invalid IP. #");
    NSString* invalidPrinterIP = @"999.999.999.999";
    UITextField* inputText = [controllerIphone textIP];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
    
    GHTestLog(@"-- save invalid IP1");
    inputText.text = invalidPrinterIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self waitForCompletion:2 withMessage:nil]; //to see the alert
    [self removeResultAlert];
    GHAssertTrue(pm.countSavedPrinters == 0, @"");
}

- (void)test008_SaveButMaximum
{
    GHTestLog(@"# CHECK: Save Already Saved Printer. #");
    NSString* printerIP = @"192.168.0.";
    NSUInteger printerMax = 10;
    UITextField* inputText = [controllerIphone textIP];
    
    GHTestLog(@"-- maxing-out the printers list");
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    for (NSUInteger i = pm.countSavedPrinters; i < printerMax; i++)
    {
        PrinterDetails* pd = [[PrinterDetails alloc] init];
        pd.name = @"RISO Printer";
        pd.ip = [NSString stringWithFormat:@"%@%lu", printerIP, (unsigned long)i];
        GHAssertTrue([pm registerPrinter:pd], @"");
    }
    
    GHTestLog(@"-- saving another printer");
    inputText.text = printerIP;
    [controllerIphone onSave:[controllerIphone saveButton]];
    [self waitForCompletion:2 withMessage:nil]; //to see the alert
    [self removeResultAlert];
    GHAssertTrue(pm.countSavedPrinters == printerMax, @"");
}

- (void)test009_AddFullCapabilityPrinter
{
    GHTestLog(@"# CHECK: Adding a Full-Capability Printer. #");
    NSString* invalidIP = @"192.168.0.1";

    PrinterManager* pm = [PrinterManager sharedPrinterManager];
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
    GHAssertTrue([fullCapPrinter.enabled_booklet_finishing boolValue], @"");
    GHAssertFalse([fullCapPrinter.enabled_finisher_2_3_holes boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_finisher_2_4_holes boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_staple boolValue], @"");
    //GHAssertTrue([fullCapPrinter.enabled_tray_auto_stacking boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_face_down boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_stacking boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_tray_top boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_lpr boolValue], @"");
    GHAssertTrue([fullCapPrinter.enabled_raw boolValue], @"");
    //GHAssertTrue([fullCapPrinter.onlineStatus boolValue], @"");
    //GHAssertNil(fullCapPrinter.defaultprinter, @"");
    GHAssertNotNil(fullCapPrinter.printjob, @"");
    GHAssertTrue([fullCapPrinter.printjob count] == 0, @"");
    GHAssertNotNil(fullCapPrinter.printsetting, @"");
    fullCapPrinter = nil;
    
    // clear out the other printers
    while (pm.countSavedPrinters != 0)
        GHAssertTrue([pm deletePrinterAtIndex:0], @"");
}

- (void)test010_moveView
{
    CGFloat testOffset = 5.0f;
    [controllerIphone moveViewUpWithOffset:testOffset];
    
    //top view constraint is negative of textIP height
    CGFloat diff = controllerIphone.viewTopConstraint.constant + testOffset;
    GHAssertTrue(fabs(diff) < 0.0001, @"");
    
    [controllerIphone moveViewDownToNormal];
    GHAssertTrue(fabs(controllerIphone.viewTopConstraint.constant) < 0.0001, @"");
    
    [controllerIphone moveViewDownToNormal];
    GHAssertTrue(fabs(controllerIphone.viewTopConstraint.constant) < 0.0001, @"");
}

- (void)test11_viewWillAppear
{
    id mockNSNotificationCenter = OCMClassMock([NSNotificationCenter class]);
    [[[[mockNSNotificationCenter stub] andReturn:mockNSNotificationCenter] classMethod] defaultCenter];
    [[mockNSNotificationCenter expect] removeObserver:controllerIphone];
    
    [controllerIphone viewWillDisappear:NO];
    
    [mockNSNotificationCenter verify];
    [mockNSNotificationCenter stopMocking];
}

- (void)test12_KeyboardDidShow_willMoveUp
{
    CGRect testRect = CGRectMake(0, 200, 200 , 200);
    NSDictionary *testUserInfo = [NSDictionary dictionaryWithObject:[NSValue valueWithCGRect:testRect] forKey:UIKeyboardFrameEndUserInfoKey];
    id mockNSNotification = OCMClassMock([NSNotification class]);
    [[[mockNSNotification stub] andReturn:testUserInfo] userInfo];
    id mockInputView = OCMClassMock([UIView class]);
    [[[mockInputView stub] andReturnValue:OCMOCK_VALUE(testRect)] convertRect:testRect fromView:nil];
    UIView *originalInputView = [controllerIphone inputView];
    [controllerIphone setValue:mockInputView forKey:@"inputView"];
    
    CGRect originalTextFrame = controllerIphone.textIP.frame;
    controllerIphone.textIP.frame = CGRectMake(0, 170, 100, 50);
    
    CGFloat testOffset = controllerIphone.textIP.frame.size.height + controllerIphone.textIP.frame.origin.y - testRect.origin.y + 8.0f;
    
    [controllerIphone keyboardDidShow:mockNSNotification];
    
    //top view constraint is negative of textIP height
    CGFloat diff = controllerIphone.viewTopConstraint.constant + testOffset;
    GHAssertTrueNoThrow(fabs(diff) < 0.0001, @"");
    
    [controllerIphone setValue:originalInputView forKey:@"inputView"];
    controllerIphone.viewTopConstraint.constant = 0;
    controllerIphone.textIP.frame = originalTextFrame;
    [mockInputView stopMocking];
    [mockNSNotification stopMocking];
}

- (void)test13_KeyboardDidShow_willNotMoveUp
{
    CGRect testRect = CGRectMake(0, 200, 200 , 200);
    NSDictionary *testUserInfo = [NSDictionary dictionaryWithObject:[NSValue valueWithCGRect:testRect] forKey:UIKeyboardFrameEndUserInfoKey];
    id mockNSNotification = OCMClassMock([NSNotification class]);
    [[[mockNSNotification stub] andReturn:testUserInfo] userInfo];
    id mockInputView = OCMClassMock([UIView class]);
    [[[mockInputView stub] andReturnValue:OCMOCK_VALUE(testRect)] convertRect:testRect fromView:nil];
    UIView *originalInputView = [controllerIphone inputView];
    [controllerIphone setValue:mockInputView forKey:@"inputView"];
    
    CGRect originalTextFrame = controllerIphone.textIP.frame;
    controllerIphone.textIP.frame = CGRectMake(0, 120, 100, 50);
    
    [controllerIphone keyboardDidShow:mockNSNotification];
    
    //no movement
    GHAssertTrueNoThrow(fabs(controllerIphone.viewTopConstraint.constant) < 0.0001, @"");
    
    [controllerIphone setValue:originalInputView forKey:@"inputView"];
    controllerIphone.viewTopConstraint.constant = 0;
    controllerIphone.textIP.frame = originalTextFrame;
    [mockInputView stopMocking];
    [mockNSNotification stopMocking];
}

- (void)test14_KeyboardDidShow_iPad
{
    id mockNSNotification = OCMClassMock([NSNotification class]);
    [[mockNSNotification reject] userInfo];
    
    [controllerIpad keyboardDidShow:mockNSNotification];
    
    [mockNSNotification verify];
    [mockNSNotification stopMocking];
}

- (void)test15_KeyboardDidHide
{
    controllerIphone.viewTopConstraint.constant = 30;
    
    [controllerIphone keyboardDidHide:nil];
    
    GHAssertTrueNoThrow(fabs(controllerIphone.viewTopConstraint.constant) < 0.0001, @"");
}

- (void)test16_KeyboardDidHide_iPad
{
    id mockController = OCMPartialMock(controllerIpad);
    [[mockController reject] moveViewDownToNormal];
    
    [mockController keyboardDidHide:nil];
    
    [mockController verify];
    
    [mockController stopMocking];
}

- (void)test17_stopSearchStopSessions
{
    id mockPrinterManager = OCMClassMock([PrinterManager class]);
    [[mockPrinterManager expect] stopSearching:YES];
    PrinterManager *originalManager = [controllerIphone valueForKey:@"printerManager"];

    [controllerIphone setValue:mockPrinterManager forKey:@"printerManager"];
    
    [controllerIphone stopSearch];
    
    [mockPrinterManager verify];
    [mockPrinterManager stopMocking];
    
    [controllerIphone setValue:originalManager forKey:@"printerManager"];
}

- (void)test18_testDeviceLockEventDidNotify
{
    [controllerIphone.saveButton setHidden:YES];
    [controllerIphone.textIP setEnabled:NO];
    
    [controllerIphone deviceLockEventDidNotify];
    
    GHAssertTrueNoThrow(controllerIphone.saveButton.hidden, @"");
    GHAssertFalseNoThrow([controllerIphone.textIP isEnabled], @"");
    
    [controllerIphone.progressIndicator startAnimating];

    GHAssertTrueNoThrow([controllerIphone.progressIndicator isAnimating], @"");
    
    [controllerIphone deviceLockEventDidNotify];
    
    GHAssertFalseNoThrow([controllerIphone.progressIndicator isAnimating], @"");
    GHAssertFalseNoThrow(controllerIphone.saveButton.hidden, @"");
    GHAssertTrueNoThrow([controllerIphone.textIP isEnabled], @"");
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
