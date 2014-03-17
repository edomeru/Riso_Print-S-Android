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

#define CELL_ROW_IP         0
#define CELL_ROW_USERNAME   1
#define CELL_ROW_PASSWORD   2

@interface AddPrinterViewController ()

/**
 Flag that indicates that a printer search was initiated, but
 either the printer was not found or the search timed-out.
 */
@property (assign, nonatomic) BOOL willEndWithoutAdd;

/**
 Progress indicator that a search is ongoing.
 */
@property (strong, nonatomic) UIActivityIndicatorView* progressIndicator;

/**
 Called when screen loads.
 Sets-up this controller's properties and views.
 */
- (void)setup;

/**
 Enables/Disables the Save button.
 
 @param isEnabled
        YES or NO
 */
- (void)enableSaveButton:(BOOL)isEnabled;

/**
 Tells the currently active TextField to close the keypad/numpad.
 */
- (void)dismissKeypad;

/**
 Removes leading zeroes and trims extra spaces from the
 input IP string.
 @param inputIP
        the UITextField contents
 @return the formatted IP string
 */
- (NSString*)formatIPString:(NSString*)inputIP;

/**
 Checks if the input IP is a valid IP address.
 @return YES if valid, NO otherwise.
 */
- (BOOL)isIPValid:(NSString*)inputIP;

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
    self.printerManager.delegate = self;
    self.hasAddedPrinters = NO;
    
    // setup view
    [self enableSaveButton:NO];
    
    // setup progress indicator
    self.progressIndicator = [[UIActivityIndicatorView alloc]
                              initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    self.progressIndicator.frame = CGRectMake(0, 0, 40, 40);
    self.progressIndicator.center = self.view.center;
    [self.progressIndicator setColor:[UIColor whiteColor]];
    [self.view addSubview:self.progressIndicator];
    [self.progressIndicator bringSubviewToFront:self.view];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

#pragma mark - Header

- (void)enableSaveButton:(BOOL)isEnabled
{
    if (isEnabled)
    {
        [self.saveButton setAlpha:1.0f];
        [self.saveButton setEnabled:YES];
    }
    else
    {
        [self.saveButton setAlpha:0.3f];
        [self.saveButton setEnabled:NO];
    }
}

- (IBAction)onBack:(UIButton *)sender 
{
    NSLog(@"[INFO][AddPrinter] canceling search");
    [self.printerManager stopSearching];
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (IBAction)onSave:(UIButton *)sender
{
    if (![NetworkManager isConnectedToNetwork])
    {
        [AlertUtils displayResult:ERR_NO_NETWORK withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
    else if ([self.printerManager isAtMaximumPrinters])
    {
        [AlertUtils displayResult:ERR_MAX_PRINTERS withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
    else
    {
        // check if the input IP is valid
        NSString* formattedIP = [self formatIPString:self.textIP.text];
        if (![self isIPValid:formattedIP])
        {
            [AlertUtils displayResult:ERR_INVALID_IP withTitle:ALERT_ADD_PRINTER withDetails:nil];
        }
        else if ([self.printerManager isIPAlreadyRegistered:formattedIP])
        {
            [AlertUtils displayResult:ERR_ALREADY_ADDED withTitle:ALERT_ADD_PRINTER withDetails:nil];
        }
        else
        {
            NSLog(@"[INFO][AddPrinter] initiating search");
            self.willEndWithoutAdd = YES; //catch for SNMP timeout, will become NO if a printer is found
            [self.printerManager searchForPrinter:formattedIP];
            // callbacks for the search will be handled in delegate methods
            
            // if UI needs to do other things, do it here
            
            // show the searching indicator
            [self.progressIndicator startAnimating];
            
            // disable save button
            [self enableSaveButton:NO];
        }
    }
    
    [self dismissKeypad];
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    if (self.willEndWithoutAdd)
        [AlertUtils displayResult:ERR_PRINTER_NOT_FOUND withTitle:ALERT_ADD_PRINTER withDetails:nil];
    
    // hide the searching indicator
    [self.progressIndicator stopAnimating];
    
    // re-enable the save button
    [self enableSaveButton:YES];
}

- (void)updateForNewPrinter:(PrinterDetails*)printerDetails
{
    NSLog(@"[INFO][AddPrinter] received NEW printer with IP=%@", printerDetails.ip);
    NSLog(@"[INFO][AddPrinter] updating UI");
    self.willEndWithoutAdd = NO; //search did not timeout
    
    if ([self.printerManager registerPrinter:printerDetails])
    {
        [AlertUtils displayResult:INFO_PRINTER_ADDED withTitle:ALERT_ADD_PRINTER withDetails:nil];
        self.hasAddedPrinters = YES;
    }
    else
    {
        [AlertUtils displayResult:ERR_CANNOT_ADD withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
}

- (void)updateForOldPrinter:(NSString*)printerIP withName:(NSString*)printerName
{
    NSLog(@"[INFO][AddPrinter] received OLD printer with IP=%@", printerIP);
    NSLog(@"[INFO][AddPrinter] updating UI");
    self.willEndWithoutAdd = NO; //search did not timeout
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

- (BOOL)textFieldShouldClear:(UITextField *)textField
{
    // disable the Save button if the IP Address text is cleared
    if (textField.tag == CELL_ROW_IP)
        [self enableSaveButton:NO];
         
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if (textField.tag == CELL_ROW_IP)
    {
        // disable the Save button if backspace will clear the IP Address text
        if ((range.length == 1) && (range.location == 0) && ([string isEqualToString:@""]))
            [self enableSaveButton:NO];
        else
            [self enableSaveButton:YES];
    }
    
    return YES;
}

#pragma mark - Input IP Validation

- (NSString*)formatIPString:(NSString*)inputIP
{
    //TODO: leading zeroes are disregarded
    //TODO: spaces are automatically trimmed
    
    return inputIP;
}

- (BOOL)isIPValid:(NSString*)inputIP;
{
    //TODO:
    /**
     IP Address Format: xxx.xxx.xxx.xxx
     
     # of Characters: 7-15
     Type of Characters: Digits and Dots
     Numerical Values: 0-255
     
     Format:
     - 4 dot-separated numbers
     - cannot input over 15 characters
     */
    
    //no issues
    return YES;
}

@end
