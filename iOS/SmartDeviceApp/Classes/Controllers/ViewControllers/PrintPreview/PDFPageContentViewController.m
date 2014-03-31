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
	// Do any additional setup after loading the view.
    
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    CGSize size = self.imageView.frame.size;
    CGRect rect = CGRectMake(0.0f, 0.0f, size.width, size.height);
    [self.activityIndicatorView startAnimating];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^
    {
        UIImage *image = [self.printDocument imageForPage:self.pageIndex + 1 withRect:rect];
        dispatch_async(dispatch_get_main_queue(), ^
        {
            [self.activityIndicatorView stopAnimating];
            self.imageView.image = image;
        });
    });
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
