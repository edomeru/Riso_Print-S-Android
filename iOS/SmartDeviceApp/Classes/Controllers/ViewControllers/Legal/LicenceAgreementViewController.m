//
//  LicenceAgreementViewController.m
//  RISOSmartPrint
//
//  Created by Gino on 2/4/15.
//  Copyright (c) 2015 aLink. All rights reserved.
//

#import "LicenceAgreementViewController.h"
#import "PrintPreviewViewController.h"
#import "AlertHelper.h"
#define LICENCE_AGREEMENT_KEY @"licence_agreement"

@interface LicenceAgreementViewController()

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

@implementation LicenceAgreementViewController


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
    NSURL *licenseAgreementUrl = [[NSBundle mainBundle] URLForResource:@"legal" withExtension:@"html"];
    [self.contentWebView loadRequest:[NSURLRequest requestWithURL:licenseAgreementUrl]];
    
    [self.okBtn     setTitle:NSLocalizedString(@"IDS_LBL_AGREE_TO_LICENSE", @"") forState:UIControlStateNormal];
    [self.cancelBtn setTitle:NSLocalizedString(@"IDS_LBL_DISAGREE_TO_LICENSE", @"") forState:UIControlStateNormal];
    
}

-(IBAction) okAction:(id)sender
{
    [self setLicenseAgreement:YES];
    [self performSegueWithIdentifier:@"LicenseAgreement_PrintPreview" sender:self];

}

-(IBAction) cancelAction:(id)sender
{
    [AlertHelper displayResult:kAlertResultErrorLicenseAgreementDisagree
                     withTitle:kAlertTitleDefault
                   withDetails:nil
           ];
}

+(BOOL) hasConfirmedToLicenseAgreement
{
    
    NSUserDefaults *defaultSettings = [NSUserDefaults standardUserDefaults];
    return [defaultSettings boolForKey:LICENCE_AGREEMENT_KEY];
    
}

-(void) setLicenseAgreement:(BOOL) doesAgree
{
    NSUserDefaults *defaultSettings = [NSUserDefaults standardUserDefaults];
    [defaultSettings setBool:doesAgree forKey:LICENCE_AGREEMENT_KEY];
    [defaultSettings synchronize];
}


@end
