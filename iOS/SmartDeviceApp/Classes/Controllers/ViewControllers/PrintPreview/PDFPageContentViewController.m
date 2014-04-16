//
//  PDFPageViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFPageContentViewController.h"

@interface PDFPageContentViewController ()

/**
 Outlet for the Image View of the View
 */
@property (nonatomic, weak) IBOutlet UIImageView *imageView;

/**
 Outlet for the Activity Indicator of the View
 */
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicatorView;

@end

@implementation PDFPageContentViewController

#pragma mark - Public Methods

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Show activity indicator if an image is not available
    if (self.image == nil && self.isBookendPage == NO)
    {
        [self.activityIndicatorView startAnimating];
    }
    else
    {
        self.imageView.image = self.image;
    }
    
    //The bookend page is not part of the actual pages of the pdf.
    //It is a transparent page (invisible) that is used as the other half of the first page/last page in a 2 page view
    if(self.isBookendPage == YES)
    {
        self.imageView.hidden = YES;
        [self.view setBackgroundColor:[UIColor clearColor]];
    }
}

#pragma mark - Getter/Setter Methods

- (void)setImage:(UIImage *)image
{
    // Hide Activity indicator when image is available
    [self.activityIndicatorView stopAnimating];
    _image = image;
    self.imageView.image = image;
}

@end
