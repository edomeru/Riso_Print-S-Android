//
//  AddPrinterScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Printer;

@interface AddPrinterScreenController : UITableViewController <UITextFieldDelegate>

/**
 A copy of the list of Printer objects from the Printers screen.
 **/
@property (nonatomic, strong) NSArray* listSavedPrinters;

/**
 A list of the new Printer objects searched and added to the database.
 This is initially nil and will remain nil if no printer/s is/are added.
 **/
@property (nonatomic, strong) NSMutableArray* addedPrinters;

/**
 Unwinds back to the Printers screen.
 Any unsaved Printer info will not be saved.
 **/
- (IBAction)onBack:(UIBarButtonItem *)sender;

/**
 The input Printer info is retrieved from the UI, then
 the printer is searched from the network. If it is
 available, its info and capabilities are retrieved. If
 the printer is supported, the printer object is created
 and stored in the DB.
 **/
- (IBAction)onSave:(UIBarButtonItem *)sender;

/**
 Input TextField for the IP Address.
 **/
@property (weak, nonatomic) IBOutlet UITextField *inputIP;

/**
 Input TextField for the Username.
 **/
@property (weak, nonatomic) IBOutlet UITextField *inputUsername;

/**
 Input TextField for the Password.
 **/
@property (weak, nonatomic) IBOutlet UITextField *inputPassword;

/**
 Save Button.
 **/
@property (weak, nonatomic) IBOutlet UIBarButtonItem *saveButton;

@end
