//
//  AppSettingsHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "AppSettingsHelper.h"


@interface AppSettingsHelperTest : GHTestCase
{
    id mockDefaults;
    NSString *mockCommunityNameToReturn;
    NSString *mockCommunityNameToSave;
}
@end

@implementation AppSettingsHelperTest

- (void)setUpClass
{
    mockDefaults = OCMClassMock([NSUserDefaults class]);
    [[[mockDefaults stub] andReturn:mockDefaults] standardUserDefaults];
    [[[mockDefaults stub] andCall:@selector(mockObjectForKey:) onObject:self] objectForKey:OCMOCK_ANY];
    [[[mockDefaults stub] andCall:@selector(mockSetObject:forKey:) onObject:self] setObject:OCMOCK_ANY forKey:OCMOCK_ANY];
}

- (void)tearDownClass
{
    [mockDefaults stopMocking];
}

- (void)test001_GetSNMPCommunityName_ReturnNil
{
    mockCommunityNameToReturn = nil;
    
    NSString *snmpCommunityName = [AppSettingsHelper getSNMPCommunityName];
    GHAssertEqualStrings(@"public", snmpCommunityName, @"");
    GHAssertEqualStrings(@"public", mockCommunityNameToSave, @"");
}

- (void)test002_GetSNMPCommunityName_ReturnOthers
{
    mockCommunityNameToReturn = @"testName";
    
    NSString *snmpCommunityName = [AppSettingsHelper getSNMPCommunityName];
    GHAssertEqualStrings(mockCommunityNameToReturn, snmpCommunityName, @"");
    GHAssertEqualStrings(@"public", mockCommunityNameToSave, @"");
}

- (void)test003_SaveSNMPCommunityName_NewValue
{
    NSString *testString = @"testSave";
    
    [AppSettingsHelper saveSNMPCommunityName:testString];
    GHAssertEqualStrings(testString, mockCommunityNameToSave, @"");
}

- (void)test004_SaveSNMPCommunityName_Blank
{
    NSString *testString = @"";
    
    [AppSettingsHelper saveSNMPCommunityName:testString];
    GHAssertEqualStrings(@"public", mockCommunityNameToSave, @"");
}

- (void)test005_SaveSNMPCommunityName_Nil
{
    mockCommunityNameToSave = @"notpublic";
    
    [AppSettingsHelper saveSNMPCommunityName:nil];
    GHAssertEqualStrings(@"public", mockCommunityNameToSave, @"");
}


#pragma-mark Mock Methods

- (id)mockObjectForKey:(NSString *)key
{
    return mockCommunityNameToReturn;
}

- (void)mockSetObject:(id)object forKey:(NSString *)key
{
    mockCommunityNameToSave = (NSString *)object;
}


@end