//
//  XMLParserTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "XMLParser.h"

@interface XMLParser(Test)

@end

@interface XMLParserTest:GHTestCase

@end

@implementation XMLParserTest

- (void)testDictionaryFromXMLFile
{
    NSURL *testXMLURL = [[NSBundle mainBundle] URLForResource:@"printsettings" withExtension:@"xml"];

    NSDictionary *dictionary = [XMLParser dictionaryFromXMLFile:[testXMLURL path]];
    GHAssertNotNil(dictionary, @"");
    
    NSDictionary *printSetting = [dictionary objectForKey:@"printsettings"];
    
    GHAssertNotNil(printSetting, @"");
    
    NSArray *groups = [printSetting objectForKey:@"group"];
    GHAssertNotNil(groups, @"");
    
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        GHAssertNotNil(settings, @"");
        
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            
            GHAssertNotNil(key, @"");
            NSString *type = [setting objectForKey:@"type"];
            GHAssertNotNil(type, @"");
            NSString *defaultValue = [setting objectForKey:@"default"];
            GHAssertNotNil(defaultValue, @"");
        }
    }
}

- (void)testDictionaryFromXMLFile_InvalidFile
{
    // Mock
    id mockUrl = [OCMockObject mockForClass:[NSURL class]];
    [[mockUrl expect] fileURLWithPath:OCMOCK_ANY];
    id mockData = [OCMockObject mockForClass:[NSData class]];
    [[[mockData expect] andReturn:[@"Invalid Data" dataUsingEncoding:NSUTF8StringEncoding]] dataWithContentsOfURL:OCMOCK_ANY];
    
    // SUT
    NSDictionary *dictionary = [XMLParser dictionaryFromXMLFile:@"Test"];
    
    GHAssertNil(dictionary, @"Dictionary must be nil");
}

@end
