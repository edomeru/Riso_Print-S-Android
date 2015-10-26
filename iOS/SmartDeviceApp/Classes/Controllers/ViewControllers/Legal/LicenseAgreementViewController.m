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

#define LICENCE_AGREEMENT_KEY @"license_agreement"
#define LICENSE_HTML @"license"
@interface LicenseAgreementViewController()

/**
 * Reference to the view for displaying the License agreement content.
 */
@property (weak, nonatomic) IBOutlet UIWebView *contentWebView;

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

@implementation LicenseAgreementViewController


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
    // Temporarily points to legal page
    [self setup];
}

-(void) setup
{
    NSURL *licenseAgreementUrl = [[NSBundle mainBundle] URLForResource:LICENSE_HTML withExtension:@"html"];
    [self.contentWebView loadRequest:[NSURLRequest requestWithURL:licenseAgreementUrl]];
    
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


@end
