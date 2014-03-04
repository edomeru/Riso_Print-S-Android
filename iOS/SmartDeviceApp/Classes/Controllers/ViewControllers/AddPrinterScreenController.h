//
//  AddPrinterScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Printer;

@interface AddPrinterScreenController : UITableViewController

/**
 A copy of the list of Printer objects from the Printers screen.
 **/
@property NSArray* listSavedPrinters;

/**
 A copy of the new Printer object searched and added to the database.
 This will be set to nil if no printer was added.
 **/
@property Printer* addedPrinter;

/**
 This method is called when returning to the Printers screen.
 The Printer info is not saved.
 **/
- (IBAction)onBack:(UIBarButtonItem *)sender;

/**
 This method is called when returning to the Printers screen.
 The Printer is first searched from the network. If it is
 available, its info and capabilities are retrieved. The
 Printer object is then stored in the DB, and the list of the
 saved printers in the Printers screen is updated.
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

@end
