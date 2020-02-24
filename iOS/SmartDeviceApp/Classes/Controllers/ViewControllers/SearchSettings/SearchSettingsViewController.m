//
//  SearchSettingsViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SearchSettingsViewController.h"
#import "UIViewController+Segue.h"
#import "AppSettingsHelper.h"
#import "AlertHelper.h"
#import "ScreenLayoutHelper.h"


#define SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN   32

@interface SearchSettingsViewController ()

#pragma mark - UI Properties

/**
 * Reference to the textfield for the SNMP Community Name.
 */
@property (weak, nonatomic) IBOutlet UITextField *snmpCommunityName;

/**
 * Reference to the constraint of the content view for phones.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *phoneContentViewWidthConstraint;

/**
 * Flag that will be set to YES when the device is a tablet.
 */
@property (assign, nonatomic) BOOL isIpad;

#pragma mark - Internal Methods

/**
 * Sets-up this controller's properties and views.
 */
- (void)setupScreen;

/**
 * Closes the "Search Settings" screen.
 */
- (void)dismissScreen;

/**
 * Sets the properties of the SlidingViewController.
 */
- (void)initialize;

#pragma mark - IBAction Methods

/**
 * Responds to pressing the back (<) button in the header (for phones only).
 * Calls the {@link dismissScreen} method.
 *
 * @param sender the button object
 */
- (IBAction)onBack:(UIButton*)sender;

@end


@implementation SearchSettingsViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    self.slideDirection = SlideRight;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setupScreen];
}

#pragma mark - Screen Actions

- (void)setupScreen
{
    [self.snmpCommunityName setPlaceholder:NSLocalizedString(IDS_LBL_SNMP_COMMUNITY_NAME, "SNMP Community Name")];
    self.snmpCommunityName.text = [AppSettingsHelper getSNMPCommunityName];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.isIpad = YES;
    }
    else
    {
        self.isIpad = NO;
        self.phoneContentViewWidthConstraint.constant = [ScreenLayoutHelper getPortraitScreenWidth];
    }
}

- (void)dismissScreen
{
    if (self.isIpad)
        [self close];
    else
        [self unwindFromOverTo:[self.parentViewController class]];
}

- (IBAction)onBack:(UIButton *)sender
{
    [self dismissScreen];
}

#pragma mark - TextFields

- (void)dismissKeypad
{
    if (self.snmpCommunityName.isEditing)
    {
        [self.snmpCommunityName resignFirstResponder];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    [self dismissKeypad];
    
    if (textField.text.length > 0)
    {
        NSString *stringToSave = (textField.text.length > SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN) ? [textField.text substringToIndex:SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN] : textField.text;
        [AppSettingsHelper saveSNMPCommunityName:stringToSave];
        textField.text = stringToSave;
    }
    
    return YES;
}

/*Called when editing in a textfield ends*/
- (void)textFieldDidEndEditing:(UITextField *)textField
{
    if (textField.text.length > 0)
    {
        NSString *stringToSave = (textField.text.length > SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN) ? [textField.text substringToIndex:SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN] : textField.text;
        [AppSettingsHelper saveSNMPCommunityName:stringToSave];
    }
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSString *numbers = @"0123456789";
    NSString *alphabetsLowerCase = @"abcdefghijklmnopqrstuvwxyz";
    NSString *symbols = @",./:;@[\\]¥^_";
    NSString *validCharacterString = [NSString stringWithFormat: @"%@%@%@%@", numbers, alphabetsLowerCase, [alphabetsLowerCase uppercaseString], symbols];
    
    NSCharacterSet *validCharactersSet = [NSCharacterSet characterSetWithCharactersInString:validCharacterString];
    
    //ignore if:
    //-input has invalid characters
    //-input will exceed max chars
    if ([string stringByTrimmingCharactersInSet:validCharactersSet].length > 0)
    {
        //show error if pasted string has invalid characters
        //considered as paste if replacement string length is greater than 1 character
        //if 1 chracter only, it is considered as keyboard input
        if (string.length > 1)
        {
            [AlertHelper displayResult:kAlertResultErrCommunityNameInvalidPaste
                             withTitle:kAlertTitleSearchSettings
                             withDetails:nil];
            [textField resignFirstResponder];
        }
        return NO;
    }

    return YES;
}

@end
