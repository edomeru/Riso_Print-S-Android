//
//  PrintersViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/6/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PrinterManager;

@interface PrintersViewController : UIViewController
/**
 Internal manager for adding printers to the DB, removing printers from the DB, and
 setting the default printer. It also maintains the list of the Printer objects from
 the DB. This is shared amongst all the child screens of the Printer screen.
 */
@property (strong, nonatomic) PrinterManager* printerManager;

/** NSIndexPath of the default printer in the Printers list **/
@property (strong, nonatomic) NSIndexPath* defaultPrinterIndexPath;

- (BOOL) setDefaultPrinter: (NSIndexPath *) indexPath;

- (IBAction)addPrinterAction:(id)sender;
- (IBAction)printerSearchAction:(id)sender;

@end
