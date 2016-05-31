//
//  PreviewSettingTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "AppSettings.h"

@interface PreviewSettingTest : GHTestCase

@property (nonatomic, strong) NSArray *keys;

@end

@implementation PreviewSettingTest

- (void)setUpClass
{
    self.keys = @[
                  @"colorMode",
                  @"orientation",
                  @"copies",
                  @"duplex",
                  @"paperSize",
                  @"scaleToFit",
                  @"paperType",
                  @"inputTray",
                  @"imposition",
                  @"impositionOrder",
                  @"sort",
                  @"booklet",
                  @"bookletFinish",
                  @"bookletLayout",
                  @"finishingSide",
                  @"staple",
                  @"punch",
                  @"outputTray"
                  ];
}

- (void)testProperties
{
    // SUT + Verification
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSMutableArray *keys = [NSMutableArray arrayWithArray:self.keys];
    [keys addObject:@"securePrint"];
    [keys addObject:@"pinCode"];
    for (NSString *key in keys)
    {
        BOOL responds = [previewSetting respondsToSelector:NSSelectorFromString(key)];
        GHAssertEquals(responds, YES, @"Preview setting should respond to: %@", key);
        NSNumber *newValue = [NSNumber numberWithInt:1];
        [previewSetting setValue:newValue forKey:key];
        NSNumber *currentValue = [previewSetting valueForKey:key];
        GHAssertEqualObjects(currentValue, newValue, @"Print document should have a property: %@", key);
    }
}

- (void)testFormattedString
{
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSString *formattedString = [previewSetting formattedString];
    
    // Verification
    GHAssertNotNil(formattedString, @"formattedString must not be nil.");
    for (NSString *key in self.keys)
    {
        NSInteger value = [[previewSetting valueForKey:key] integerValue];
        NSString *field = [NSString stringWithFormat:@"%@=%ld\n", key, (long)value];
        NSRange range = [formattedString rangeOfString:field];
        GHAssertTrue(range.location != NSNotFound, @"Formatted string should have %@.", field);
    }
}

- (void)testFormattedString_SecurePrint_NotEmpty
{
    // Mock
    id mockNSUserDefaults = [OCMockObject partialMockForObject:[NSUserDefaults standardUserDefaults]];
    [[[mockNSUserDefaults stub] andReturn:@"user"] valueForKey:KEY_APPSETTINGS_LOGIN_ID];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.securePrint = YES;
    previewSetting.pinCode = @"1234";
    NSString *formattedString = [previewSetting formattedString];
    
    [mockNSUserDefaults stopMocking];
    
    // Verification
    GHAssertNotNil(formattedString, @"formattedString must not be nil.");
    NSString *loginIdField = @"loginId=user\n";
    NSString *pinCodeField = @"pinCode=1234\n";
    NSString *securePrint = @"securePrint=1";
    NSRange rangeLogin = [formattedString rangeOfString:loginIdField];
    GHAssertTrue(rangeLogin.location != NSNotFound, @"Formatted string should have login ID.");
    NSRange rangePinCode = [formattedString rangeOfString:pinCodeField];
    GHAssertTrue(rangePinCode.location != NSNotFound, @"Formatted string should have pin code.");
    NSRange rangeSecurePrint = [formattedString rangeOfString:securePrint];
    GHAssertTrue(rangeSecurePrint.location != NSNotFound, @"Formatted string should have secure print.");
}

- (void)testFormattedString_SecurePrint_Empty
{
    // Mock
    id mockNSUserDefaults = [OCMockObject partialMockForObject:[NSUserDefaults standardUserDefaults]];
    [[[mockNSUserDefaults stub] andReturn:nil] valueForKey:KEY_APPSETTINGS_LOGIN_ID];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.securePrint = YES;
    previewSetting.pinCode = nil;
    NSString *formattedString = [previewSetting formattedString];
    
    [mockNSUserDefaults stopMocking];
    
    // Verification
    GHAssertNotNil(formattedString, @"formattedString must not be nil.");
    NSString *loginIdField = @"loginId=\n";
    NSString *pinCodeField = @"pinCode=\n";
    NSString *securePrint = @"securePrint=1";
    NSRange rangeLogin = [formattedString rangeOfString:loginIdField];
    GHAssertTrue(rangeLogin.location != NSNotFound, @"Formatted string should have login ID.");
    NSRange rangePinCode = [formattedString rangeOfString:pinCodeField];
    GHAssertTrue(rangePinCode.location != NSNotFound, @"Formatted string should have pin code.");
    NSRange rangeSecurePrint = [formattedString rangeOfString:securePrint];
    GHAssertTrue(rangeSecurePrint.location != NSNotFound, @"Formatted string should have secure print.");
}

- (void)testFormattedString_PrinterSettingsNil
{
    // Mock
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper expect] andReturn:nil] sharedPrintSettingsTree];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSString *formattedString = [previewSetting formattedString];
    
    [mockPrintSettingsHelper stopMocking];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    NSString *loginIdField = @"loginId=\n";
    NSString *pinCodeField = @"pinCode=\n";
    NSString *securePrint = @"securePrint=0\n";
    NSRange rangeLogin = [formattedString rangeOfString:loginIdField];
    GHAssertTrue(rangeLogin.location != NSNotFound, @"Formatted string should have login ID.");
    NSRange rangePinCode = [formattedString rangeOfString:pinCodeField];
    GHAssertTrue(rangePinCode.location != NSNotFound, @"Formatted string should have pin code.");
    NSRange rangeSecurePrint = [formattedString rangeOfString:securePrint];
    GHAssertTrue(rangeSecurePrint.location != NSNotFound, @"Formatted string should have secure print.");
}

- (void)testFormattedString_InvalidKey
{
    // Mock
    NSDictionary *invalidKey = @{@"name": @"Invalid Key", @"type": @"numeric", @"defaultValue": @"0"};
    NSArray *settings = @[invalidKey];
    NSDictionary *group = @{@"setting": settings};
    NSArray *groups = @[group];
    NSDictionary *settingsTree = @{@"group": groups};
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper stub] andReturn:settingsTree] sharedPrintSettingsTree];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSString *formattedString = [previewSetting formattedString];
    
    [mockPrintSettingsHelper stopMocking];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    NSString *loginIdField = @"loginId=\n";
    NSString *pinCodeField = @"pinCode=\n";
    NSString *securePrint = @"securePrint=0\n";
    NSRange rangeLogin = [formattedString rangeOfString:loginIdField];
    GHAssertTrue(rangeLogin.location != NSNotFound, @"Formatted string should have login ID.");
    NSRange rangePinCode = [formattedString rangeOfString:pinCodeField];
    GHAssertTrue(rangePinCode.location != NSNotFound, @"Formatted string should have pin code.");
    NSRange rangeSecurePrint = [formattedString rangeOfString:securePrint];
    GHAssertTrue(rangeSecurePrint.location != NSNotFound, @"Formatted string should have secure print.");
}

- (void)testFormattedString_ListType
{
    // Mock
    NSDictionary *listKey = @{@"name": @"colorMode", @"type": @"list", @"defaultValue": @"0"};
    NSArray *settings = @[listKey];
    NSDictionary *group = @{@"setting": settings};
    NSArray *groups = @[group];
    NSDictionary *settingsTree = @{@"group": groups};
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper expect] andReturn:settingsTree] sharedPrintSettingsTree];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.colorMode = 1;
    NSString *formattedString = [previewSetting formattedString];
    
    [mockPrintSettingsHelper stopMocking];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    NSRange range = [formattedString rangeOfString:@"colorMode=1\n"];
    GHAssertTrue(range.location != NSNotFound, @"Formatted string should have colorMode=1\\n.");
}

- (void)testFormattedString_NumericType
{
    // Mock
    NSDictionary *listKey = @{@"name": @"copies", @"type": @"numeric", @"defaultValue": @"1"};
    NSArray *settings = @[listKey];
    NSDictionary *group = @{@"setting": settings};
    NSArray *groups = @[group];
    NSDictionary *settingsTree = @{@"group": groups};
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper expect] andReturn:settingsTree] sharedPrintSettingsTree];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.copies = 100;
    NSString *formattedString = [previewSetting formattedString];
    
    [mockPrintSettingsHelper stopMocking];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    NSRange range = [formattedString rangeOfString:@"copies=100\n"];
    GHAssertTrue(range.location != NSNotFound, @"Formatted string should have copies=100\\n.");
}

- (void)testFormattedString_BooleanType
{
    // Mock
    NSDictionary *listKey = @{@"name": @"scaleToFit", @"type": @"boolean", @"defaultValue": @"1"};
    NSArray *settings = @[listKey];
    NSDictionary *group = @{@"setting": settings};
    NSArray *groups = @[group];
    NSDictionary *settingsTree = @{@"group": groups};
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper expect] andReturn:settingsTree] sharedPrintSettingsTree];
    [[[mockPrintSettingsHelper expect] andReturn:settingsTree] sharedPrintSettingsTree]; // Called twice
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.scaleToFit = NO;
    NSString *formattedString1 = [previewSetting formattedString];
    previewSetting.scaleToFit = YES;
    NSString *formattedString2 = [previewSetting formattedString];
    
    [mockPrintSettingsHelper stopMocking];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString1, @"formattedString must no be nil.");
    GHAssertNotNil(formattedString2, @"formattedString must no be nil.");
    NSRange range = [formattedString1 rangeOfString:@"scaleToFit=0\n"];
    GHAssertTrue(range.location != NSNotFound, @"Formatted string should have scaleToFit=0\\n.");
    range = [formattedString2 rangeOfString:@"scaleToFit=1\n"];
    GHAssertTrue(range.location != NSNotFound, @"Formatted string should have scaleToFit=0\\n.");
}

@end
