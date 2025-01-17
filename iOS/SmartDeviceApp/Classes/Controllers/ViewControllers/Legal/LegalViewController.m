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

#define USE_INFO_PLIST_COPYRIGHT 1

@interface LegalViewController ()<UIWebViewDelegate>

/**
 * Reference to the view for displaying the Legal content.
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
 * Unwind segue back to the "Legal" screen.
 * Called when transitioning back to the "Legal"
 * screen from the the Main Menu panel.
 *
 * @param sender the segue object
 */
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
    NSString *build = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    NSString *showVersion = [NSString stringWithFormat:@"%@.%@", version, build];
    
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_APP_NAME_ID, appName]];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_VERSION_ID, showVersion]];
    
#if USE_INFO_PLIST_COPYRIGHT
    NSString *copyright = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSHumanReadableCopyright"];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_COPYRIGHT_ID, copyright]];
#endif
}

@end
