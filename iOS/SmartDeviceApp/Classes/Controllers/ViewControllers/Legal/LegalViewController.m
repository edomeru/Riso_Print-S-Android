//
//  LegalViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "LegalViewController.h"
#import "UIViewController+Segue.h"
#import "HomeViewController.h"

#define LEGAL_HTML @"legal"

#define DOC_APP_NAME_ID @"localize_appname"
#define DOC_VERSION_ID @"localize_version"
#define DOC_COPYRIGHT_ID @"localize_copyright"

#define DOC_REPLACE_STRING @"document.getElementById('%@').innerHTML = '%@'"

@interface LegalViewController ()<UIWebViewDelegate>

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

#pragma mark - UIWebViewDelegate

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSString *appName = NSLocalizedString(IDS_APP_NAME, "");
    NSString *version = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"];
    NSString *copyright = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSHumanReadableCopyright"];
    
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_APP_NAME_ID, appName]];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_VERSION_ID, version]];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_COPYRIGHT_ID, copyright]];
}

@end
