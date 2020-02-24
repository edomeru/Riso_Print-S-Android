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
#import "IPhoneXHelper.h"
#import <WebKit/WKWebView.h>
#import <WebKit/WKNavigationDelegate.h>
#import <WebKit/WKNavigationAction.h>
#import "HTMLHelper.h"

#define HELP_HTML @"help"

@interface HelpViewController ()<WKNavigationDelegate>

/**
 * Reference to the view for displaying the Help content.
 * This is only used in iOS 8.
 */
@property (nonatomic, weak) IBOutlet UIWebView *webView;

/**
 * Reference to the view for displaying the Help content.
 * This is only used in iOS 9 and above.
 */
@property (nonatomic, strong) WKWebView *wkWebView;

/**
 * Reference to the view for displaying the page's title
 * and main menu button
 */
@property (weak, nonatomic) IBOutlet UIView *headerView;

/**
 * Reference to the main menu button on the header.
 */
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;

@property (nonatomic) BOOL isIOS9;

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
    
    if (@available(iOS 13.0, *)) {
        self.view.backgroundColor = [UIColor colorNamed:@"color_white_gray6"];
    }
    
    /*
     * WKWebView has an issue in iOS 8 when loading local assets referenced in HTML.
     * For iOS 8, load the HTML in UIWebView instead.
     */
    NSOperatingSystemVersion iOS9 = (NSOperatingSystemVersion){9,0,0};
    self.isIOS9 = [[NSProcessInfo processInfo] isOperatingSystemAtLeastVersion:iOS9];
    if (self.isIOS9) {
        [self.webView setHidden:YES];
        [self setupWkWebView];
        [HTMLHelper loadHTML:HELP_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:YES];
    } else {
        [HTMLHelper loadHTML:HELP_HTML toWebView:self.webView];
    }
}

- (void)traitCollectionDidChange:(UITraitCollection *)previousTraitCollection
{
    [super traitCollectionDidChange:previousTraitCollection];

    if (@available(iOS 13.0, *)) {
        if (previousTraitCollection.userInterfaceStyle != self.traitCollection.userInterfaceStyle) {
            if (self.isIOS9) {
                [HTMLHelper loadHTML:HELP_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:YES];
            } else {
                [HTMLHelper loadHTML:HELP_HTML toWebView:self.webView];
            }
        }
    }
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

#pragma mark - Private Methods

- (void)setupWkWebView
{
    self.wkWebView = [[WKWebView alloc] init];
    self.wkWebView.navigationDelegate = self;
    [self.view addSubview:self.wkWebView];

    /* WKWebView automatically ajusts to safe area bounds.
     * While safe area layout is not yet implemented, allow WKWebView to cover device's full width.
     */
    if (@available(iOS 11.0, *)) {
        if ([IPhoneXHelper isDeviceIPhoneX]) {
            self.wkWebView.scrollView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        }
    }

    // Set up constraints
    self.wkWebView.translatesAutoresizingMaskIntoConstraints = NO;
    NSLayoutConstraint *leadingConstraint = [NSLayoutConstraint constraintWithItem:self.wkWebView attribute:NSLayoutAttributeLeading relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeLeading multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *trailingConstraint = [NSLayoutConstraint constraintWithItem:self.wkWebView attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeTrailing multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *topConstraint = [NSLayoutConstraint constraintWithItem:self.wkWebView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self.headerView attribute:NSLayoutAttributeBottom multiplier:1.0f constant:0.0f];
    NSLayoutConstraint *bottomConstraint = [NSLayoutConstraint constraintWithItem:self.wkWebView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeBottom multiplier:1.0f constant:0.0f];

    [self.view addConstraints:@[leadingConstraint, trailingConstraint, topConstraint, bottomConstraint]];
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    if ([IPhoneXHelper isDeviceIPhoneX]) {
        UIDeviceOrientation orientation = UIDevice.currentDevice.orientation;
        if (orientation == UIDeviceOrientationPortrait) {
            self.wkWebView.scrollView.contentInset = UIEdgeInsetsZero;
        }
        else if (orientation == UIDeviceOrientationLandscapeLeft) {
            self.wkWebView.scrollView.contentInset = UIEdgeInsetsMake(0, 0, 0, -[IPhoneXHelper sensorHousingHeight]);
        }
    }
}

#pragma mark - WKWebViewNavigationDelegate

- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler
{
    decisionHandler(WKNavigationActionPolicyAllow);

    // When an internal link is clicked, the scrollview's inset and offset change.
    // On an iPhone X these values have to be adjusted to account for our notch mask view
    if ([IPhoneXHelper isDeviceIPhoneX]) {

        // If the URL's fragment part is not empty, this means we're navigating to an internal link
        if ([navigationAction.request.URL.lastPathComponent isEqualToString:[HELP_HTML stringByAppendingString:@".html"]] && navigationAction.request.URL.fragment.length != 0) {
            webView.scrollView.contentInset = UIEdgeInsetsZero;
            webView.scrollView.contentOffset = CGPointZero;
        }
    }
}
@end
