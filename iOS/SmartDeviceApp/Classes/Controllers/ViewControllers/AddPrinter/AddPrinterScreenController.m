//
//  AddPrinterScreenController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "AddPrinterScreenController.h"
#import "Printer.h"
#import "PrinterManager.h"
#import "SNMPManager.h"
#import "NetworkManager.h"
#import "InputUtils.h"

#define INPUT_ROWS          3
#define CELL_ROW_IP         0
#define CELL_ROW_USERNAME   1
#define CELL_ROW_PASSWORD   2

#define UNWIND_TO_PRINTERS  @"UnwindRight"

typedef enum
{
    NO_ERROR,
    ERR_NO_NETWORK,
    ERR_INVALID_IP,
    ERR_CANNOT_ADD,
    ERR_ALREADY_ADDED,
} RESULT_TYPE;

@interface AddPrinterScreenController ()

/**
 Enables/Disables the Save button.
 
 @param isEnabled
        YES or NO
 **/
- (void)enableSaveButton:(BOOL)isEnabled;

/**
 Displays an AlertView for the result of the Add Printer operation.
 
 @param result
        One of the following result types:
            NO_ERROR,
            ERR_NO_NETWORK,
            ERR_INVALID_IP,
            ERR_CANNOT_ADD,
            ERR_ALREADY_ADDED
 **/
- (void)displayResult:(RESULT_TYPE)result;

/**
 Tells the currently active TextField to close the keypad/numpad.
 **/
- (void)dismissKeypad;

@end

@implementation AddPrinterScreenController

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

    // setup properties
    self.addedPrinters = [NSMutableArray array];

    // setup view
    [self enableSaveButton:NO];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
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
    [self performSegueWithIdentifier:UNWIND_TO_PRINTERS sender:self];
}

- (IBAction)onSave:(UIButton *)sender
{
    // check if trying to add the same printer
    if ([self.addedPrinters count] != 0)
    {
        for (Printer* onePrinter in self.addedPrinters)
        {
            if ([onePrinter.ip_address isEqualToString:self.textIP.text])
            {
                [self displayResult:ERR_ALREADY_ADDED];
                break;
            }
        }
    }
    else
    {
        // check if the device is connected to the network
        BOOL isConnectedToNetwork = [NetworkManager checkNetworkConnection];
        if (!isConnectedToNetwork)
        {
            [self displayResult:ERR_NO_NETWORK];
        }
        else
        {
            // check if the input IP is valid
            NSString* formattedIP = self.textIP.text;
            BOOL isInputValid = [InputUtils validateAndFormatIP:&formattedIP];
            if (!isInputValid)
            {
                [self displayResult:ERR_INVALID_IP];
            }
            else
            {
                // check if printer can be added to the list of saved printers in DB
                BOOL canAddPrinter = [PrinterManager canAddPrinter:formattedIP toList:self.listSavedPrinters];
                if (!canAddPrinter)
                {
                    [self displayResult:ERR_CANNOT_ADD];
                }
                else
                {
                    // create Printer object
                    Printer* newPrinter = [PrinterManager createPrinter];
                    newPrinter.ip_address = formattedIP;
                    
                    //TODO: load searching/progress indicator
                    
                    // use SNMP to search for the printer and get its capabilities
                    // check if printer is supported
                    BOOL isAvailableAndSupported = [PrinterManager searchForPrinter:&newPrinter];
                    if (!isAvailableAndSupported)
                    {
                        [self displayResult:ERR_CANNOT_ADD];
                    }
                    else
                    {
                        //TODO: copy the default print settings values to the print settings object of the printer object
                        PrintSetting* printSetting = [PrinterManager createPrintSetting];
                        newPrinter.printsetting = printSetting;
                        // add the printer to DB
                        BOOL isAddedToDB = [PrinterManager addPrinterToDB:newPrinter];//Amor: This line may not be needed because once the object is created using createPrinter and save is called, the object is automatically inserted in DB. 
                        if (!isAddedToDB)
                        {
                            [self displayResult:ERR_CANNOT_ADD];
                        }
                        else
                        {
                            //TODO: disable searching indicator
                            
                            [self displayResult:NO_ERROR];
                            
                            //since printer can be added, then it is online
                            newPrinter.onlineStatus = [NSNumber numberWithBool:YES];
                            
                            // update the list of added printers
                            [self.addedPrinters addObject:newPrinter];
                            newPrinter = nil;
                        }
                    }
                }
            }
        }
    }
    
    [self dismissKeypad];
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

#pragma mark - Display Result

- (void)displayResult:(RESULT_TYPE)result
{
    //TODO: replace messages with localizable strings
    
    UIAlertView* resultAlert = [[UIAlertView alloc] initWithTitle:@"Add Printer Info"
                                                          message:nil
                                                         delegate:nil
                                                cancelButtonTitle:@"OK"
                                                otherButtonTitles:nil];
    
    switch (result)
    {
        case NO_ERROR:
            [resultAlert setMessage:@"The new printer was added successfully."];
            break;
        case ERR_NO_NETWORK:
            [resultAlert setMessage:@"The device is not connected to the network."];
            break;
        case ERR_INVALID_IP:
            [resultAlert setMessage:@"The IP address is invalid. The printer could not be found."];
            break;
        case ERR_ALREADY_ADDED:
            [resultAlert setMessage:@"The printer has already been added."];
            break;
        case ERR_CANNOT_ADD:
        default:
            [resultAlert setMessage:@"The printer could not be added."];
            break;
            //TODO: it would be better to explain why the printer could not be added
    }
    
    [resultAlert show];
}

@end
