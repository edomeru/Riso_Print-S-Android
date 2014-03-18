//
//  PrintersScreenController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintersViewController.h"
#import "PrinterInfoViewController.h"

@class Printer;
@class PrinterManager;

@interface PrintersIphoneViewController : PrintersViewController <UITableViewDataSource, UITableViewDelegate, PrinterInfoViewControllerDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@end
