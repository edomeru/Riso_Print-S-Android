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

@protocol PrinterInfoViewControllerDelegate
-(void) updateDefaultPrinter:(BOOL) isDefaultOn atIndexPath: (NSIndexPath *) indexPath;
-(Printer *) getPrinterAtIndexPath: (NSIndexPath *) indexPath;
@end

@interface PrinterInfoViewController : SlidingViewController <PrinterStatusHelperDelegate>
/*NSIndexPath of the printer of which info was shown*/
@property (weak, nonatomic) NSIndexPath* indexPath;
/*Delegate to get and update printer*/
@property (weak, nonatomic) id <PrinterInfoViewControllerDelegate> delegate;
/*Default printer indicator*/
@property BOOL isDefaultPrinter;
@end
