//
//  PrinterSearchViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"
#import "UIViewController+Segue.h"
#import "PrintersIpadViewController.h"
#import "PrinterManager.h"

/**
 * Controller for the "Printer Search" screen (phone and tablet).
 */
@interface PrinterSearchViewController : SlidingViewController <UITableViewDataSource, UITableViewDelegate, PrinterSearchDelegate, UIGestureRecognizerDelegate>

/**
 * Flag that will be set to YES when a printer is successfully added.
 */
@property (readonly, assign, nonatomic) BOOL hasAddedPrinters;

/**
 * Reference to the controller of the "Printers" screen on a tablet.
 * If the device is a phone, then this property is nil and unused.
 */
@property (weak, nonatomic) PrintersIpadViewController* printersViewController;

@end
