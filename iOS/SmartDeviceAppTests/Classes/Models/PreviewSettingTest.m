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
        NSString *field = [NSString stringWithFormat:@"%@=%d\n", key, value];
        NSRange range = [formattedString rangeOfString:field];
        GHAssertTrue(range.location != NSNotFound, @"Formatted string should have %@.", field);
    }
}

- (void)testFormattedString_PrinterSettingsNil
{
    // Mock
    id mockPrintSettingsHelper = [OCMockObject mockForClass:[PrintSettingsHelper class]];
    [[[mockPrintSettingsHelper expect] andReturn:nil] sharedPrintSettingsTree];
    
    // SUT
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSString *formattedString = [previewSetting formattedString];
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    GHAssertTrue([formattedString length] == 0, @"formattedString must be empty.");
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
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    GHAssertTrue([formattedString length] == 0, @"formattedString must be empty.");
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
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    GHAssertEqualStrings(formattedString, @"colorMode=1\n", @"formattedString must match.");
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
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString, @"formattedString must no be nil.");
    GHAssertEqualStrings(formattedString, @"copies=100\n", @"formattedString must match.");
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
    
    // Verification
    GHAssertNoThrow([mockPrintSettingsHelper verify], @"");
    GHAssertNotNil(formattedString1, @"formattedString must no be nil.");
    GHAssertNotNil(formattedString2, @"formattedString must no be nil.");
    GHAssertEqualStrings(formattedString1, @"scaleToFit=0\n", @"formattedString must match.");
    GHAssertEqualStrings(formattedString2, @"scaleToFit=1\n", @"formattedString must match.");
}

@end
