//
//  PrintSettingsPrinterListTableViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusHelper.h"
@class Printer;

/**
 * Controller for the screen listing the available printers.
 * Selecting a printer automatically updates the preview and the current
 * set of preview settings.
 */
@interface PrintSettingsPrinterListTableViewController : UITableViewController <PrinterStatusHelperDelegate>

@end
