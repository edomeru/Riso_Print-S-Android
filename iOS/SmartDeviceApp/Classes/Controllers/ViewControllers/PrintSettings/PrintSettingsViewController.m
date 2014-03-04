//
//  PrintSettingsViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsViewController.h"
#import "RootViewController.h"

@interface PrintSettingsViewController ()

@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;

@end

@implementation PrintSettingsViewController

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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)didMoveToParentViewController:(UIViewController *)parent
{
    RootViewController *container = (RootViewController *) parent;
    // Slide enter
    if (container != nil)
    {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(toMainAction:)];
        [container.mainView addGestureRecognizer:tapRecognizer];
        self.tapRecognizer = tapRecognizer;
    }
    else
    {
        for (UIGestureRecognizer *recognizer in self.tapRecognizer.view.gestureRecognizers)
        {
            if (recognizer == self.tapRecognizer)
            {
                [self.tapRecognizer.view removeGestureRecognizer:self.tapRecognizer];
                break;
            }
        }
    }
}

#pragma mark -
#pragma mark IBActions
- (IBAction)toMainAction:(id)sender
{
    // Unwind
    [self performSegueWithIdentifier:@"UnwindRight" sender:self];
}

@end
