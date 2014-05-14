//
//  PrinterInfoInfoController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SlidingViewController.h"

@class Printer;
@class PrinterManager;

@protocol PrinterInfoDelegate
/** Method called by PrinterInfoViewController to be able to segue to print settings screen
   @note This method is used by the PrinterInfoViewController to call the parent view controller to do the segue to the Print Settings view controller. This is because the slide segue can only be executed correctly by the view controller in the root view controller which is the parent of the PrinterInfoViewController */
-(void) segueToPrintSettings;
@end


@interface PrinterInfoViewController : SlidingViewController
/*NSIndexPath of the printer of which info was shown*/
@property (weak, nonatomic) NSIndexPath* indexPath;
/*Default printer indicator*/
@property BOOL isDefaultPrinter;
/*Online status of printer*/
@property (weak, nonatomic) id <PrinterInfoDelegate> delegate;
/*Button to default print settings screen*/
@property (weak, nonatomic) IBOutlet UIButton *printSettingsButton;

@end
