//
//  SettingsViewController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SettingsViewController.h"
#import "HomeViewController.h"
#import "UIViewController+Segue.h"
#import "DefaultView.h"
#import "PListHelper.h"
#import "SettingsValidationHelper.h"


#define KEY_SETTINGS_CARD_READER_ID     @"CardReaderID"
#define KEY_SETTINGS_COMMUNITY_NAME     @"CommunityName"

@interface SettingsViewController ()
@property (weak, nonatomic) IBOutlet UITextField *cardId; /**< Reference outlet to Card ID input textfield*/
@property (weak, nonatomic) IBOutlet UITextField *communityName; /**< Reference outlet to Community Name input textfield*/
@property (weak, nonatomic) IBOutlet UIButton *mainMenuButton; /**< Reference outlet to Main Menu button*/
@property (weak, nonatomic) IBOutlet UIView *contentView; /**< Reference outlet to view that contains the settings inputs fields*/
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *contentViewWidthConstraint;  /**< Reference outlet to the width constraint of the contentView*/
@property (weak, nonatomic) IBOutlet UIView *communityNameView;  /**< Reference outlet to view that contains the community view input field*/
@property (strong, nonatomic) NSDictionary* settings;  /**< Dictionary that contains the values of the settings*/
@property (strong, nonatomic) UIAlertView *errorAlert; /**< Alert to use when to notify error in settings input*/
/**
 Processes the cardId textfield input after editing
 @param inputString The current string in the cardId textfield
 @return YES if valid; NO otherwise
 */
- (void) cardIDDidEndEditing:(NSString *)inputString;

/**
 Processes the communityName textfield input after editing
 @param inputString The current string in the communityName textfield
 */
- (void) communityNameDidEndEditing:(NSString *)inputString;

/**
 Validates the current string in the communityName textfield
 @param inputString The current string in the communityName textfield
 @return kSettingsInputError
 */
- (void)showValidationError:(kSettingsInputError)error;

/**
 Selector method that receives notification when keyboard is shown
 Temporarily adjusts the contentView position when textfield to edit is covered by keyboard
 @param notif The notification received
 */
- (void)didKeyboardShow:(NSNotification *)notif;

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
    
    //init text fields from values in plist
    //get current value of settings in plist
    self.settings = [[PListHelper readApplicationSettings] mutableCopy];
    self.cardId.text = (NSString *)[self.settings objectForKey:KEY_SETTINGS_CARD_READER_ID];
    self.cardId.delegate = self;
    self.communityName.text = (NSString *)[self.settings objectForKey:KEY_SETTINGS_COMMUNITY_NAME];
    self.communityName.delegate =self;
    
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
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didKeyboardShow:) name:UIKeyboardDidShowNotification object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Keyboard notification selector
- (void)didKeyboardShow:(NSNotification *)notif
{
    if([self.communityName isFirstResponder] == NO)
    {
        return;
    }
    
    NSDictionary *kbInfo = [notif userInfo];
    CGSize keyboardSize = [[kbInfo objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    CGRect visibleRect = self.contentView.frame;

    if(UIInterfaceOrientationIsLandscape(self.interfaceOrientation) == YES)
    {
        visibleRect.size.height -= keyboardSize.width;
    }
    else
    {
        visibleRect.size.height -= keyboardSize.height;
    }
    
    CGRect convertedTextRect = [self.view convertRect:self.communityName.frame fromView:self.communityNameView];
    
    //handle when keyboard hides community name text field, offset the view to put text field above keyboard view
    if(CGRectContainsPoint(visibleRect, convertedTextRect.origin) == NO)
    {
        self.contentView.frame = CGRectOffset(self.contentView.frame, 0, -100);
        isContentOffset = YES;
    }
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
    if([textField isEqual:self.communityName])
    {
        return [SettingsValidationHelper shouldAcceptCommunityNameInput:string];
    }
    return[SettingsValidationHelper shouldAcceptCardIDInput:string];
}

/*Called when editing in a textfield ends*/
- (void)textFieldDidEndEditing:(UITextField *)textField
{
    //auto save after user finished editing text field
    if([textField isEqual:self.communityName])
    {
        [self communityNameDidEndEditing:textField.text];
        return;
    }

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
- (void)communityNameDidEndEditing:(NSString *)inputString
{
    kSettingsInputError validationError = [SettingsValidationHelper validateCommunityNameInput:inputString];
    if(validationError == kSettingsInputErrorNone)
    {
        [self.settings setValue:inputString forKey:KEY_SETTINGS_COMMUNITY_NAME];
        [PListHelper setApplicationSettings:self.settings];
    }
    else
    {
        self.communityName.text = (NSString *)[self.settings objectForKey:KEY_SETTINGS_COMMUNITY_NAME];
        [self showValidationError:validationError];
    }
}

- (void)cardIDDidEndEditing:(NSString *)inputString
{
    kSettingsInputError validationError = [SettingsValidationHelper validateCardIDInput:inputString];
    if(validationError == kSettingsInputErrorNone)
    {
        [self.settings setValue:inputString forKey:KEY_SETTINGS_CARD_READER_ID];
        [PListHelper setApplicationSettings:self.settings];
    }
    else
    {
        self.cardId.text = (NSString *)[self.settings objectForKey:KEY_SETTINGS_CARD_READER_ID];
        [self showValidationError:validationError];
    }
}

- (void)showValidationError:(kSettingsInputError)error
{
    self.errorAlert.message = [SettingsValidationHelper errorMessageForSettingsInputError:error];
    [self.errorAlert show];
}

@end
