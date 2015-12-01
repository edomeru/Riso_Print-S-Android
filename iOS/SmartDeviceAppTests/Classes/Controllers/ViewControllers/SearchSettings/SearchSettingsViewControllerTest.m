//
//  SearchSettingsViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "SearchSettingsViewController.h"
#import "UIViewController+Segue.h"
#import "AppSettingsHelper.h"


@interface SearchSettingsViewController (UnitTest)
// expose private properties
- (UITextField*)snmpCommunityName;

//exposed private methods
- (void)dismissKeypad;
- (void)dismissScreen;
- (IBAction)onBack:(UIButton*)sender;
@end

@interface SearchSettingsViewControllerTest : GHTestCase
{
    UIStoryboard* storyboard;
    SearchSettingsViewController* controllerIphone;
    SearchSettingsViewController* controllerIpad;
    NSString *testCommunityName;
    id mockAppSettingsHelper;
}

@end

@implementation SearchSettingsViewControllerTest
#pragma mark - Setup/TearDown Methods

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

// Run at start of all tests in the class
- (void)setUpClass
{
    testCommunityName = @"public";
    
    mockAppSettingsHelper = OCMClassMock([AppSettingsHelper class]);
    [[[mockAppSettingsHelper stub] andCall:@selector(mockGetSNMPCommunityName) onObject:self] getSNMPCommunityName];
    [[[mockAppSettingsHelper stub] andCall:@selector(mockSaveSNMPCommunityName:) onObject:self] saveSNMPCommunityName:OCMOCK_ANY];
    
    NSString* storyboardTitle = @"Main";
    storyboard = [UIStoryboard storyboardWithName:storyboardTitle bundle:nil];
    GHAssertNotNil(storyboard, @"unable to retrieve storyboard file %@", storyboardTitle);
    
    NSString* controllerIphoneName = @"SearchSettingsIphoneViewController";
    controllerIphone = [storyboard instantiateViewControllerWithIdentifier:controllerIphoneName];
    GHAssertNotNil(controllerIphone, @"unable to instantiate controller (%@)", controllerIphoneName);
    GHAssertNotNil(controllerIphone.view, @"");
    
    NSString* controllerIpadName = @"SearchSettingsIpadViewController";
    controllerIpad = [storyboard instantiateViewControllerWithIdentifier:controllerIpadName];
    GHAssertNotNil(controllerIpad, @"unable to instantiate controller (%@)", controllerIpadName);
    GHAssertNotNil(controllerIpad.view, @"");
}

// Run at end of all tests in the class
- (void)tearDownClass
{
    [mockAppSettingsHelper stopMocking];
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
    
    GHAssertNotNil([controllerIphone snmpCommunityName], @"");
    GHAssertEqualObjects(controllerIphone, [[controllerIphone snmpCommunityName] delegate], @"");
    GHAssertEqualStrings([[controllerIphone snmpCommunityName] text] , testCommunityName, @"");
    
    GHAssertNotNil([controllerIpad snmpCommunityName], @"");
    GHAssertEqualObjects(controllerIphone, [[controllerIphone snmpCommunityName] delegate], @"");
    GHAssertEqualStrings([[controllerIphone snmpCommunityName] text] , testCommunityName, @"");
}

- (void)test002_TextFieldActions_Validation
{
    //paste string with mix valid and invalid characters
    NSString *testString = @"abcd$123+x";
    NSRange testRange;
    GHAssertFalse([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                            replacementString:testString], @"");
    
    //paste all valid character but can exceed total
    controllerIphone.snmpCommunityName.text = @"abcdefghijklmnopqrstuvwxyz";
    testString = @"1234567890";
    GHAssertFalse([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                            replacementString:testString], @"");

    
    //type Valid character but already full
    controllerIphone.snmpCommunityName.text = @"abcdefghijklmnopqrstuvwxyz123456";
    testString = @"/";
    GHAssertFalse([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                            replacementString:testString], @"");
    
    //Backspace when full
    testString = @"";
    GHAssertTrue([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                           replacementString:testString], @"");
    
    //clear text field
    controllerIphone.snmpCommunityName.text = @"";
    
    //test all valid characters
    //valid symbols
    testString = @",./:;@[\\]^_";
    GHAssertTrue([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                           replacementString:testString], @"");
    
    //lowercase alphabets
    testString = @"abcdefghijklmnopqrstuvwxyz";
    GHAssertTrue([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                           replacementString:testString], @"");
    
    //uppercase alphabets
    testString = @"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    GHAssertTrue([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                           replacementString:testString], @"");
    
    //numeroc characters
    testString = @"1234567890";
    GHAssertTrue([controllerIphone textField:controllerIphone.snmpCommunityName shouldChangeCharactersInRange:testRange
                           replacementString:testString], @"");
    
}

- (void)test003_TextFieldActions_Saving
{
    testCommunityName = @"testName";
    controllerIphone.snmpCommunityName.text = @"";
    [controllerIphone textFieldDidEndEditing:controllerIphone.snmpCommunityName];
    GHAssertNotEqualStrings(testCommunityName, @"", @"");
    GHAssertEqualStrings(testCommunityName, @"testName", @"");
    
    NSString *testValue = @"testValue";
    controllerIphone.snmpCommunityName.text = testValue;
    [controllerIphone textFieldDidEndEditing:controllerIphone.snmpCommunityName];
    
    GHAssertEqualStrings(testCommunityName, testValue, @"");
}

- (void)test004_TextFieldShouldReturn_BlankTextField
{
    UITextField *originalTextField = controllerIphone.snmpCommunityName;
    id mockTextField = OCMClassMock([UITextField class]);
    [[[mockTextField stub] andReturnValue:OCMOCK_VALUE(NO)] isEditing];
    //set expectation
    [[mockTextField expect] resignFirstResponder];
    [controllerIphone setValue:mockTextField forKey:@"snmpCommunityName"];
    
    NSString *testValue = @"";
    testCommunityName = @"testName";
    
    [[[mockTextField stub] andReturn:testValue] text];
    [controllerIphone textFieldShouldReturn:mockTextField];
    [mockTextField verify];
    GHAssertNotEqualStrings(testCommunityName, testValue, @"");
    
    //clean-up
    [controllerIphone setValue:originalTextField forKey:@"snmpCommunityName"];
    [mockTextField stopMocking];
}


- (void)test005_TextFieldShouldReturn
{
    UITextField *originalTextField = controllerIphone.snmpCommunityName;
    id mockTextField = OCMClassMock([UITextField class]);
    [[[mockTextField stub] andReturnValue:OCMOCK_VALUE(NO)] isEditing];
    //set expectation
    [[mockTextField expect] resignFirstResponder];
    [controllerIphone setValue:mockTextField forKey:@"snmpCommunityName"];
    
    NSString *testValue = @"testValue";
    testCommunityName = @"testName";
    
    [[[mockTextField stub] andReturn:testValue] text];
    [controllerIphone textFieldShouldReturn:mockTextField];
    
    //verify expectation
    [mockTextField verify];
    GHAssertEqualStrings(testCommunityName, testValue, @"");
    
    //clean-up
    [controllerIphone setValue:originalTextField forKey:@"snmpCommunityName"];
    [mockTextField stopMocking];
}

- (void)test006_DismissKeyPad_Editing
{
    UITextField *originalTextField = controllerIphone.snmpCommunityName;
    id mockTextField = OCMClassMock([UITextField class]);
    [[[mockTextField stub] andReturnValue:OCMOCK_VALUE(YES)] isEditing];
    //set expectation
    [[mockTextField expect] resignFirstResponder];
    [controllerIphone setValue:mockTextField forKey:@"snmpCommunityName"];
    
    [controllerIphone performSelector:@selector(dismissKeypad) withObject:nil];
    
    //verify expectation
    [mockTextField verify];
    
    //clean-up
    [controllerIphone setValue:originalTextField forKey:@"snmpCommunityName"];
    [mockTextField stopMocking];
}

- (void)test007_DismissKeyPad_NotEditing
{
    UITextField *originalTextField = controllerIphone.snmpCommunityName;
    id mockTextField = OCMClassMock([UITextField class]);
    [[[mockTextField stub] andReturnValue:OCMOCK_VALUE(NO)] isEditing];
    //set expectation
    [[mockTextField reject] resignFirstResponder];
    [controllerIphone setValue:mockTextField forKey:@"snmpCommunityName"];
    
    [controllerIphone performSelector:@selector(dismissKeypad) withObject:nil];
    
    //verify expectation
    [mockTextField verify];
    
    //clean-up
    [controllerIphone setValue:originalTextField forKey:@"snmpCommunityName"];
    [mockTextField stopMocking];
}

- (void)test008_onBack
{
    id mockController = OCMPartialMock(controllerIphone);

    //set expectation
    [[mockController expect] dismissScreen];
    
    [mockController onBack:nil];
    
    //verify expectation
    [mockController verify];
    
    //clean-up
    [mockController stopMocking];
}

- (void)test009_dismissScreen_iPad
{
    id mockController = OCMPartialMock(controllerIphone);
    [mockController setValue:[NSNumber numberWithBool:YES] forKey:@"isIpad"];
    
    //set expectation
    [[mockController expect] close];
    [[mockController reject] unwindFromOverTo:OCMOCK_ANY];
    
    [mockController dismissScreen];
    
    //verify expectation
    [mockController verify];
    
    //clean-up
    [mockController stopMocking];
}

- (void)test010_dismissScreen_NotIPad
{
    id mockController = OCMPartialMock(controllerIphone);
    [mockController setValue:[NSNumber numberWithBool:NO] forKey:@"isIpad"];
    
    //set expectation
    [[mockController reject] close];
    [[mockController expect] unwindFromOverTo:OCMOCK_ANY];
    
    [mockController dismissScreen];
    
    //verify expectation
    [mockController verify];
    
    //clean-up
    [mockController stopMocking];
}


#pragma mark - Mock Methods
     
- (NSString *)mockGetSNMPCommunityName
{
    return testCommunityName;
}
     
-(void)mockSaveSNMPCommunityName:(NSString *)communityName
{
    testCommunityName = communityName;
}

@end


