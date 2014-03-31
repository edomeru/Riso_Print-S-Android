//
//  PListHelperTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/31/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PListHelper.h"

@interface PListHelperTest : GHTestCase{ }

@end

@implementation PListHelperTest
-(void) testReadApplicationSettings
{
    NSDictionary *appSettings = [PListHelper readApplicationSettings];
    GHAssertNotNil(appSettings, @"");
    NSString *cardId = (NSString *)[appSettings objectForKey:@"CardReaderID"];
    GHAssertNotNil(cardId, @"");
    NSString *communityName = (NSString *)[appSettings objectForKey:@"CommunityName"];
    GHAssertNotNil(communityName, @"");
}


-(void) testSetApplicationSettings
{
    NSMutableDictionary *appSettings = [[PListHelper readApplicationSettings] mutableCopy];
    GHAssertNotNil(appSettings, @"");
    NSString *testCardIDData = @"testCardID";
    [appSettings setObject: testCardIDData forKey:@"CardReaderID"];
    NSString *testCommunityName = @"testCommunityName";
    [appSettings setObject: testCommunityName forKey:@"CommunityName"];
    
    [PListHelper setApplicationSettings:appSettings];
    
    NSDictionary *updatedDict = [PListHelper readApplicationSettings];
    
    GHAssertEqualStrings(testCardIDData, [updatedDict objectForKey:@"CardReaderID"], @"");
    GHAssertEqualStrings(testCommunityName, [updatedDict objectForKey:@"CommunityName"], @"");
}
@end
