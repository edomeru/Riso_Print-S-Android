//
//  RootViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "RootViewController.h"
#import "SlidingSegue.h"

@interface RootViewController ()

@end

@implementation RootViewController

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
    
    [self performSegueWithIdentifier:@"Root-PrintPreview" sender:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

- (UIStoryboardSegue *)segueForUnwindingToViewController:(UIViewController *)toViewController fromViewController:(UIViewController *)fromViewController identifier:(NSString *)identifier
{
    if ([identifier isEqualToString:@"UnwindLeft"])
    {
        SlidingSegue *segue = [[SlidingSegue alloc] initWithIdentifier:identifier source:fromViewController destination:toViewController];
        segue.isUnwinding = YES;
        segue.slideDirection = SlideLeft;
        return segue;
    }
    else if ([identifier isEqualToString:@"UnwindRight"])
    {
        SlidingSegue *segue = [[SlidingSegue alloc] initWithIdentifier:identifier source:fromViewController destination:toViewController];
        segue.isUnwinding = YES;
        segue.slideDirection = SlideRight;
        return segue;
    }
    
    return nil;
}

@end
