//
//  PrintersViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeViewController.h"
#import "UIViewController+Segue.h"

@class PrinterManager;

@interface PrintersViewController : UIViewController

/** Handler for the Printer data. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** NSIndexPath of the default printer in the Printers list **/
@property (strong, nonatomic) NSIndexPath* defaultPrinterIndexPath;
@property (strong, nonatomic) NSIndexPath *toDeleteIndexPath;

- (IBAction)mainMenuAction:(id)sender;
- (IBAction)addPrinterAction:(id)sender;
- (IBAction)printerSearchAction:(id)sender;

/** Reloads the displayed list of Printers */
- (void)reloadData;

@end
