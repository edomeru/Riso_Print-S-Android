//
//  PDFPageContentControllerViewTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PDFPageContentViewController.h"

@interface PDFPageContentViewController (Test)
@property (nonatomic, weak) IBOutlet UIImageView *imageView;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicatorView;
@end

@interface PDFPageContentViewControllerTest : GHTestCase

@end

@implementation PDFPageContentViewControllerTest
{
    NSString* storyboardId;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PDFPageContentViewController";
}


- (void)test001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PDFPageContentViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    [viewController view];
    
    GHAssertNotNil(viewController.imageView, @"");
    GHAssertNotNil(viewController.activityIndicatorView, @"");
}

- (void)test002_UIViewLoading_NoImage
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PDFPageContentViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.isBookendPage = NO;
    viewController.image = nil;
    
    [viewController view];
    
    GHAssertNotNil(viewController.activityIndicatorView, @"");
    GHAssertTrue(viewController.activityIndicatorView.isAnimating, @"");
}

- (void)test003_UIViewLoading_WithImage
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PDFPageContentViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.isBookendPage = NO;
    viewController.image = [UIImage imageNamed:@"LaunchImage"]; //use launch image as temp image
    
    [viewController view];
    
    GHAssertNotNil(viewController.activityIndicatorView, @"");
    GHAssertFalse(viewController.activityIndicatorView.isAnimating, @"");
}

- (void)test004_UIViewLoading_BookendPage
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PDFPageContentViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.isBookendPage = YES;
    viewController.image = nil; //use launch image as temp image
    
    [viewController view];
    
    GHAssertNotNil(viewController.activityIndicatorView, @"");
    GHAssertFalse(viewController.activityIndicatorView.isAnimating, @"");
    GHAssertTrue(viewController.imageView.hidden, @"");
    GHAssertEquals(viewController.view.backgroundColor, [UIColor clearColor], @"");
}

- (void)test005_setImage
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PDFPageContentViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.isBookendPage = NO;
    viewController.image = nil;
    
    [viewController view];
    
    GHAssertNotNil(viewController.activityIndicatorView, @"");
    GHAssertTrue(viewController.activityIndicatorView.isAnimating, @"");
    UIImage * testImage = [UIImage imageNamed:@"LaunchImage"];
    
    [viewController setImage:testImage];
    
    GHAssertFalse(viewController.activityIndicatorView.isAnimating, @"");
    GHAssertFalse(viewController.imageView.hidden, @"");
    GHAssertEqualObjects(viewController.imageView.image, testImage, @"");
    GHAssertEqualObjects(viewController.image, testImage, @"");}
@end
