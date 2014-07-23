//
//  PrinterInfoInfoController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SlidingViewController.h"

@class Printer;
@class PrinterManager;

/**
 * Allows the "Printer Info" screen to coordinate with the "Printers" screen.
 */
@protocol PrinterInfoDelegate <NSObject>

@required

/** 
 * Requests the "Printers" screen to display the "Default Print Settings" screen.
 */
- (void)segueToPrintSettings;

@end

/**
 * Controller for the "Printer Info" screen (phone only).
 */
@interface PrinterInfoViewController : SlidingViewController

/** 
 * Index path of the printer in the list displayed in PrintersIphoneViewController.
 */
@property (weak, nonatomic) NSIndexPath* indexPath;

/**
 * Flag that is set to YES if the printer being displayed is the default printer.
 */
@property BOOL isDefaultPrinter;

/**
 * Reference to the PrintersIphoneViewController.
 */
@property (weak, nonatomic) id <PrinterInfoDelegate> delegate;

/**
 * Reference to the default print settings button.
 */
@property (weak, nonatomic) IBOutlet UIButton *printSettingsButton;

@end
