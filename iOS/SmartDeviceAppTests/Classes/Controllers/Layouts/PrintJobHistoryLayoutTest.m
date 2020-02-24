//
//  PrintHistoryLayoutTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "PrintJobHistoryLayout.h"
#import "IPhoneXHelper.h"

@interface PrintJobHistoryLayoutTest : GHTestCase

@end

@implementation PrintJobHistoryLayoutTest

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

-(void) test001_setupForOrientation_HeightGreaterThanWidth
{
    if (![IPhoneXHelper isDeviceIPhoneX]) {
        PrintJobHistoryLayout *layout = [[PrintJobHistoryLayout alloc] init];

        id mockScreen = OCMClassMock([UIScreen class]);

        CGFloat expectedHeight = 568.0;
        CGFloat expectedWidth = 320.0;
        CGRect testRect = CGRectMake(0,0,expectedWidth, expectedHeight);
        OCMStub([mockScreen bounds]).andReturn(testRect);
        [[[[mockScreen stub] classMethod] andReturn:mockScreen] mainScreen];

        [layout setupForOrientation:UIInterfaceOrientationLandscapeLeft forDevice:UIUserInterfaceIdiomPhone];

        CGFloat groupWidth = [[layout valueForKey:@"groupWidth"] floatValue];
        GHAssertEqualsWithAccuracy(groupWidth, expectedHeight, 0.001 , @"");

        [mockScreen stopMocking];
    }
}

-(void) test001_setupForOrientation_WidthGreaterThanHeight
{
    if (![IPhoneXHelper isDeviceIPhoneX]) {
        PrintJobHistoryLayout *layout = [[PrintJobHistoryLayout alloc] init];

        id mockScreen = OCMClassMock([UIScreen class]);

        CGFloat expectedHeight = 320.0;
        CGFloat expectedWidth = 568.0;
        CGRect testRect = CGRectMake(0,0,expectedWidth, expectedHeight);
        OCMStub([mockScreen bounds]).andReturn(testRect);
        [[[[mockScreen stub] classMethod] andReturn:mockScreen] mainScreen];

        [layout setupForOrientation:UIInterfaceOrientationLandscapeLeft forDevice:UIUserInterfaceIdiomPhone];

        CGFloat groupWidth = [[layout valueForKey:@"groupWidth"] floatValue];
        GHAssertEqualsWithAccuracy(groupWidth, expectedWidth, 0.001 , @"");

        [mockScreen stopMocking];
    }
}
@end
