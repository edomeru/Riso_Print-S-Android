//
//  SettingsViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SettingsViewController.h"
#import "HomeViewController.h"
#import "UIViewController+Segue.h"
#import "DefaultView.h"
#import "PListHelper.h"
#import "SettingsValidationHelper.h"
#import "AppSettings.h"

@interface SettingsViewController ()

/** Reference outlet to Login ID input textfield */
@property (weak, nonatomic) IBOutlet UITextField *loginId;

/** Reference outlet to Main Menu button */
@property (weak, nonatomic) IBOutlet UIButton *mainMenuButton;

/** Reference outlet to view that contains the settings inputs fields */
@property (weak, nonatomic) IBOutlet UIView *contentView;

/** Reference outlet to the width constraint of the contentView */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *contentViewWidthConstraint;

/**
 Main menu button action
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 Action after menu screen unwinds to settings screen
 */
- (IBAction)unwindToSettings:(UIStoryboardSegue *)sender;

@end

@implementation SettingsViewController
{
    BOOL isContentOffset; /** flag to indicate that content view is currently offset*/
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
    
    // Set placeholder
    self.loginId.placeholder = NSLocalizedString(IDS_LBL_LOGIN_ID, @"");
    
    //init text fields from values in plist
    //get current value of settings user defaults
    NSUserDefaults *appSettings = [NSUserDefaults standardUserDefaults];
    NSString *loginId = [appSettings objectForKey:KEY_APPSETTINGS_LOGIN_ID];
    if(loginId == nil)
    {
        loginId = @"";
        [appSettings setObject:loginId forKey:KEY_APPSETTINGS_LOGIN_ID];
    }
    
    self.loginId.text = loginId;
    self.loginId.delegate = self;
    
    isContentOffset = NO;//set flag to indicate that content view is offset
    //Content occupies full width of screen only in iPhone Portrait.
    //Otherwise it is centered horizontally centered in view.
    
    //If iPad, make the content view wider
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.contentViewWidthConstraint.constant += 200;
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
    [self.mainMenuButton setEnabled:NO];
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)unwindToSettings:(UIStoryboardSegue *)sender
{
    [self.mainMenuButton setEnabled:YES];
}

#pragma mark - UITextFieldDelegate methods

/*Checks the keyboard input if should be accepted in textfield*/
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if((textField.text.length - range.length) == LOGIN_ID_MAX_INPUT)
    {
        return NO;
    }
    
    NSMutableString *newString = [NSMutableString stringWithString:textField.text];
    BOOL shouldAccept = YES;
    if([SettingsValidationHelper validateLoginIDInput:string] == kSettingsInputErrorNone)
    {
        [newString deleteCharactersInRange:range];
        if(newString.length + string.length > LOGIN_ID_MAX_INPUT)
        {
            [newString insertString:[string substringToIndex:LOGIN_ID_MAX_INPUT - newString.length]
                            atIndex:range.location];
            textField.text = newString;
            shouldAccept = NO;
        }
    }
    else
    {
        shouldAccept = NO;
    }
    return shouldAccept;
}

/*Called when editing in a textfield ends*/
- (void)textFieldDidEndEditing:(UITextField *)textField
{
    // validation is done during text input
    // save the input login ID
    NSUserDefaults *appSettings = [NSUserDefaults standardUserDefaults];
    [appSettings setValue:textField.text forKey:KEY_APPSETTINGS_LOGIN_ID];
}

/*Called when the Done button is pressed in keyboard*/
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    //if view is currently offset due to keyboard covering textfield,
    //remove view offset when keyboard is removed from view
    if(isContentOffset == YES)
    {
        self.contentView.frame = CGRectOffset(self.contentView.frame, 0, 100);
        isContentOffset = NO;
    }
    
    [textField resignFirstResponder];
    return YES;
}

@end
