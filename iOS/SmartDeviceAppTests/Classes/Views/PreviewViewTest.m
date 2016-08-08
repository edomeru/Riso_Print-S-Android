//
//  PreviewViewTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PreviewView.h"
#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"

@interface PreviewView (Test)

- (CGPoint)adjustPreviewPosition:(CGPoint)position;
@end

@interface PreviewViewTest : GHTestCase

@end

@implementation PreviewViewTest
{
    float tolerance;
}

- (void) setUpClass
{
    tolerance = 0.001f;
}

#ifndef DISABLE_FAILED_TESTS
-(void) test001_setPreviewWithOrientation_Portrait
{
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    CGFloat testWidth = screenRect.size.width;
    CGFloat testHeight = screenRect.size.height;
    
    CGFloat aspectRatio = testWidth/testHeight;
    
    PreviewView *view = [[PreviewView alloc] initWithFrame:CGRectMake(0, 0, testWidth, testHeight)];
    [view setPreviewWithOrientation:kPreviewViewOrientationPortrait aspectRatio:aspectRatio];
    
    GHAssertNotNil(view.pageContentView, @"");
    GHAssertLessThanOrEqual(view.pageContentView.frame.size.height, testHeight, @"");
    GHAssertLessThanOrEqual(view.pageContentView.frame.size.width, testWidth, @"");
    
    CGFloat resultingRatio = view.pageContentView.frame.size.width/view.pageContentView.frame.size.height;
    
    GHAssertLessThanOrEqual(fabs(resultingRatio - aspectRatio), tolerance, @"");
}

-(void) test002_setPreviewWithOrientation_Landscape
{
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    CGFloat testWidth = screenRect.size.width;
    CGFloat testHeight = screenRect.size.height;
    
    CGFloat aspectRatio = testWidth/testHeight;
    
    PreviewView *view = [[PreviewView alloc] initWithFrame:CGRectMake(0, 0, testWidth, testHeight)];
    [view setPreviewWithOrientation:kPreviewViewOrientationLandscape aspectRatio:aspectRatio];
    
    GHAssertNotNil(view.pageContentView, @"");
    GHAssertLessThanOrEqual(view.pageContentView.frame.size.height, testHeight, @"");
    GHAssertLessThanOrEqual(view.pageContentView.frame.size.width, testWidth, @"");
    
    CGFloat resultingRatio = view.pageContentView.frame.size.height/view.pageContentView.frame.size.width;
    
    GHAssertLessThanOrEqual(fabs(resultingRatio - aspectRatio), tolerance, @"");
}
#endif //DISABLE_FAILED_TESTS

- (void)test003_adjustPreviewPosition
{
    CGPoint originalPosition = CGPointMake(0, 0);
    
    PreviewView *view = [[PreviewView alloc] initWithFrame:CGRectMake(0, 0, 200, 500)];
    UIView *dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 300, 600)];
    [view setValue:dummyContentView forKey:@"contentView"];
    
    // scale is not greater than 1, retain
    CGPoint newPosition = [view adjustPreviewPosition:originalPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(originalPosition, newPosition), @"");
    
    //scale is greater than 1, position greater than maxX
    [view setValue:[NSNumber numberWithFloat:1.5] forKey:@"scale"];
    CGPoint testPosition = CGPointMake(100, 25);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(50, testPosition.y)), @"");
    
    testPosition = CGPointMake(-75, 25);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(-50, testPosition.y)), @"");
    
    //scale is greater than 1, position greater than maxY
    testPosition = CGPointMake(25, 75);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, 50)), @"");
    
    testPosition = CGPointMake(25, -75);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, -50)), @"");
    
    //scale is greater than 1, position not exceeding max
    testPosition = CGPointMake(-30, 40);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, testPosition.y)), @"");
    
    //content width is less than container width
    dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 150, 600)];
    [view setValue:dummyContentView forKey:@"contentView"];
    testPosition = CGPointMake(-20, 35);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(0, testPosition.y)), @"");
    
    //content height is less than container height
    dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 400, 300)];
    [view setValue:dummyContentView forKey:@"contentView"];
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, 0)), @"");
    
    //left bookend is shown
    //book is horizontally flipping, width > height
    view.isLeftBookendShown = YES;
    testPosition = CGPointMake(20, 35);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(0, 0)), @"");
    //book is vertically flipping, width < height
    dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 250, 600)];
    [view setValue:dummyContentView forKey:@"contentView"];
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, 0)), @"");

    //right bookend is shown
    view.isLeftBookendShown = NO;
    view.isRightBookendShown = YES;
    //book is horizontally flipping, width > height
    dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 400, 300)];
    [view setValue:dummyContentView forKey:@"contentView"];
    testPosition = CGPointMake(-20, 35);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(0, 0)), @"");
    //book is vertically flipping, width < height
    dummyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 250, 600)];
    [view setValue:dummyContentView forKey:@"contentView"];
    testPosition = CGPointMake(25, -35);
    newPosition = [view adjustPreviewPosition:testPosition];
    GHAssertTrueNoThrow(CGPointEqualToPoint(newPosition, CGPointMake(testPosition.x, 0)), @"");
}

- (void)test004_adjustPannedPosition
{
    CGPoint originalPosition = CGPointMake(0, 0);
    PreviewView *view = [[PreviewView alloc] initWithFrame:CGRectMake(0, 0, 200, 500)];
    [view setValue:[NSValue valueWithCGPoint:originalPosition] forKey:@"position"];
    id mockPreviewView = OCMPartialMock(view);
    [[mockPreviewView expect] adjustPreviewPosition:originalPosition];
    
    [mockPreviewView adjustPannedPosition];
    
    [mockPreviewView verify];
    [mockPreviewView stopMocking];
}

@end
