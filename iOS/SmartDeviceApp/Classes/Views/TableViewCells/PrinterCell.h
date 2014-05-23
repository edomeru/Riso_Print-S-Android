//
//  PrinterCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrinterStatusView.h"
#import "DeleteButton.h"

@interface PrinterCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *printerName;
@property (weak, nonatomic) IBOutlet PrinterStatusView *printerStatus;
@property (weak, nonatomic) IBOutlet UIView* separator;
@property (weak, nonatomic) IBOutlet UILabel *ipAddress;

- (void)setCellToBeDeletedState:(BOOL)isCellForDelete;
- (void)setCellStyleForDefaultCell;
- (void)setCellStyleForNormalCell;
- (void)cancelDeleteButton;
- (void)setDeleteButtonLayout;

@end
