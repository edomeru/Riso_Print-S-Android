//
//  PrintPreviewViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintPreviewViewController.h"
#import "RootViewController.h"
#import "SlideSegue.h"

@interface PrintPreviewViewController ()

@end

@implementation PrintPreviewViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -
#pragma mark IBActions
- (IBAction)mainMenuAction:(id)sender
{
    [self performSegueWithIdentifier:@"PrintPreview-Home" sender:self];
}

- (IBAction)printSettingsAction:(id)sender
{
    [self performSegueWithIdentifier:@"PrintPreview-PrintSettings" sender:self];
}

- (IBAction)unwindFromSlidingDrawer:(UIStoryboardSegue *)segue
{
}

@end
