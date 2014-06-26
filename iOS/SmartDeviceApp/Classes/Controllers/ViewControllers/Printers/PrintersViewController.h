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

/**
 * Parent class for PrintersIphoneViewController and PrintersIpadViewController.
 * Contains the properties and methods affecting both phones and tablets.
 */
@interface PrintersViewController : UIViewController

/**
 * Reference to the PrinterManager singleton.
 */
@property (strong, nonatomic) PrinterManager* printerManager;

/**
 * Index path of the default printer in the list of printers. 
 * The index path refers to a row in the UITableView (for phones)
 * or an item in the UICollectionView (for tablets).
 */
@property (strong, nonatomic) NSIndexPath *defaultPrinterIndexPath;

/**
 * Index path of the printer marked for deletion in the list of printers.
 * The index path refers to a row in the UITableView (for phones)
 * or an item in the UICollectionView (for tablets).\n
 * This is nil if no printer is marked for deletion.
 */
@property (strong, nonatomic) NSIndexPath *toDeleteIndexPath;

/**
 * Reference to the "No Printers" label that is displayed if there are no printers.
 */
@property (weak, nonatomic) IBOutlet UILabel* emptyLabel;

/** 
 * Responds to pressing the main menu button in the header.
 * Displays the Main Menu panel.
 *
 * @param sender the button object
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 * Responds to pressing the add printer button in the header.
 * Displays the "Add Printer" screen.
 *
 * @param sender the button object
 */
- (IBAction)addPrinterAction:(id)sender;

/**
 * Responds to pressing the printer search button in the header.
 * Displays the "Printer Search" screen.
 *
 * @param sender the button object
 */
- (IBAction)printerSearchAction:(id)sender;

/** 
 * Reloads the displayed list of printers.
 */
- (void)reloadPrinters;

@end
