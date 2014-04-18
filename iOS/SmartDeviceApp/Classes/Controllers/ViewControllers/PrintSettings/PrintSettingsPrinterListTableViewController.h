//
//  PrintSettingsPrinterListTableViewController.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
@class Printer;

@interface PrintSettingsPrinterListTableViewController : UITableViewController
@property (weak, nonatomic) Printer *selectedPrinter;
@end
