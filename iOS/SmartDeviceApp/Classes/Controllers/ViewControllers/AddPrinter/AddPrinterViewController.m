//
//  AddPrinterScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AddPrinterViewController.h"
#import "PrinterDetails.h"
#import "PrinterManager.h"
#import "NetworkManager.h"
#import "AlertUtils.h"
#import "InputUtils.h"

#define TAG_TEXT_IP         0
#define TAG_TEXT_USERNAME   1
#define TAG_TEXT_PASSWORD   2

@interface AddPrinterViewController ()

#pragma mark - Data Properties

/** Handler for the Printer data. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readwrite, assign, nonatomic) BOOL hasAddedPrinters;

/**
 Flag that indicates that a printer search was initiated, but
 either the printer was not found or the search timed-out.
 */
@property (assign, nonatomic) BOOL willEndWithoutAdd;

#pragma mark - UI Properties

/** Progress indicator that a search is ongoing. */
@property (strong, nonatomic) UIActivityIndicatorView* progressIndicator;

/** Input TextField for the IP Address. */
@property (weak, nonatomic) IBOutlet UITextField *textIP;

/** Input TextField for the Username. */
@property (weak, nonatomic) IBOutlet UITextField *textUsername;

/** Input TextField for the Password. */
@property (weak, nonatomic) IBOutlet UITextField *textPassword;

/** Save Button in the Header. */
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

/** Reference to the entire UIView below the Header. */
@property (weak, nonatomic) IBOutlet UIView *container;

#pragma mark - Internal Methods

/**
 Called when screen loads.
 Sets-up this controller's properties and views.
 */
- (void)setup;

/**
 Tells the currently active TextField to close the keypad/numpad.
 */
- (void)dismissKeypad;

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
    self.isFixedSize = YES;
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setup];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Setup

- (void)setup
{
    // setup properties
    self.printerManager = [PrinterManager sharedPrinterManager];
    self.printerManager.searchDelegate = self;
    self.hasAddedPrinters = NO;
    
    // setup the header buttons
    [self.saveButton setEnabled:NO];
    
    // setup progress indicator
    self.progressIndicator = [[UIActivityIndicatorView alloc]
                              initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    self.progressIndicator.frame = CGRectMake(0, 0, 40, 40);
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
        self.progressIndicator.center = self.view.center;
    else
        //for iPad, cannot use self.view.center because it is only partially visible (slide panel)
        self.progressIndicator.center = CGPointMake(self.container.frame.size.width/2,
                                                    self.container.frame.size.height/2);
    [self.progressIndicator setColor:[UIColor whiteColor]];
    [self.view addSubview:self.progressIndicator];
    [self.progressIndicator bringSubviewToFront:self.view];
}

#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // if the SNMP is still searching, the search is canceled
    // the printer, if found, will not be added to the list of saved printers
    if ([self.progressIndicator isAnimating])
    {
        NSLog(@"[INFO][AddPrinter] canceling search");
        [self.printerManager stopSearching];
    }
}

#pragma mark - Header

- (IBAction)onBack:(UIButton *)sender
{
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (IBAction)onSave:(UIButton *)sender
{
    [self dismissKeypad];
    
    // can the device connect to the network?
    if (![NetworkManager isConnectedToNetwork])
    {
        [AlertUtils displayResult:ERR_NO_NETWORK
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        return;
    }
    
    // is it still possible to add a printer
    if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertUtils displayResult:ERR_MAX_PRINTERS
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        return;
    }
    
    // properly format/trim the input IP
    NSString* trimmedIP = [InputUtils trimIP:self.textIP.text];
    NSLog(@"[INFO][AddPrinter] trimmedIP=%@", trimmedIP);
    self.textIP.text = trimmedIP;
    
    // is the IP a valid IP address?
    if (![InputUtils isIPValid:trimmedIP])
    {
        [AlertUtils displayResult:ERR_INVALID_IP
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        return;
    }
    
    // was this printer already added before?
    if ([self.printerManager isIPAlreadyRegistered:trimmedIP])
    {
        [AlertUtils displayResult:ERR_ALREADY_ADDED
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        return;
    }

    NSLog(@"[INFO][AddPrinter] initiating search");
    self.willEndWithoutAdd = YES; //catch for SNMP timeout, will become NO if a printer is found
    [self.printerManager searchForPrinter:trimmedIP];
    // callbacks for the search will be handled in delegate methods
    
    // if UI needs to do other things, do it here
    
    // show the searching indicator
    [self.progressIndicator startAnimating];
    
    // disable the save button
    [self.saveButton setEnabled:NO];
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    if (self.willEndWithoutAdd)
    {
        [AlertUtils displayResult:ERR_PRINTER_NOT_FOUND
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
    }

    // hide the searching indicator
    [self.progressIndicator stopAnimating];
    
    // re-enable the save button
    [self.saveButton setEnabled:YES];
    
    // if this is an iPad, reload the center panel
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        UIViewController* rootViewController = [self parentViewController];
        UIViewController* centerPanel = [[rootViewController childViewControllers] objectAtIndex:0];
        [centerPanel performSelector:@selector(reloadData)];
    }
}

- (void)updateForNewPrinter:(PrinterDetails*)printerDetails
{
    NSLog(@"[INFO][AddPrinter] received NEW printer with IP=%@", printerDetails.ip);
    NSLog(@"[INFO][AddPrinter] updating UI");
    self.willEndWithoutAdd = NO; //search did not timeout
    
    if ([self.printerManager registerPrinter:printerDetails])
    {
        [AlertUtils displayResult:INFO_PRINTER_ADDED
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
        self.hasAddedPrinters = YES;
    }
    else
    {
        [AlertUtils displayResult:ERR_CANNOT_ADD
                        withTitle:ALERT_TITLE_PRINTERS_ADD
                      withDetails:nil];
    }
}

- (void)updateForOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
    // will not be called since search will only be initiated
    // if the IP is not yet registered
}

#pragma mark - TextFields

- (void)dismissKeypad
{
    if (self.textIP.isEditing)
        [self.textIP resignFirstResponder];
    else if (self.textUsername.isEditing)
        [self.textUsername resignFirstResponder];
    else if (self.textPassword.isEditing)
        [self.textPassword resignFirstResponder];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self dismissKeypad];
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
