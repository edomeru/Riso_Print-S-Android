//
//  PrinterCell.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusView.h"

@interface PrinterCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet PrinterStatusView *printerStatus;


-(void) setAsDefaultPrinterCell:(BOOL) isDefaultPrinterCell;
@end
