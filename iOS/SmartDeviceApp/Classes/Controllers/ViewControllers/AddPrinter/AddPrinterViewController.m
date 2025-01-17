//
//  AddPrinterScreenController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AddPrinterViewController.h"
#import "PrinterDetails.h"
#import "PrinterManager.h"
#import "NetworkManager.h"
#import "AlertHelper.h"
#import "InputHelper.h"
#import "AppSettingsHelper.h"
#import "ScreenLayoutHelper.h"
#import "DeviceLockObserver.h"
#import "NotificationNames.h"

NSString *const BROADCAST_ADDRESS = @"255.255.255.255";

@interface AddPrinterViewController ()

#pragma mark - Data Properties

/**
 * Reference to the PrinterManager singleton.
 */
@property (strong, nonatomic) PrinterManager* printerManager;

/**
 * Flag that will be set to YES when a printer is successfully added.
 */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

#pragma mark - UI Properties

/**
 * Reference to the animated searching indicator.
 * This is displayed while the printer search is ongoing.
 */
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView* progressIndicator;

/** 
 * Reference to the textfield for the IP address.
 */
@property (weak, nonatomic) IBOutlet UITextField *textIP;

/**
 * Reference to the text display for the SNMP Community Name.
 */
@property (weak, nonatomic) IBOutlet UILabel *communityNameDisplay;

/**
 * Reference to the constraint between the header and top of content view
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *viewTopConstraint;

/** 
 * Reference to the save (+) button.
 */
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

/**
 * Reference to the constraint of the content view for phones.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *phoneContentViewWidthConstraint;

/**
 * Reference to the view containing the IP input
 */
@property (weak, nonatomic) IBOutlet UIView *inputView;

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
 * Closes the "Add Printer" screen.
 */
- (void)dismissScreen;

/**
 * Dismisses the keypad.
 */
- (void)dismissKeypad;

/**
 * Gets the input IP address then searches the network for the printer.
 * If the input IP address is invalid or if the device is not connected to a network,
 * then the search is not started and an error message is displayed instead.\n\n
 * The results of the search are handled in the PrinterSearchDelegate methods.
 */
- (void)savePrinter;

/**
 * Sets the properties of the SlidingViewController.
 */
- (void)initialize;

/**
 * Adds a full-capability printer.
 * This is called when the printer search has failed (printer was not
 * found or when the device is not connected to a network).
 * 
 * @param ipAddress the printer's IP address
 * @return YES if successful, NO otherwise
 */
- (BOOL)addFullCapabilityPrinter:(NSString *)ipAddress;

/**
 * Moves the view by up adjusting the top constraint to of the view that holds
 * the snmp community name display. This is to be able to lift textIP field
 * when it is covered by the keyboard
 *
 * @param offset
 */
- (void)moveViewUpWithOffset:(CGFloat)offset;

/**
 * Moves the view down to normal location
 */
- (void)moveViewDownToNormal;

/**
 * Responds to pressing the back (<) button in the header (for phones only).
 * Calls the {@link dismissScreen} method.
 * 
 * @param sender the button object
 */
- (IBAction)onBack:(UIButton*)sender;

/**
 * Responds to pressing the save (+) button.
 * Calls the {@link savePrinter} method.
 *
 * @param sender the button object
 */
- (IBAction)onSave:(UIButton*)sender;

@end

@implementation AddPrinterViewController

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
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector (keyboardDidShow:)
                                                 name: UIKeyboardDidShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector (keyboardDidHide:)
                                                 name: UIKeyboardDidHideNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector (deviceLockEventDidNotify)
                                                 name: NOTIF_DEVICE_LOCK object:nil];

    [[DeviceLockObserver sharedObserver] startObserver];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[DeviceLockObserver sharedObserver] stopObserver];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Screen Actions

- (void)setupScreen
{
    // setup properties
    self.printerManager = [PrinterManager sharedPrinterManager];
    self.printerManager.searchDelegate = self;
    self.hasAddedPrinters = NO;
    
    [self.progressIndicator setHidden:YES];
    [self.saveButton setHidden:NO];
    [self.textIP setEnabled:YES];
    [self.textIP setPlaceholder:NSLocalizedString(IDS_LBL_IP_ADDRESS, @"")];
    
    self.communityNameDisplay.text = [AppSettingsHelper getSNMPCommunityName];
    
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

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([self.progressIndicator isAnimating])
    {
        [self.progressIndicator stopAnimating];
#if DEBUG_LOG_ADD_PRINTER_SCREEN
        NSLog(@"[INFO][AddPrinter] canceling search");
#endif
        [self stopSearch];
    }
}

#pragma mark - Header

- (IBAction)onBack:(UIButton *)sender
{
    [self dismissScreen];
}

- (IBAction)onSave:(UIButton *)sender
{
    [self savePrinter];
}

- (BOOL)addFullCapabilityPrinter:(NSString *)ipAddress
{
    PrinterDetails *pd = [[PrinterDetails alloc] init];
    pd.ip = ipAddress;
    pd.port = [NSNumber numberWithInt:0];
    pd.enBookletFinishing = YES;
    pd.enStaple = YES;
    pd.enFinisher23Holes = NO;
    pd.enFinisher24Holes = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.isPrinterFound = NO;
    return [self.printerManager registerPrinter:pd];
}

- (void)savePrinter
{
    [self dismissKeypad];
    
    if (self.textIP.text == nil || [self.textIP.text length] == 0)
    {
        [AlertHelper displayResult:kAlertResultErrInvalidIP
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }
    
    NSString *formattedIP = self.textIP.text;
    bool isValid = [InputHelper isIPValid:&formattedIP];
    if (!isValid || [formattedIP isEqualToString:BROADCAST_ADDRESS])
    {
        [AlertHelper displayResult:kAlertResultErrInvalidIP
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }

    self.textIP.text = formattedIP;
    
    // was this printer already added before?
    if ([self.printerManager isIPAlreadyRegistered:formattedIP])
    {
        [AlertHelper displayResult:kAlertResultErrPrinterDuplicate
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }
    
    // can the device connect to the network?
    
    // for ORPHIS FW start
    /*
    if (![NetworkManager isConnectedToLocalWifi])
    {
        if([self addFullCapabilityPrinter:formattedIP])
        {
            // for ORPHIS FW start
            //self.hasAddedPrinters = YES;
            self.hasAddedPrinters = NO;
            // for ORPHIS FW end
            
            
            if (self.isIpad)
                [self.printersViewController reloadPrinters];
            
            [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self dismissScreen];
                    }];
            
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil];
        }
        
        return;
    }
    */


    if (![NetworkManager isConnectedToLocalWifi])
    {
            self.hasAddedPrinters = NO;
        
            if (self.isIpad)
                [self.printersViewController reloadPrinters];
            
            [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self dismissScreen];
                    }];
        return;
    }
    // for ORPHIS FW end

#if DEBUG_LOG_ADD_PRINTER_SCREEN
    NSLog(@"[INFO][AddPrinter] initiating search");
#endif
    [self.printerManager searchForPrinter:formattedIP];
    // callbacks for the search will be handled in delegate methods
    
    // if UI needs to do other things, do it here
    
    [self.progressIndicator startAnimating];
    [self.saveButton setHidden:YES];
    [self.textIP setEnabled:NO];
}

#pragma mark - PrinterSearchDelegate

- (void)printerSearchEndedwithResult:(BOOL)printerFound
{
    [self.progressIndicator stopAnimating];
    [self.saveButton setHidden:NO];
    [self.textIP setEnabled:YES];

// for ORPHIS FW start    
/*
    if (!printerFound)
    {
       NSString* trimmedIP = self.textIP.text;
        if([self addFullCapabilityPrinter:trimmedIP])
        {
            // for ORPHIS FW start
            //self.hasAddedPrinters = YES;
            self.hasAddedPrinters = NO;
            // for ORPHIS FW end
            
            if (self.isIpad)
                [self.printersViewController reloadPrinters];
            
            [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self dismissScreen];
                    }];
            
        }
        
        
        
        else
        {
            [AlertHelper displayResult:kAlertResultErrDB
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil];
        }
    }
*/
    if (!printerFound)
    {
            self.hasAddedPrinters = NO;
        
            if (self.isIpad)
                [self.printersViewController reloadPrinters];
            
            [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                             withTitle:kAlertTitlePrintersAdd
                           withDetails:nil
                    withDismissHandler:^(CXAlertView *alertView) {
                        [self dismissScreen];
                    }];
    }
// for ORPHIS FW end
}

- (void)printerSearchDidFoundNewPrinter:(PrinterDetails*)printerDetails
{
#if DEBUG_LOG_ADD_PRINTER_SCREEN
    NSLog(@"[INFO][AddPrinter] received NEW printer with IP=%@", printerDetails.ip);
    NSLog(@"[INFO][AddPrinter] updating UI");
#endif
    
    if ([self.printerManager registerPrinter:printerDetails])
    {
        self.hasAddedPrinters = YES;
        if (self.isIpad)
            [self.printersViewController reloadPrinters];
        
        [AlertHelper displayResult:kAlertResultInfoPrinterAdded
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self dismissScreen];
                }];
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDB
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
    }
}

#pragma mark - TextFields

- (void)dismissKeypad
{
    if (self.textIP.isEditing)
        [self.textIP resignFirstResponder];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    [self dismissKeypad];
    
    if (textField.text.length > 0)
    {
        [self savePrinter];
    }

    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSCharacterSet *validCharacters = [NSCharacterSet characterSetWithCharactersInString:@"0123456789abcdefABCDEF.:"];
    // ignore not valid characters
    if ([string stringByTrimmingCharactersInSet:validCharacters].length > 0)
    {
        return NO;
    }
    
    textField.text = [textField.text stringByReplacingCharactersInRange:range withString:string];
    return NO;
}

- (void)moveViewUpWithOffset:(CGFloat)offset
{
    self.viewTopConstraint.constant = -offset;
    [UIView animateWithDuration:0.1f delay:0.0f options:UIViewAnimationOptionCurveLinear animations: ^(void){
        [self.view layoutIfNeeded];
    }completion:nil];
}

- (void)moveViewDownToNormal
{
    self.viewTopConstraint.constant = 0;
    [UIView animateWithDuration:0.1f delay:0.0f options:UIViewAnimationOptionCurveLinear animations: ^(void){
        [self.view layoutIfNeeded];
    }completion:nil];
}

#pragma mark - Keyboard Notification Methods

- (void)keyboardDidShow:(NSNotification *)notif
{
    if(self.isIpad)
    {
        return;
    }
    
    // Get the rect of the keyboard.
    NSDictionary* info = [notif userInfo];
    NSValue* aValue = [info objectForKey:UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardRect = [aValue CGRectValue];
    CGRect keyboardFrameInInputView = [self.inputView convertRect:keyboardRect fromView:nil];
    
    CGFloat keyboardTopPosY = keyboardFrameInInputView.origin.y;
    CGFloat textViewBottomPosY = self.textIP.frame.origin.y + self.textIP.bounds.size.height;

    if (keyboardTopPosY < textViewBottomPosY)
    {
        [self moveViewUpWithOffset: (textViewBottomPosY - keyboardTopPosY) + 8.0f];
    }
}

- (void)keyboardDidHide:(NSNotification *)notif
{
    if(self.isIpad)
    {
        return;
    }
    
    //put the view back to normal when keypad is dismissed
    [self moveViewDownToNormal];
}

- (void)deviceLockEventDidNotify
{
    if ([self.progressIndicator isAnimating])
    {
        [self stopSearch];
        [self.progressIndicator stopAnimating];
        [self.saveButton setHidden:NO];
        [self.textIP setEnabled:YES];
    }
}

- (void)stopSearch
{
    NSOperatingSystemVersion iOS10 = (NSOperatingSystemVersion){10,0,0};
    BOOL isIOS10 = [[NSProcessInfo processInfo] isOperatingSystemAtLeastVersion:iOS10];
    if (isIOS10)
    {
       [self.printerManager stopSearching:YES];
    }
    else
    {
        [self.printerManager stopSearching:NO];
    }
}

@end
