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
#import "PrinterCell.h"

@interface PrintersIphoneViewController : PrintersViewController <UITableViewDataSource, UITableViewDelegate, PrinterInfoDelegate, PrinterCellDelegate>

@end
