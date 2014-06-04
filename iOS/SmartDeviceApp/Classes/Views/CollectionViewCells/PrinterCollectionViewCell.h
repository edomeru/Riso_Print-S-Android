//
//  PrinterCollectionViewCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PrinterStatusView;
@class DeleteButton;

@interface PrinterCollectionViewCell : UICollectionViewCell

@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UILabel *ipAddressLabel;
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;
@property (weak, nonatomic) IBOutlet UISwitch *defaultSwitch;
@property (weak, nonatomic) IBOutlet UIImageView *defaultSetIcon;
@property (weak, nonatomic) IBOutlet PrinterStatusView *statusView;
@property (weak, nonatomic) IBOutlet UIView *cellHeader;
@property (weak, nonatomic) IBOutlet UIButton *defaultSettingsButton;
@property (weak, nonatomic) IBOutlet DeleteButton *deleteButton;
@property (weak, nonatomic) IBOutlet UIView *defaultSettingsRow;

- (void)setAsDefaultPrinterCell:(BOOL)isDefaultPrinterCell;
- (void)setCellToBeDeletedState:(BOOL)isCellForDelete;
- (void)setDefaultSettingsRowToSelected:(BOOL)isSelected;
@end
