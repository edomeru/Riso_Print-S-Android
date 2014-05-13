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


#define KEY_SETTINGS_CARD_READER_ID     @"SDA_CardReaderID"

@interface SettingsViewController ()
@property (weak, nonatomic) IBOutlet UITextField *cardId; /**< Reference outlet to Card ID input textfield*/
@property (weak, nonatomic) IBOutlet UIButton *mainMenuButton; /**< Reference outlet to Main Menu button*/
@property (weak, nonatomic) IBOutlet UIView *contentView; /**< Reference outlet to view that contains the settings inputs fields*/
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *contentViewWidthConstraint;  /**< Reference outlet to the width constraint of the contentView*/

@property (strong, nonatomic) UIAlertView *errorAlert; /**< Alert to use when to notify error in settings input*/
/**
 Processes the cardId textfield input after editing
 @param inputString The current string in the cardId textfield
 @return YES if valid; NO otherwise
 */
- (void) cardIDDidEndEditing:(NSString *)inputString;
/**
 Validates the current string in the communityName textfield
 @param inputString The current string in the communityName textfield
 @return kSettingsInputError
 */
- (void)showValidationError:(kSettingsInputError)error;

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
    self.cardId.placeholder = NSLocalizedString(IDS_LBL_LOGIN_ID, @"");
    
    //init text fields from values in plist
    //get current value of settings user defaults
    NSUserDefaults *appSettings = [NSUserDefaults standardUserDefaults];
    NSString *cardId = [appSettings objectForKey:KEY_SETTINGS_CARD_READER_ID];
    if(cardId == nil)
    {
        cardId = @"";
        [appSettings setObject:cardId forKey:KEY_SETTINGS_CARD_READER_ID];
    }
    
    self.cardId.text = cardId;
    self.cardId.delegate = self;

    self.errorAlert = [[UIAlertView alloc] initWithTitle:@"Error"
                                            message:@""
                                           delegate:nil
                                  cancelButtonTitle:@"OK"
                                  otherButtonTitles:nil];
    
    isContentOffset = NO;//set flag to indicate that content view is offset
    
    //content occupies full width of screen only in iPhone Portrait. Otherwise it is centered horizontally centered in view
    //if iPad, make the content view wider
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
    if((textField.text.length - range.length) == CARD_ID_MAX_INPUT)
    {
        return NO;
    }
    
    NSMutableString *newString = [NSMutableString stringWithString:textField.text];
    BOOL shouldAccept = YES;
    if([SettingsValidationHelper validateCardIDInput:string] == kSettingsInputErrorNone)
    {
        [newString deleteCharactersInRange:range];
        if(newString.length + string.length > CARD_ID_MAX_INPUT)
        {
            [newString insertString:[string substringToIndex:CARD_ID_MAX_INPUT - newString.length] atIndex:range.location];
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
    //auto save after user finished editing text field
    [self cardIDDidEndEditing:textField.text];
}

/*Called when the Done button is pressed in keyboard*/
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    //if view is currently offset due to keyboard covering textfield, remove view offset when keyboard is removed from view
    if(isContentOffset == YES)
    {
        NSLog(@"offset");
        self.contentView.frame = CGRectOffset(self.contentView.frame, 0, 100);
        isContentOffset = NO;
    }
    
    [textField resignFirstResponder];
    return YES;
}

#pragma mark  Class Methods

- (void)cardIDDidEndEditing:(NSString *)inputString
{
    kSettingsInputError validationError = [SettingsValidationHelper validateCardIDInput:inputString];
    NSUserDefaults  *appSettings = [NSUserDefaults standardUserDefaults];
    if(validationError == kSettingsInputErrorNone)
    {
        [appSettings setValue:inputString forKey:KEY_SETTINGS_CARD_READER_ID];
    }
    else
    {
        self.cardId.text = (NSString *)[appSettings objectForKey:KEY_SETTINGS_CARD_READER_ID];
        [self showValidationError:validationError];
    }
}

- (void)showValidationError:(kSettingsInputError)error
{
    self.errorAlert.message = [SettingsValidationHelper errorMessageForSettingsInputError:error];
    [self.errorAlert show];
}

@end
