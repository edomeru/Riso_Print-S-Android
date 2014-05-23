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

#define TAG_TEXT_IP         0
#define TAG_TEXT_USERNAME   1
#define TAG_TEXT_PASSWORD   2

@interface AddPrinterViewController ()

#pragma mark - Data Properties

/** Handler for the Printer data. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UIActivityIndicatorView* progressIndicator;

/** Input TextField for the IP Address. */
@property (weak, nonatomic) IBOutlet UITextField *textIP;

/** Save Button in the Header. */
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

@property (assign, nonatomic) BOOL isIpad;

#pragma mark - Internal Methods

/**
 Called when screen loads.
 Sets-up this controller's properties and views.
 */
- (void)setupScreen;

/**
 Called to close the Add Printer screen.
 */
- (void)dismissScreen;

/**
 Tells the currently active TextField to close the keypad/numpad.
 */
- (void)dismissKeypad;

- (void)savePrinter;

/**
 Adds a full-capability printer (for failed manual snmp search)
 */
- (void)addFullCapabilityPrinter:(NSString *)ipAddress;

/**
 Unwinds back to the Printers screen.
 Cancels any ongoing search operation.
 This is for the iPhone only.
 */
- (IBAction)onBack:(UIButton*)sender;

/**
 The Printer IP and other details are retrieved from the UI, then
 the printer is searched from the network. If it is available, the
 Printer object is created and stored in the DB.
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
    [self.saveButton setEnabled:NO];
    [self.textIP setEnabled:YES];
    
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

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([self.progressIndicator isAnimating])
    {
        [self.progressIndicator stopAnimating];
#if DEBUG_LOG_ADD_PRINTER_SCREEN
        NSLog(@"[INFO][AddPrinter] canceling search");
#endif
        [self.printerManager stopSearching];
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

- (void)addFullCapabilityPrinter:(NSString *)ipAddress
{
    PrinterDetails *pd = [[PrinterDetails alloc] init];
    pd.ip = ipAddress;
    pd.port = [NSNumber numberWithInt:0];
    pd.enBooklet = YES;
    pd.enStaple = YES;
    pd.enFinisher23Holes = NO;
    pd.enFinisher24Holes = YES;
    pd.enTrayAutoStacking = YES;
    pd.enTrayFaceDown = YES;
    pd.enTrayStacking = YES;
    pd.enTrayTop = YES;
    pd.enLpr = YES;
    pd.enRaw = YES;
    pd.isPrinterFound = NO;
    [self.printerManager registerPrinter:pd];
}

- (void)savePrinter
{
    [self dismissKeypad];
    
    // is it still possible to add a printer
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertHelper displayResult:kAlertResultErrMaxPrinters
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }
    
    // properly format/trim the input IP
    NSString* trimmedIP = [InputHelper trimIP:self.textIP.text];
#if DEBUG_LOG_ADD_PRINTER_SCREEN
    NSLog(@"[INFO][AddPrinter] trimmedIP=%@", trimmedIP);
#endif
    self.textIP.text = trimmedIP;
    
    // is the IP a valid IP address?
    if (![InputHelper isIPValid:trimmedIP])
    {
        [AlertHelper displayResult:kAlertResultErrInvalidIP
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }
    
    // was this printer already added before?
    if ([self.printerManager isIPAlreadyRegistered:trimmedIP])
    {
        [AlertHelper displayResult:kAlertResultErrPrinterDuplicate
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil];
        return;
    }
    
    // can the device connect to the network?
    if (![NetworkManager isConnectedToLocalWifi])
    {
        [self addFullCapabilityPrinter:trimmedIP];
        self.hasAddedPrinters = YES;
        if (self.isIpad)
            [self.printersViewController reloadData];
        
        [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self dismissScreen];
                }];
        
        return;
    }

#if DEBUG_LOG_ADD_PRINTER_SCREEN
    NSLog(@"[INFO][AddPrinter] initiating search");
#endif
    [self.printerManager searchForPrinter:trimmedIP];
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

    if (!printerFound)
    {
        NSString* trimmedIP = [InputHelper trimIP:self.textIP.text];
        [self addFullCapabilityPrinter:trimmedIP];
        self.hasAddedPrinters = YES;
        if (self.isIpad)
            [self.printersViewController reloadData];
        
        [AlertHelper displayResult:kAlertResultErrPrinterNotFound
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil
         withDismissHandler:^(CXAlertView *alertView) {
             [self dismissScreen];
         }];
    }
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
            [self.printersViewController reloadData];
        
        [AlertHelper displayResult:kAlertResultInfoPrinterAdded
                         withTitle:kAlertTitlePrintersAdd
                       withDetails:nil
                withDismissHandler:^(CXAlertView *alertView) {
                    [self dismissScreen];
                }];
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrPrinterCannotBeAdded
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

- (BOOL)textFieldShouldClear:(UITextField *)textField
{
    // disable the Save button if the IP Address text is cleared
    if (textField.tag == TAG_TEXT_IP)
        [self.saveButton setEnabled:NO];
         
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (textField.tag == TAG_TEXT_IP)
    {
        // ignore whitespace (for iPad keyboard)
        if ([string isEqualToString:@" "])
        {
            return NO;
        }

        // disable the Save button if backspace will clear the IP Address text
        if ((range.length == 1) && (range.location == 0) && ([string isEqualToString:@""]))
        {
            [self.saveButton setEnabled:NO];
        }
        else
        {
            [self.saveButton setEnabled:YES];
        }
    }
    
    return YES;
}

@end
