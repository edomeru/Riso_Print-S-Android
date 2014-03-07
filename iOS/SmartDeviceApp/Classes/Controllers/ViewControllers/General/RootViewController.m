//
//  RootViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "RootViewController.h"
#import "PrintPreviewViewController.h"
#import "SlideSegue.h"
#import "SlideOverSegue.h"
#import "UIViewController+Segue.h"

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
    [self performSegueTo:[PrintPreviewViewController class]];
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
    if ([identifier hasPrefix:@"UnwindTo"])
    {
        SlideSegue *segue = [[SlideSegue alloc] initWithIdentifier:identifier source:fromViewController destination:toViewController];
        segue.isUnwinding = YES;
        return segue;
    }
    
    if ([identifier hasPrefix:@"UnwindFromOverTo"])
    {
        SlideOverSegue *segue = [[SlideOverSegue alloc] initWithIdentifier:identifier source:fromViewController destination:toViewController];
        segue.isUnwinding = YES;
        return  segue;
    }
    
    return nil;
}

@end
