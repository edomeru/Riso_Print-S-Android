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
#import "AppSettings.h"
#import "ScreenLayoutHelper.h"

#define LOGIN_ID_MAX_INPUT 20

@interface SettingsViewController ()

/**
 * Reference to the textfield for the Login ID.
 */
@property (weak, nonatomic) IBOutlet UITextField *loginId;

/**
 * Reference to the main menu button on the header.
 */
@property (weak, nonatomic) IBOutlet UIButton *mainMenuButton;

/** 
 * Reference to the container view for the text fields.
 */
@property (weak, nonatomic) IBOutlet UIView *contentView;

/** 
 * Reference to the width constraint of {@link contentView}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *contentViewWidthConstraint;

/**
 * Responds to pressing the main menu button in the header.
 * Displays the Main Menu panel.
 *
 * @param sender the button object
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 * Unwind segue back to the "Settings" screen.
 * Called when transitioning back to the "Settings"
 * screen from the the Main Menu panel.
 *
 * @param sender the segue object
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
    self.loginId.placeholder = NSLocalizedString(IDS_LBL_OWNER_NAME, @"");
    
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
    //Otherwise it is centered horizontally centered in view
    
    
    //If iPad, make the content view wider
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.contentViewWidthConstraint.constant += 200;
    }
    else
    {
        self.contentViewWidthConstraint.constant = [ScreenLayoutHelper getPortraitScreenWidth];
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

/*Called when editing in a textfield ends*/
- (void)textFieldDidEndEditing:(UITextField *)textField
{
    //check if input length is greater than the max input
    //if yes, truncate
    if(textField.text.length > LOGIN_ID_MAX_INPUT)
    {
        NSMutableString *newString = [NSMutableString stringWithString:textField.text];
        [newString deleteCharactersInRange:NSMakeRange(LOGIN_ID_MAX_INPUT, newString.length - LOGIN_ID_MAX_INPUT)];
        textField.text = newString;
    }
    
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
