//
//  LegalViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 4/16/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "LegalViewController.h"
#import "UIViewController+Segue.h"
#import "HomeViewController.h"

#define LEGAL_HTML @"legal"

@interface LegalViewController ()

@property (nonatomic, weak) IBOutlet UIWebView *webView;
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;

- (IBAction)mainMenuAction:(id)sender;
- (IBAction)unwindToLegal:(UIStoryboardSegue *)segue;

@end

@implementation LegalViewController

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
    NSURL *helpUrl = [[NSBundle mainBundle] URLForResource:LEGAL_HTML withExtension:@"html"];
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

- (IBAction)unwindToLegal:(UIStoryboardSegue *)segue
{
    self.mainMenuButton.enabled = YES;
}

@end
