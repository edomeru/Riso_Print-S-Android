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
#import "AppDelegate.h"
#import "IPhoneXHelper.h"

@interface RootViewController ()

@end

@implementation RootViewController

bool isIPhoneX;

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
    
    isIPhoneX = [IPhoneXHelper isDeviceIPhoneX];
    if (isIPhoneX) {
        // Subscribe to device orientation changes to show / hide the notch mask view for iPhone X
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationDidChange) name:UIDeviceOrientationDidChangeNotification object:nil];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    /*
     * Set the width of notch mask view for iPhone X based on the safe area insets.
     */
    if (@available(iOS 11, *)) {
        if (isIPhoneX) {
            self.leftNotchMaskWidthConstraint.constant = [IPhoneXHelper sensorHousingHeight];
            self.rightNotchMaskWidthConstraint.constant = [IPhoneXHelper sensorHousingHeight];
            
            [self adjustConstraints];
        }
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    /*
     * Set lastOrientation before the current instance of RootViewController disappears.
     * This is used as an alternative to UIDevice's orientation property which returns UIDeviceOrientationUnknown
     * when a new RootViewController is first pushed into the view hierarchy / when a PDF is loaded in the print preview screen
     */
    if (isIPhoneX) {
        AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        if (UIDevice.currentDevice.orientation == UIDeviceOrientationLandscapeLeft) {
            appDelegate.lastOrientation = UIDeviceOrientationLandscapeLeft;
        }
        if (UIDevice.currentDevice.orientation == UIDeviceOrientationLandscapeRight) {
            appDelegate.lastOrientation = UIDeviceOrientationLandscapeRight;
        }
        if (UIDevice.currentDevice.orientation == UIDeviceOrientationPortrait) {
            appDelegate.lastOrientation = UIDeviceOrientationPortrait;
        }
    }
}

- (void)orientationDidChange
{
    [self adjustConstraints];
}

- (void)adjustConstraints
{
    /*
     * While iOS 8 is still supported, the app cannot automatically employ the safe area layout guideline (introduced in iOS 11, supports up to iOS 9)
     * which prevents views from being obstructed by device-specific elements such as the iPhone X's sensor housing.
     * As a temporary workaround, an opaque black view whose width matches the safe area inset
     * is added on the left or right edge when the device is in landscape mode on an iPhone X.
     * The constraints of the main and sliding views will adjust based on this view if it is visible.
     * This does not affect other devices.
     */
    if (@available(iOS 11, *)) {
        if (isIPhoneX) {
            AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
            UIDeviceOrientation orientation = UIDevice.currentDevice.orientation;

            if (orientation == UIDeviceOrientationLandscapeLeft || ((appDelegate.lastOrientation == UIDeviceOrientationLandscapeLeft) && (orientation == UIDeviceOrientationUnknown))) {
                appDelegate.lastOrientation = UIDeviceOrientationLandscapeLeft;
                if (self.leftNotchMaskView.isHidden) {
                    [self.leftNotchMaskView setHidden:NO];
                    self.leftMainConstraint.constant += self.leftNotchMaskView.frame.size.width;
                    self.leftSlidingConstraint.constant += self.leftNotchMaskView.frame.size.width;
                }
                if (!self.rightNotchMaskView.isHidden) {
                    self.rightMainConstraint.constant -= self.rightNotchMaskView.frame.size.width;
                    self.rightSlidingConstraint.constant -= self.rightNotchMaskView.frame.size.width;
                    [self.rightNotchMaskView setHidden:YES];
                }
            }
            else if (orientation == UIDeviceOrientationLandscapeRight || ((appDelegate.lastOrientation == UIDeviceOrientationLandscapeRight) && (orientation == UIDeviceOrientationUnknown))) {
                appDelegate.lastOrientation = UIDeviceOrientationLandscapeRight;
                if (self.rightNotchMaskView.isHidden) {
                    [self.rightNotchMaskView setHidden:NO];
                    self.rightMainConstraint.constant += self.rightNotchMaskView.frame.size.width;
                    self.rightSlidingConstraint.constant += self.rightNotchMaskView.frame.size.width;
                }
                if (!self.leftNotchMaskView.isHidden) {
                    self.leftMainConstraint.constant -= self.leftNotchMaskView.frame.size.width;
                    self.leftSlidingConstraint.constant -= self.leftNotchMaskView.frame.size.width;
                    [self.leftNotchMaskView setHidden:YES];
                }
            }
            else if (orientation == UIDeviceOrientationPortrait || ((appDelegate.lastOrientation == UIDeviceOrientationPortrait) && (orientation == UIDeviceOrientationUnknown))) {
                appDelegate.lastOrientation = UIDeviceOrientationPortrait;
                if (!self.leftNotchMaskView.isHidden) {
                    self.leftMainConstraint.constant -= self.leftNotchMaskView.frame.size.width;
                    self.leftSlidingConstraint.constant -= self.leftNotchMaskView.frame.size.width;
                    [self.leftNotchMaskView setHidden:YES];
                }
                if (!self.rightNotchMaskView.isHidden) {
                    self.rightMainConstraint.constant -= self.rightNotchMaskView.frame.size.width;
                    self.rightSlidingConstraint.constant -= self.rightNotchMaskView.frame.size.width;
                    [self.rightNotchMaskView setHidden:YES];
                }
            }
        }
    }
}

- (void)dealloc
{
    if (isIPhoneX) {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
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

- (void)unwindForSegue:(UIStoryboardSegue *)unwindSegue towardsViewController:(UIViewController *)toViewController
{
    NSString *identifier = unwindSegue.identifier;
    UIViewController *fromViewController = unwindSegue.sourceViewController;

    UIStoryboardSegue *segue = [self segueFromViewController:fromViewController toViewController:toViewController withIdentifier:identifier];
    [segue perform];
}

- (UIStoryboardSegue *)segueForUnwindingToViewController:(UIViewController *)toViewController fromViewController:(UIViewController *)fromViewController identifier:(NSString *)identifier
{
    /**
     * This function has been deprecated starting with iOS 9. For backwards compatibility, it will be retained while iOS 8 is still supported by the application.
     * For iOS 9 and above, it is recommended to use unwindForSegue:towardsViewController.
     * However, when a class overrides both functions, only segueForUnwindingToViewController:fromViewController:identifier is called regardless of iOS version.
     * As a workaround while iOS 8 is still supported, if the SDK used is iOS 9 or above, processing will be passed to the recommended unwindForSegue:towardsViewController by calling it from this function.
     */
    if ([self respondsToSelector:@selector(unwindForSegue:towardsViewController:)]) {
        UIStoryboardSegue *segue = [[UIStoryboardSegue alloc] initWithIdentifier:identifier source:fromViewController destination:toViewController];
        [self unwindForSegue:segue towardsViewController:toViewController];
        return [super segueForUnwindingToViewController:toViewController fromViewController:fromViewController identifier:identifier];
    }
    
    return [self segueFromViewController:fromViewController toViewController:toViewController withIdentifier:identifier];
}

- (UIStoryboardSegue *)segueFromViewController:(UIViewController *)fromViewController toViewController:(UIViewController *)toViewController withIdentifier:(NSString *)identifier
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
        return segue;
    }
    
    return nil;
}

@end
