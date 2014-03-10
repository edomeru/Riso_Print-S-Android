//
//  PrinterSearchScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

@interface PrinterSearchScreenController : SlidingViewController <UITableViewDataSource, UITableViewDelegate>

/**
 A copy of the list of Printer objects from the Printers screen.
 **/
@property (nonatomic, strong) NSArray* listSavedPrinters;

/**
 Unwinds back to the Printers screen.
 **/
- (IBAction)onBack:(UIBarButtonItem *)sender;

@end
