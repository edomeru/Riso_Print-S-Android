//
//  PDFPageViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PDFPageContentViewController.h"

@interface PDFPageContentViewController ()

/**
 * Reference to the animated loading indicator.
 * This is displayed while the rendered image is not yet available for display.
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
        self.view.layer.contents = (id)self.image.CGImage;
        [self.view setNeedsDisplay];
    }
    
    //The bookend page is not part of the actual pages of the pdf.
    //It is a transparent page (invisible) that is used as the other half of the first page/last page in a 2 page view
    if(self.isBookendPage == YES)
    {
        [self.view setBackgroundColor:[UIColor clearColor]];
    }
}

#pragma mark - Getter/Setter Methods

- (void)setImage:(UIImage *)image
{
    // Hide Activity indicator when image is available
    [self.activityIndicatorView stopAnimating];
    _image = image;
    self.view.layer.contents = (id)_image.CGImage;
    [self.view setNeedsDisplay];
}

@end
