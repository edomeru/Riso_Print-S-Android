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
#import "PrintersIpadViewController.h"
#import "PrinterManager.h"

@interface AddPrinterViewController : SlidingViewController <UITextFieldDelegate, PrinterSearchDelegate>

/** Flag that will be set to YES when at least one successful printer was added. */
@property (readonly, assign, nonatomic) BOOL hasAddedPrinters;

/** Reference to the Printers screen (for iPad only) */
@property (weak, nonatomic) PrintersIpadViewController* printersViewController;

@end
