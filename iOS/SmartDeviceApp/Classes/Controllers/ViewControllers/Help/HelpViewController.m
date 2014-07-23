//
//  HelpViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "HelpViewController.h"
#import "UIViewController+Segue.h"
#import "HomeViewController.h"

#define HELP_HTML @"help"

@interface HelpViewController ()

/**
 * Reference to the view for displaying the Help content.
 */
@property (nonatomic, weak) IBOutlet UIWebView *webView;

/**
 * Reference to the main menu button on the header.
 */
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;

/**
 * Responds to pressing the main menu button in the header.
 * Displays the Main Menu panel.
 *
 * @param sender the button object
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 * Unwind segue back to the "Help" screen.
 * Called when transitioning back to the "Help"
 * screen from the the Main Menu panel.
 *
 * @param sender the segue object
 */
- (IBAction)unwindToHelp:(UIStoryboardSegue *)segue;

@end

@implementation HelpViewController

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
    NSURL *helpUrl = [[NSBundle mainBundle] URLForResource:HELP_HTML withExtension:@"html"];
    [self.webView loadRequest:[NSURLRequest requestWithURL:helpUrl]];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - IBActions

- (IBAction)mainMenuAction:(id)sender
{
    self.mainMenuButton.enabled = NO;
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)unwindToHelp:(UIStoryboardSegue *)segue
{
    self.mainMenuButton.enabled = YES;
}

@end
