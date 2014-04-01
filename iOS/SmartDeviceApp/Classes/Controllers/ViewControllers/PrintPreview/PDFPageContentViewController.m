//
//  PDFPageViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFPageContentViewController.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"

@interface PDFPageContentViewController ()

@property (nonatomic, weak) IBOutlet UIImageView *imageView;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicatorView;

@property (nonatomic, weak) PrintDocument *printDocument;

@end

@implementation PDFPageContentViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Show activity indicator
    if (self.image == nil)
    {
        [self.activityIndicatorView startAnimating];
    }
    else
    {
        self.imageView.image = self.image;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setImage:(UIImage *)image
{
    [self.activityIndicatorView stopAnimating];
    _image = image;
    self.imageView.image = image;
}

@end
