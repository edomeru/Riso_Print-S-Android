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
 * Reference to the save (+) button.
 */
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

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
    if([string stringByTrimmingCharactersInSet:validCharacters].length > 0)
    {
        return NO;
    }
    
    textField.text = [textField.text stringByReplacingCharactersInRange:range withString:string];
    return NO;
}

@end
