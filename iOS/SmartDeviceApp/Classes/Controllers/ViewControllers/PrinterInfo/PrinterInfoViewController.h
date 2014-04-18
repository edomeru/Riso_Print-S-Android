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

@protocol PrinterInfoDelegate
/** Method called by PrinterInfoViewController to be able to segue to print settings screen
   @note This method is used by the PrinterInfoViewController to call the parent view controller to do the segue to the Print Settings view controller. This is because the slide segue can only be executed correctly by the view controller in the root view controller which is the parent of the PrinterInfoViewController */
-(void) segueToPrintSettings;
@end


@interface PrinterInfoViewController : SlidingViewController <PrinterStatusHelperDelegate>
/*NSIndexPath of the printer of which info was shown*/
@property (weak, nonatomic) NSIndexPath* indexPath;
/*Default printer indicator*/
@property BOOL isDefaultPrinter;
/*Online status of printer*/
@property BOOL onlineStatus;
@property (weak, nonatomic) id <PrinterInfoDelegate> delegate;
@end
