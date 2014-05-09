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

@interface PrinterSearchViewController : SlidingViewController <UITableViewDataSource, UITableViewDelegate, PrinterSearchDelegate, UIGestureRecognizerDelegate>

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readonly, assign, nonatomic) BOOL hasAddedPrinters;

/** Reference to the Printers screen (for iPad only) */
@property (weak, nonatomic) PrintersIpadViewController* printersViewController;

@end
