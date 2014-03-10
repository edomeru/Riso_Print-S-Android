//
//  AddPrinterScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

@class Printer;

@interface AddPrinterScreenController : SlidingViewController <UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate>

/**
 A copy of the list of Printer objects from the Printers screen.
 **/
@property (strong, nonatomic) NSArray* listSavedPrinters;

/**
 A list of the new Printer objects searched and added to the database.
 This is initially nil and will remain nil if no printer/s is/are added.
 **/
@property (strong, nonatomic) NSMutableArray* addedPrinters;

/**
 Input TextField for the IP Address.
 **/
@property (weak, nonatomic) IBOutlet UITextField *textIP;

/**
 Input TextField for the Username.
 **/
@property (weak, nonatomic) IBOutlet UITextField *textUsername;

/**
 Input TextField for the Password.
 **/
@property (weak, nonatomic) IBOutlet UITextField *textPassword;

/**
 Save Button.
 **/
@property (weak, nonatomic) IBOutlet UIButton *saveButton;

/**
 TableViewCell for the IP Address
 **/
@property (strong, nonatomic) IBOutlet UITableViewCell *cellIPAddress;

/**
 TableViewCell for the Username
 **/
@property (strong, nonatomic) IBOutlet UITableViewCell *cellUsername;

/**
 TableViewCell for the Password
 **/
@property (strong, nonatomic) IBOutlet UITableViewCell *cellPassword;

/**
 Unwinds back to the Printers screen.
 Any unsaved Printer info will not be saved.
 **/
- (IBAction)onBack:(UIButton *)sender;

/**
 The input Printer info is retrieved from the UI, then
 the printer is searched from the network. If it is
 available, its info and capabilities are retrieved. If
 the printer is supported, the printer object is created
 and stored in the DB.
 **/
- (IBAction)onSave:(UIButton *)sender;

@end
