//
//  LicenseAgreementViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "UIViewController+Segue.h"
#import "LicenseAgreementViewController.h"
#import "PrintPreviewViewController.h"
#import "AlertHelper.h"
#import "IPhoneXHelper.h"
#import <WebKit/WKWebView.h>
#import <sys/utsname.h>
#import "HTMLHelper.h"

#define LICENCE_AGREEMENT_KEY @"license_agreement"
#define LICENSE_HTML @"license"
@interface LicenseAgreementViewController()<UIWebViewDelegate, UIScrollViewDelegate>

/**
 * Reference to the view for displaying the License agreement content.
 * This is only used in iOS 8.
 */
@property (weak, nonatomic) IBOutlet UIWebView *contentWebView;

/**
 * Reference to the view for displaying the License agreement content.
 * This is only used in iOS 9 and above.
 */
@property (strong, nonatomic) WKWebView *wkWebView;

/**
 * Reference to the view for displaying the page's title
 * and main menu button
 */
@property (weak, nonatomic) IBOutlet UIView *headerView;


/**
 * Reference to the disagree button
 */
@property (weak, nonatomic) IBOutlet UIButton *cancelBtn;

/**
 * Reference to the agree button
 */
@property (weak, nonatomic) IBOutlet UIButton *okBtn;

/**
 * sets localization and webpage content
 */
-(void) setup;

/**
 * sets the license agreement flag to YES
 */
-(void) setLicenseAgreement:(BOOL)doesAgree;

/**
 * Responds to pressing the "Disagree" button
 * Displays alert popup with error msg
 *
 * @param sender the button object
 */

-(IBAction) cancelAction:(id)sender;

/**
 * Responds to pressing the "Agree" button.
 * Displays Print Preview view controller
 *
 * @param sender the button object
 */

-(IBAction) okAction:(id)sender;

@end

@implementation LicenseAgreementViewController {
    bool isIOS9;
    bool isIphonePlus;
    bool userDidZoom;
    NSNumber * _Nullable portraitMinimumZoomScale;
    NSNumber * _Nullable portraitMaximumZoomScale;
    NSNumber * _Nullable landscapeMinimumZoomScale;
    NSNumber * _Nullable landscapeMaximumZoomScale;
    UIDeviceOrientation lastOrientation;
}


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
       [self setup];
    }
    return self;
}

-(void)viewDidLoad
{
    [super viewDidLoad];

    if (@available(iOS 13.0, *)) {
        self.view.backgroundColor = [UIColor colorNamed:@"color_white_gray6"];
    }

    // Temporarily points to legal page
    [self setup];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    if (isIOS9) {
        self.wkWebView.scrollView.delegate = nil;
    } else {
        [self.contentWebView.scrollView removeObserver:self forKeyPath:@"contentSize"];
        self.contentWebView.scrollView.delegate = nil;
    }
}

- (void)traitCollectionDidChange:(UITraitCollection *)previousTraitCollection
{
    [super traitCollectionDidChange:previousTraitCollection];

    if (@available(iOS 13.0, *)) {
        if (previousTraitCollection.userInterfaceStyle != self.traitCollection.userInterfaceStyle) {
            if (isIOS9) {
                [HTMLHelper loadHTML:LICENSE_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:NO];
            } else {
                [HTMLHelper loadHTML:LICENSE_HTML toWebView:self.contentWebView];
            }
        }
    }
}

-(void) setup
{
    /*
     * BUG #7039: Layout bug occurring only on iPhone Plus devices.
     * Fix: Check if device is iPhone plus and compute aspect ratio using self.view
     */
    isIphonePlus = [self isIphonePlusDevice];

    /*
     * WKWebView has an issue in iOS 8 when loading local assets referenced in HTML.
     * For iOS 8, load the HTML in UIWebView instead.
     */
    NSOperatingSystemVersion iOS9 = (NSOperatingSystemVersion){9,0,0};
    isIOS9 = [[NSProcessInfo processInfo] isOperatingSystemAtLeastVersion:iOS9];
    if (isIOS9) {
        [self.contentWebView setHidden:YES];
        [self setupWkWebView];
        [HTMLHelper loadHTML:LICENSE_HTML toWebView:self.wkWebView withTrait:self.traitCollection andHelpHTML:NO];
    }
    else {
        self.contentWebView.delegate = self;
        self.contentWebView.scrollView.delegate = self;
        [self.contentWebView.scrollView addObserver:self forKeyPath:@"contentSize" options:0 context:nil];
        [HTMLHelper loadHTML:LICENSE_HTML toWebView:self.contentWebView];
    }
    
    [self.okBtn     setTitle:NSLocalizedString(@"IDS_LBL_AGREE", @"") forState:UIControlStateNormal];
    [self.cancelBtn setTitle:NSLocalizedString(@"IDS_LBL_DISAGREE", @"") forState:UIControlStateNormal];
    
}

#pragma mark - IBActions
-(IBAction) okAction:(id)sender
{
    [self setLicenseAgreement:YES];
    [self performSegueTo:[PrintPreviewViewController class]];

}

-(IBAction) cancelAction:(id)sender
{
    [AlertHelper displayResult:kAlertResultErrorLicenseAgreementDisagree
                     withTitle:kAlertTitleLicenseAgreement
                   withDetails:nil
           ];
}

#pragma mark - public functions 

+(BOOL) hasConfirmedToLicenseAgreement
{
    
    NSUserDefaults *defaultSettings = [NSUserDefaults standardUserDefaults];
    return [defaultSettings boolForKey:LICENCE_AGREEMENT_KEY];
    
}


#pragma mark - helper functions
-(void) setLicenseAgreement:(BOOL) doesAgree
{
    NSUserDefaults *defaultSettings = [NSUserDefaults standardUserDefaults];
    [defaultSettings setBool:doesAgree forKey:LICENCE_AGREEMENT_KEY];
    [defaultSettings synchronize];
}

-(BOOL) isIphonePlusDevice
{
    // iPhone 6 Plus, iPhone 6S Plus, iPhone 7 Plus (CDMA), iPhone 7 Plus (GSM), iPhone 8 Plus (CDMA), iPhone 8 Plus (GSM)
    NSArray *iphonePlusDevices = [NSArray arrayWithObjects:@"iPhone7,1", @"iPhone8,2", @"iPhone9,2", @"iPhone9,4", @"iPhone10,2", @"iPhone10,5", nil];
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *device = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    return [iphonePlusDevices containsObject:device];
}

- (void)setupWkWebView
{
    self.wkWebView = [[WKWebView alloc] init];
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
    NSLayoutConstraint *bottomConstraint = [NSLayoutConstraint constraintWithItem:self.wkWebView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.cancelBtn attribute:NSLayoutAttributeTop multiplier:1.0f constant:0.0f];

    [self.view addConstraints:@[leadingConstraint, trailingConstraint, topConstraint, bottomConstraint]];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSURL *licenseAgreementUrl = [[NSBundle mainBundle] URLForResource:LICENSE_HTML withExtension:@"html"];
    [webView loadRequest: [NSURLRequest requestWithURL:licenseAgreementUrl]];
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    CGFloat aspectRatio;
    UIScrollView *scrollView;
    if (isIOS9) {
        if (isIphonePlus) {
            // use screen view in computing aspect ratio
            aspectRatio = self.view.frame.size.width / self.view.frame.size.height;
        } else {
            aspectRatio = self.wkWebView.frame.size.width / self.wkWebView.frame.size.height;
        }
        scrollView = self.wkWebView.scrollView;
    }
    else {
        if (isIphonePlus) {
            // use screen view in computing aspect ratio
            aspectRatio = self.view.frame.size.width / self.view.frame.size.height;
        } else {
            aspectRatio = self.contentWebView.frame.size.width / self.contentWebView.frame.size.height;
        }
        scrollView = self.contentWebView.scrollView;
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
        if (self->isIphonePlus && (orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight) && (self->lastOrientation == UIDeviceOrientationLandscapeLeft || self->lastOrientation == UIDeviceOrientationLandscapeRight)) {
            // do not rescale if landscape to landscape rotation
            self->lastOrientation = orientation;
        } else {
            if (!self->userDidZoom) {
                /* Check if device is iPhone Plus and if minimum zoom scale is greater than previously set minimum zoom scale for certain orientation.
                 * Reset the minimum and maximum zoom scale for scrollview.
                 */
                if (self->isIphonePlus && orientation == UIDeviceOrientationPortrait && scrollView.minimumZoomScale != self->portraitMinimumZoomScale.floatValue) {
                    [scrollView setMinimumZoomScale:self->portraitMinimumZoomScale.floatValue];
                    [scrollView setMaximumZoomScale:self->portraitMaximumZoomScale.floatValue];
                } else if (self->isIphonePlus && (orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight) && scrollView.minimumZoomScale != self->landscapeMinimumZoomScale.floatValue) {
                    [scrollView setMinimumZoomScale:self->landscapeMinimumZoomScale.floatValue];
                    [scrollView setMaximumZoomScale:self->landscapeMaximumZoomScale.floatValue];
                }
                /* User did not zoom, reset scale to minimum.
                 * Set the zoom scale only after rotation has completed to get the correct minimum zoom scale value.
                 */
                [scrollView setZoomScale:scrollView.minimumZoomScale];
            }
            self->lastOrientation = orientation;
        }
    }];
}

#pragma mark - Scrollview zoom scale handling

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context
{
    if (object == self.contentWebView.scrollView) {
        if ([keyPath isEqualToString:@"contentSize"]) {
            /* An issue occurs in UIWebView where the contentSize becomes excessive when rotating from landscape to portrait.
             * This happens when the user previously changed the zoom scale, went back to normal scale, then rotated the device.
             * To catch this case, check if the contentSize width is wider than the frame of the web view even though the user
             * did not zoom in.
             * If the user did initiate the zoom (and consequently the change in contentSize), do not constrain the
             * contentSize because we need to be able to scroll.
             * Otherwise, set the contentSize width equal to the width of the webview.
             */
            if ((self.contentWebView.scrollView.contentSize.width > self.contentWebView.frame.size.width) && !userDidZoom) {
                [self.contentWebView.scrollView setContentSize:CGSizeMake(self.contentWebView.frame.size.width, self.contentWebView.scrollView.contentSize.height)];
            }
        }
    }
}

#pragma mark - Scrollview zoom scale handling

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
