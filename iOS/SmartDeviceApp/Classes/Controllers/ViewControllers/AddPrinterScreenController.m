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

typedef enum
{
    NO_ERROR,
    ERR_NO_NETWORK,
    ERR_INVALID_IP,
    ERR_CANNOT_ADD,
} RESULT_TYPE;

@interface AddPrinterScreenController ()

@property NSString* savedIP;
@property NSString* savedUsername;
@property NSString* savedPassword;

/**
 Displays an AlertView for the result of the Add Printer operation.
 
 @param addResult
        YES if successful, NO otherwise
 
 @param errorCode
        One of the following error codes:
            ERR_CODE_NO_ERROR,
            ERR_CODE_NO_NETWORK,
            ERR_CODE_INVALID_IP,
            ERR_CODE_CANNOT_ADD
 **/
- (void)displayResult:(RESULT_TYPE)result;

/**
 Saves the content of the TextFields.
 **/
- (void)getTextFieldInputs;

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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Navigation Bar Actions

- (IBAction)onBack:(UIBarButtonItem *)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)onSave:(UIBarButtonItem *)sender
{
    // check if the device is connected to the network
    BOOL isConnectedToNetwork = [NetworkManager checkNetworkConnection];
    if (!isConnectedToNetwork)
        [self displayResult:ERR_NO_NETWORK];
    else
    {
        // get the user input
        [self getTextFieldInputs];
        
        // check if the input IP is valid
        NSString* formattedIP = self.savedIP;
        BOOL isInputValid = [InputUtils validateAndFormatIP:&formattedIP];
        if (!isInputValid)
            [self displayResult:ERR_INVALID_IP];
        else
        {
            // update IP address with the formatted copy
            self.savedIP = formattedIP;
            
            // check if printer can be added to the list
            BOOL canAddPrinter = [PrinterManager canAddPrinter:self.savedIP toList:self.listSavedPrinters];
            if (!canAddPrinter)
                [self displayResult:ERR_CANNOT_ADD];
            else
            {
                // create Printer object
                Printer* newPrinter = [PrinterManager createPrinter];
                newPrinter.ip_address = self.savedIP;
                
                //TODO: load searching/progress indicator
                
                // use SNMP to search for the printer and get its capabilities
                BOOL isAccessibleAndSupported = [SNMPManager searchForPrinter:&newPrinter];
                if (!isAccessibleAndSupported)
                    [self displayResult:ERR_CANNOT_ADD];
                else
                {
                    //TODO: copy the default print settings values to the print settings object of the printer object
                    
                    // add the printer to DB
                    BOOL isAddedToDB = [PrinterManager addPrinterToDB:newPrinter];
                    if (!isAddedToDB)
                        [self displayResult:ERR_CANNOT_ADD];
                    else
                    {
                        //TODO: disable searching indicator
                        
                        [self displayResult:NO_ERROR];
                        
                        // set the copy of the Printer to be accessed by the Printers screen
                        self.addedPrinter = newPrinter;
                    }
                }
            }
        }
    }
}

#pragma mark - Get TextField Inputs

- (void)getTextFieldInputs
{
    self.savedIP = self.inputIP.text;
    self.savedUsername = self.inputUsername.text;
    self.savedPassword = self.inputPassword.text;
}

#pragma mark - Display Result

- (void)displayResult:(RESULT_TYPE)result
{
    UIAlertView* resultAlert = [[UIAlertView alloc] initWithTitle:@"Add Printer Info"
                                                          message:nil
                                                         delegate:nil
                                                cancelButtonTitle:@"OK"
                                                otherButtonTitles:nil];
    
    //TODO: replace with localizable strings
    
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
        case ERR_CANNOT_ADD:
        default:
            [resultAlert setMessage:@"The printer could not be added."];
            break;
        //TODO: it would be better to explain why the printer could not be added
    }
    
    [resultAlert show];
}

@end
