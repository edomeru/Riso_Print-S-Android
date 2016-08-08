//
//  ScreenLayoutHelperTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2016 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "ScreenLayoutHelper.h"


@interface ScreenLayoutHelperTest : GHTestCase

@end

@implementation ScreenLayoutHelperTest

- (void)testGetPortraitScreenWidth_Portrait
{
    CGFloat expectedWidth = 200.0f;
    //mock screen rect
    CGRect screenRect = CGRectMake(0, 0, expectedWidth, 500.0f);
    
    id mockUIScreen = OCMClassMock([UIScreen class]);
    [[[mockUIScreen stub] andReturnValue:OCMOCK_VALUE(screenRect)] bounds];
    [[[[mockUIScreen stub] andReturn:mockUIScreen] classMethod] mainScreen];
    

    CGFloat screenWidth = [ScreenLayoutHelper getPortraitScreenWidth];

    GHAssertTrueNoThrow(fabs(expectedWidth - screenWidth) < 0.00001, @"");
    
    [mockUIScreen stopMocking];
}

- (void)testGetPortraitScreenWidth_Landscape
{
    CGFloat expectedWidth = 200.0f;
    //mock screen rect
    CGRect screenRect = CGRectMake(0, 0, 500.0f, expectedWidth);
    
    id mockUIScreen = OCMClassMock([UIScreen class]);
    [[[mockUIScreen stub] andReturnValue:OCMOCK_VALUE(screenRect)] bounds];
    [[[[mockUIScreen stub] andReturn:mockUIScreen] classMethod] mainScreen];
    
    
    CGFloat screenWidth = [ScreenLayoutHelper getPortraitScreenWidth];
    
    GHAssertTrueNoThrow(fabs(expectedWidth - screenWidth) < 0.00001, @"");
    
    [mockUIScreen stopMocking];
}


@end