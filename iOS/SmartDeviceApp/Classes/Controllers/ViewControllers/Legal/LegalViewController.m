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
#import "IPhoneXHelper.h"
#import <WebKit/WKWebView.h>
#import <WebKit/WKNavigationDelegate.h>
#import "HTMLHelper.h"

#define LEGAL_HTML @"legal"

#define DOC_APP_NAME_ID @"localize_appname"
#define DOC_VERSION_ID @"localize_version"
#define DOC_COPYRIGHT_ID @"localize_copyright"

#define DOC_REPLACE_STRING @"document.getElementById('%@').innerHTML = '%@'"

#define USE_INFO_PLIST_COPYRIGHT 1

@interface LegalViewController ()<UIWebViewDelegate, WKNavigationDelegate, UIScrollViewDelegate>

/**
 * Reference to the view for displaying the Legal content.
 * This is only used in iOS 8.
 */
@property (nonatomic, weak) IBOutlet UIWebView *webView;

/**
 * Reference to the view for displaying the Legal content.
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

NSString *appName;
NSString *version;
NSString *build;
NSString *showVersion;
bool isIOS9;
bool userDidZoom;
NSNumber * _Nullable portraitMinimumZoomScale;
NSNumber * _Nullable portraitMaximumZoomScale;
NSNumber * _Nullable landscapeMinimumZoomScale;
NSNumber * _Nullable landscapeMaximumZoomScale;

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

    if (@available(iOS 13.0, *)) {
        self.view.backgroundColor = [UIColor colorNamed:@"color_white_gray6"];
    }

    // Set up the strings that will be injected into the HTML once the webview has finished loading
    appName = NSLocalizedString(IDS_APP_NAME, "");
    version = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"];
    build = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    showVersion = [NSString stringWithFormat:@"%@.%@", version, build];

    /*
     * WKWebView has an issue in iOS 8 when loading local assets referenced in HTML.
     * For iOS 8, load the HTML in UIWebView instead.
     */
    NSOperatingSystemVersion iOS9 = (NSOperatingSystemVersion){9,0,0};
    isIOS9 = [[NSProcessInfo processInfo] isOperatingSystemAtLeastVersion:iOS9];
    if (isIOS9) {
        [self.webView setHidden:YES];
        [self setupWkWebView];
        [HTMLHelper loadHTML:LEGAL_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:NO];
    }
    else {
        self.webView.scrollView.delegate = self;
        [self.webView.scrollView addObserver:self forKeyPath:@"contentSize" options:0 context:nil];
        [HTMLHelper loadHTML:LEGAL_HTML toWebView:self.webView];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    if (isIOS9) {
        self.wkWebView.scrollView.delegate = nil;
    }
    else {
        [self.webView.scrollView removeObserver:self forKeyPath:@"contentSize"];
        self.webView.scrollView.delegate = nil;
    }
}

- (void)traitCollectionDidChange:(UITraitCollection *)previousTraitCollection
{
    [super traitCollectionDidChange:previousTraitCollection];

    if (@available(iOS 13.0, *)) {
        if (previousTraitCollection.userInterfaceStyle != self.traitCollection.userInterfaceStyle) {
            if (isIOS9) {
                [HTMLHelper loadHTML:LEGAL_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:NO];
            } else {
                [HTMLHelper loadHTML:LEGAL_HTML toWebView:self.webView];
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

- (IBAction)unwindToLegal:(UIStoryboardSegue *)segue
{
    self.mainMenuButton.enabled = YES;
}

#pragma mark - UIWebViewDelegate

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_APP_NAME_ID, appName]];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_VERSION_ID, showVersion]];

#if USE_INFO_PLIST_COPYRIGHT
    NSString *copyright = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSHumanReadableCopyright"];
    [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_COPYRIGHT_ID, copyright]];
#endif
}

#pragma mark - WKNavigationDelegate

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation
{
    [webView evaluateJavaScript:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_APP_NAME_ID, appName] completionHandler:nil];
    [webView evaluateJavaScript:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_VERSION_ID, showVersion] completionHandler:nil];
    
#if USE_INFO_PLIST_COPYRIGHT
    NSString *copyright = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSHumanReadableCopyright"];
    [webView evaluateJavaScript:[NSString stringWithFormat:DOC_REPLACE_STRING, DOC_COPYRIGHT_ID, copyright] completionHandler:nil];
#endif
}

#pragma mark - Private Methods

- (void)setupWkWebView
{
    self.wkWebView = [[WKWebView alloc] init];
    self.wkWebView.navigationDelegate = self;
    self.wkWebView.scrollView.delegate = self;
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

#pragma mark - Scrollview zoom scale handling

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context
{
    if (object == self.webView.scrollView) {
        if ([keyPath isEqualToString:@"contentSize"]) {
            /* An issue occurs in UIWebView where the contentSize becomes excessive when rotating from landscape to portrait.
             * This happens when the user previously changed the zoom scale, went back to normal scale, then rotated the device.
             * To catch this case, check if the contentSize width is wider than the frame of the web view even though the user
             * did not zoom in.
             * If the user did initiate the zoom (and consequently the change in contentSize), do not constrain the
             * contentSize because we need to be able to scroll.
             * Otherwise, set the contentSize width equal to the width of the webview.
             */
            if ((self.webView.scrollView.contentSize.width > self.webView.frame.size.width) && !userDidZoom) {
                [self.webView.scrollView setContentSize:CGSizeMake(self.webView.frame.size.width, self.webView.scrollView.contentSize.height)];
            }
        }
    }
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    CGFloat aspectRatio;
    UIScrollView *scrollView;
    if (isIOS9) {
        aspectRatio = self.wkWebView.frame.size.width / self.wkWebView.frame.size.height;
        scrollView = self.wkWebView.scrollView;
    }
    else {
        aspectRatio = self.webView.frame.size.width / self.webView.frame.size.height;
        scrollView = self.webView.scrollView;
    }
    
    // Set the min and max zoom scales for landscape and portrait orientations only once
    UIDeviceOrientation orientation = UIDevice.currentDevice.orientation;
    if (orientation == UIDeviceOrientationPortrait) {
        if (portraitMinimumZoomScale == nil) {
            portraitMinimumZoomScale = [NSNumber numberWithFloat:scrollView.minimumZoomScale / aspectRatio];
            scrollView.minimumZoomScale = scrollView.minimumZoomScale / aspectRatio;
        }
        if (portraitMaximumZoomScale == nil) {
            portraitMaximumZoomScale = [NSNumber numberWithFloat:scrollView.maximumZoomScale / aspectRatio];
            scrollView.maximumZoomScale = scrollView.maximumZoomScale / aspectRatio;
        }
        if ([IPhoneXHelper isDeviceIPhoneX]) {
            scrollView.contentInset = UIEdgeInsetsZero;
        }
    }
    else if (orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight) {
        if (landscapeMinimumZoomScale == nil) {
            landscapeMinimumZoomScale = [NSNumber numberWithFloat:scrollView.minimumZoomScale * aspectRatio];
            scrollView.minimumZoomScale = scrollView.minimumZoomScale * aspectRatio;
        }
        if (landscapeMaximumZoomScale == nil) {
            landscapeMaximumZoomScale = [NSNumber numberWithFloat:scrollView.maximumZoomScale * aspectRatio];
            scrollView.maximumZoomScale = scrollView.maximumZoomScale * aspectRatio;
        }
        
        if ([IPhoneXHelper isDeviceIPhoneX] && orientation == UIDeviceOrientationLandscapeLeft) {
            scrollView.contentInset = UIEdgeInsetsMake(0, 0, 0, -[IPhoneXHelper sensorHousingHeight]);
        }
    }
    
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
    } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        if (!userDidZoom) {
            /* User did not zoom, reset scale to minimum.
             * Set the zoom scale only after rotation has completed to get the correct minimum zoom scale value.
             */
            [scrollView setZoomScale:scrollView.minimumZoomScale];
        }
    }];
}

- (void)scrollViewDidZoom:(UIScrollView *)scrollView
{
    /* This is only needed in iOS 8 where an issue with excessive contentSize occurs.
     * To catch the bug, update the value of the userDidZoom flag whenever the user zooms in or out.
     * This flag will then be used to determine whether the contentSize should be constrained when
     * the notification for contentSize change is received.
     * See implementation in observeValueForKeyPath:ofObject:change:context: for more details.
     */
    if (!isIOS9) {
        userDidZoom = scrollView.zoomScale > scrollView.minimumZoomScale;
    }
}

- (void)scrollViewDidEndZooming:(UIScrollView *)scrollView withView:(UIView *)view atScale:(CGFloat)scale
{
    userDidZoom = scale > scrollView.minimumZoomScale;
}

@end
