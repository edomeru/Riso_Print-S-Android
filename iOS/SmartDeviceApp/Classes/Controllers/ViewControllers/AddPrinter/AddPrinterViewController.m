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

#define INPUT_ROWS          3
#define CELL_ROW_IP         0
#define CELL_ROW_USERNAME   1
#define CELL_ROW_PASSWORD   2

#define ALERT_ADD_PRINTER @"Add Printer Info"

@interface AddPrinterViewController ()

/**
 A list of the IP addresses of the new printers added.
 This is initially nil and will remain nil if no printer/s is/are added.
 */
@property (strong, nonatomic) NSMutableArray* listAddedPrinters;

/**
 Stores the number of saved printers.
 */
@property (assign, nonatomic) BOOL willEndWithoutAdd;

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
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;
    }
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
    self.listAddedPrinters = [NSMutableArray array];
    self.hasAddedPrinters = NO;
    
    // setup view
    [self enableSaveButton:NO];
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
    [self.printerManager stopSearching];
    [self unwindFromOverTo:[self.parentViewController class]];
}

- (IBAction)onSave:(UIButton *)sender
{
    // check if trying to add the same printer
    BOOL isAlreadyAdded = NO;
    if ([self.listAddedPrinters count] != 0)
    {
        for (NSString* printerIP in self.listAddedPrinters)
        {
            if ([printerIP isEqualToString:self.textIP.text])
            {
                isAlreadyAdded = YES;
                break;
            }
        }
    }
    
    if (isAlreadyAdded)
    {
        [AlertUtils displayResult:ERR_ALREADY_ADDED withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
    else
    {
        // check if the device is connected to the network
        BOOL isConnectedToNetwork = [NetworkManager checkNetworkConnection];
        if (!isConnectedToNetwork)
        {
            [AlertUtils displayResult:ERR_NO_NETWORK withTitle:ALERT_ADD_PRINTER withDetails:nil];
        }
        else
        {
            // check if the input IP is valid
            NSString* formattedIP = self.textIP.text;
            BOOL isInputValid = [self validateAndFormatIP:&formattedIP];
            if (!isInputValid)
            {
                [AlertUtils displayResult:ERR_INVALID_IP withTitle:ALERT_ADD_PRINTER withDetails:nil];
            }
            else
            {
                NSLog(@"initiated search");
                self.willEndWithoutAdd = YES; //catch for timeout
                [self.printerManager searchForPrinter:formattedIP];
                NSLog(@"returned to screen controller");
                // callbacks for the search will be handled in delegate methods
                
                // if UI needs to do other things, do it here
                //TODO: show the searching indicator
            }
        }
    }
    
    [self dismissKeypad];
}

#pragma mark - PrinterSearchDelegate

- (void)searchEnded
{
    if (self.willEndWithoutAdd)
        [AlertUtils displayResult:ERR_PRINTER_NOT_FOUND withTitle:ALERT_ADD_PRINTER withDetails:nil];
    
    //TODO: hide the searching indicator
}

- (void)updateForNewPrinter:(PrinterDetails*)printerDetails
{
    NSLog(@"update UI for NEW printer with IP=%@", printerDetails.ip);
    self.willEndWithoutAdd = NO; //search did not timed-out
    
    if ([self.printerManager registerPrinter:printerDetails])
    {
        [AlertUtils displayResult:NO_ERROR withTitle:ALERT_ADD_PRINTER withDetails:nil];
        self.hasAddedPrinters = YES;
        [self.listAddedPrinters addObject:printerDetails.ip];
    }
    else
    {
        [AlertUtils displayResult:ERR_CANNOT_ADD withTitle:ALERT_ADD_PRINTER withDetails:nil];
    }
    
    //TODO: hide the searching indicator
}

- (void)updateForOldPrinter:(NSString*)printerIP withExtra:(NSArray*)otherDetails
{
    NSLog(@"update UI for OLD printer with IP=%@", printerIP);
    self.willEndWithoutAdd = NO; //search did not timed-out
    
    [AlertUtils displayResult:ERR_ALREADY_ADDED withTitle:ALERT_ADD_PRINTER withDetails:@[printerIP]];
    
    //TODO: hide the searching indicator
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return INPUT_ROWS;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger row = indexPath.row;
    UITableViewCell* cell;
    switch (row)
    {
        case 0:
            cell = self.cellIPAddress;
            break;
        case 1:
            cell = self.cellUsername;
            break;
        case 2:
            cell = self.cellPassword;
            break;
    }
    
    return cell;
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

- (BOOL)validateAndFormatIP:(NSString**)ip
{
    //TODO: check if IP address is valid
    //TODO: format the IP address (save in the same parameter)
    /**
     IP Address Format: xxx.xxx.xxx.xxx
     
     # of Characters: 7-15
     Type of Characters: Digits and Dots
     Numerical Values: 0-255
     
     Format:
     - 4 dot-separated numbers
     - leading zeroes are disregarded
     - spaces are automatically trimmed
     - cannot input over 15 characters
     */
    
    //no issues
    return YES;
}

@end
