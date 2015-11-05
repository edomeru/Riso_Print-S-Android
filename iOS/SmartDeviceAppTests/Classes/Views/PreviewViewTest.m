//
//  PreviewViewTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PreviewView.h"
#import <GHUnitIOS/GHUnit.h>

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
    
    GHAssertLessThanOrEqual(fabsf(resultingRatio - aspectRatio), tolerance, @"");
}

-(void) test001_setPreviewWithOrientation_Landscape
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
    
    GHAssertLessThanOrEqual(fabsf(resultingRatio - aspectRatio), tolerance, @"");
}

@end
