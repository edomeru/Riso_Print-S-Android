//
//  AddPrinterScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"
#import "UIViewController+Segue.h"
#import "PrinterSearchDelegate.h"

@class PrinterManager;

@interface AddPrinterViewController : SlidingViewController <UITextFieldDelegate, PrinterSearchDelegate>

/** Reference to the PrinterManager object of the Printers screen. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (assign, nonatomic) BOOL hasAddedPrinters;

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

/**
 Unwinds back to the Printers screen.
 This is for iPhone only.
 */
- (IBAction)onBack:(UIButton*)sender;

/**
 The input Printer info is retrieved from the UI, then the printer
 is searched from the network. If it is available and it is
 supported the printer object is created and stored in the DB.
 */
- (IBAction)onSave:(UIButton*)sender;

@end
