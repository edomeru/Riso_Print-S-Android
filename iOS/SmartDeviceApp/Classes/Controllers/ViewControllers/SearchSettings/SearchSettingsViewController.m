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


#define SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN   32

@interface SearchSettingsViewController ()

#pragma mark - UI Properties

/**
 * Reference to the textfield for the SNMP Community Name.
 */
@property (weak, nonatomic) IBOutlet UITextField *snmpCommunityName;

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
    self.snmpCommunityName.text = [AppSettingsHelper getSNMPCommunityName];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        self.isIpad = YES;
    else
        self.isIpad = NO;
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
        [AppSettingsHelper saveSNMPCommunityName:textField.text];
    }
    
    return YES;
}

/*Called when editing in a textfield ends*/
- (void)textFieldDidEndEditing:(UITextField *)textField
{
    if (textField.text.length > 0)
    {
        [AppSettingsHelper saveSNMPCommunityName:textField.text];
    }
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSMutableCharacterSet *validCharacters = [NSMutableCharacterSet alphanumericCharacterSet];
    [validCharacters addCharactersInString:@",./:;@[\\]^_"];
    
    //ignore if:
    //-input has invalid characters
    //-input will exceed max chars
    if ([string stringByTrimmingCharactersInSet:validCharacters].length > 0 ||
       ((textField.text.length + string.length) > SEARCHSETTINGS_COMMUNITY_NAME_MAX_LEN && string.length > 0))
    {
        return NO;
    }
    
    return YES;
}

@end
