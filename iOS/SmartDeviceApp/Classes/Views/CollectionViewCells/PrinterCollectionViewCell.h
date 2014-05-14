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

@property (nonatomic, weak) IBOutlet UILabel *nameLabel;
@property (nonatomic, weak) IBOutlet UILabel *ipAddressLabel;
@property (weak, nonatomic) IBOutlet UISegmentedControl *portSelection;
@property (nonatomic, weak) IBOutlet UISwitch *defaultSwitch;
@property (nonatomic, weak) IBOutlet PrinterStatusView *statusView;
@property (weak, nonatomic) IBOutlet UIView *cellHeader;
@property (weak, nonatomic) IBOutlet UIButton *defaultSettingsButton;
@property (weak, nonatomic) IBOutlet DeleteButton *deleteButton;

-(void) setAsDefaultPrinterCell:(BOOL) isDefaultPrinterCell;
-(void) setCellToBeDeletedState:(BOOL) isCellForDelete;
@end
