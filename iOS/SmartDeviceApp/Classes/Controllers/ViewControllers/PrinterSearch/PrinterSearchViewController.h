//
//  PrinterSearchScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"
#import "PrinterSearchDelegate.h"
#import "UIViewController+Segue.h"

@class PrinterManager;

@interface PrinterSearchViewController : SlidingViewController <UITableViewDataSource, UITableViewDelegate, PrinterSearchDelegate>

/** Reference to the PrinterManager object of the Printers screen. */
@property (strong, nonatomic) PrinterManager* printerManager;

/** Flag that will be set to YES when at least one successful printer was added. */
@property (assign, nonatomic) BOOL hasAddedPrinters;

/** TableView for the Search Results */
@property (weak, nonatomic) IBOutlet UITableView *tableView;

/**
 Unwinds back to the Printers screen.
 */
- (IBAction)onBack:(UIButton*)sender;

@end
