//
//  PrintersScreenController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintersViewController.h"
#import "PrinterInfoViewController.h"
#import "PrinterInfoViewController.h"
#import "PrinterStatusHelper.h"

/**
 * Controller for the "Printers" screen (phone).
 */
@interface PrintersIphoneViewController : PrintersViewController <UITableViewDataSource, UITableViewDelegate, PrinterInfoDelegate, UIGestureRecognizerDelegate, UIScrollViewDelegate, PrinterStatusHelperDelegate>

@end
