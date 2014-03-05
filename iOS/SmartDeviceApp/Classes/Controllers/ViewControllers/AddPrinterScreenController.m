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

#define TAG_TEXT_IP         1
#define TAG_TEXT_USERNAME   2
#define TAG_TEXT_PASSWORD   3

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
 Sets-up the Navigation Bar views.
 1. Disables the Save button.
 **/
- (void)setupNavBar;

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
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // setup properties
    self.addedPrinters = [NSMutableArray array];

    // setup view
    [self setupNavBar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Navigation Bar

- (void)setupNavBar
{
    [self.saveButton setEnabled:NO];
}

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)onSave:(UIBarButtonItem *)sender
{
    // check if trying to add the same printer
    BOOL bIsAlreadyAdded = NO;
    if ([self.addedPrinters count] != 0)
    {
        for (Printer* onePrinter in self.addedPrinters)
        {
            if ([onePrinter.ip_address isEqualToString:self.inputIP.text])
            {
                bIsAlreadyAdded = YES;
                break;
            }
        }
    }
    if (bIsAlreadyAdded)
    {
        [self displayResult:ERR_ALREADY_ADDED];
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
            NSString* formattedIP = self.inputIP.text;
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
                        
                        // add the printer to DB
                        BOOL isAddedToDB = [PrinterManager addPrinterToDB:newPrinter];
                        if (!isAddedToDB)
                        {
                            [self displayResult:ERR_CANNOT_ADD];
                        }
                        else
                        {
                            //TODO: disable searching indicator
                            
                            [self displayResult:NO_ERROR];
                            
                            // update the list of added printers
                            [self.addedPrinters addObject:newPrinter];
    
                            //newPrinter = nil;
                        }
                    }
                }
            }
        }
    }
    
    [self dismissKeypad];
}

#pragma mark - TextFields

- (void)dismissKeypad
{
    if (self.inputIP.isEditing)
        [self.inputIP resignFirstResponder];
    else if (self.inputUsername.isEditing)
        [self.inputUsername resignFirstResponder];
    else if (self.inputPassword.isEditing)
        [self.inputPassword resignFirstResponder];
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
        // disable the Save button if backspace will clear the IP Address text
        if ((range.length == 1) && (range.location == 0) && ([string isEqualToString:@""]))
            [self.saveButton setEnabled:NO];
        else
            [self.saveButton setEnabled:YES];
    }
    
    return YES;
}

#pragma mark - Display Result

- (void)displayResult:(RESULT_TYPE)result
{
    UIAlertView* resultAlert = [[UIAlertView alloc] initWithTitle:@"Add Printer Info"
                                                          message:nil
                                                         delegate:nil
                                                cancelButtonTitle:@"OK"
                                                otherButtonTitles:nil];
    
    //TODO: replace messages with localizable strings
    
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
