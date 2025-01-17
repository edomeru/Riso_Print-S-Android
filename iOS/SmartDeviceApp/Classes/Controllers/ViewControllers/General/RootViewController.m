//
//  RootViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "RootViewController.h"
#import "PrintPreviewViewController.h"
#import "SlideSegue.h"
#import "SlideOverSegue.h"
#import "UIViewController+Segue.h"
#import "PDFFileManager.h"

#import "LicenseAgreementViewController.h"

@interface RootViewController ()

@end

@implementation RootViewController

static RootViewController *container;

+ (RootViewController *)container
{
    return container;
}

- (void)awakeFromNib
{
    [super awakeFromNib];
    container = self;
}

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
    
    
    if(![LicenseAgreementViewController hasConfirmedToLicenseAgreement])
    {
        // if user has not confirmed to license agreement yet
        // open license agreement
        [self performSegueTo:[LicenseAgreementViewController class]];
    }
    else
    {
        // Show Print Preview Screen
        [self performSegueTo:[PrintPreviewViewController class]];
    }
    
    
    
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
