//
//  PrinterInfoScreenController.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/6/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SlidingViewController.h"
#import "PrinterStatusHelper.h"

@class Printer;
@class PrinterManager;

@interface PrinterInfoViewController : SlidingViewController <PrinterStatusHelperDelegate>
/*NSIndexPath of the printer of which info was shown*/
@property (weak, nonatomic) NSIndexPath* indexPath;
/*Default printer indicator*/
@property BOOL isDefaultPrinter;
/*Online status of printer*/
@property BOOL onlineStatus;
@end
