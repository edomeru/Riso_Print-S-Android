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
#import "IPhoneXHelper.h"

@interface ScreenLayoutHelperTest : GHTestCase

@end

@implementation ScreenLayoutHelperTest

- (void)testGetPortraitScreenWidth_Portrait
{
    if (![IPhoneXHelper isDeviceIPhoneX]) {
        CGFloat expectedWidth = 200.0f;
        //mock screen rect
        CGRect screenRect = CGRectMake(0, 0, expectedWidth, 500.0f);

        UIScreen* mainScreen = [UIScreen mainScreen];
        id mockUIScreen = [OCMockObject partialMockForObject:mainScreen];
        [[[mockUIScreen stub] andReturnValue:OCMOCK_VALUE(screenRect)] bounds];

        CGFloat screenWidth = [ScreenLayoutHelper getPortraitScreenWidth];
        [mockUIScreen stopMocking];

        GHAssertTrueNoThrow(fabs(expectedWidth - screenWidth) < 0.00001, @"");
    }
}

- (void)testGetPortraitScreenWidth_Landscape
{
    if (![IPhoneXHelper isDeviceIPhoneX]) {
        CGFloat expectedWidth = 200.0f;
        //mock screen rect
        CGRect screenRect = CGRectMake(0, 0, 500.0f, expectedWidth);

        UIScreen* mainScreen = [UIScreen mainScreen];
        id mockUIScreen = [OCMockObject partialMockForObject:mainScreen];
        [[[mockUIScreen stub] andReturnValue:OCMOCK_VALUE(screenRect)] bounds];

        CGFloat screenWidth = [ScreenLayoutHelper getPortraitScreenWidth];
        [mockUIScreen stopMocking];

        GHAssertTrueNoThrow(fabs(expectedWidth - screenWidth) < 0.00001, @"");
    }
}


@end
