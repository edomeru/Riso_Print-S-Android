//
//  PrinterCollectionViewCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PrinterStatusView;

@protocol PrinterCollectionViewCellDelegate
-(void) setDefaultPrinterCell:(BOOL) isDefaultOn forIndexPath:(NSIndexPath *) indexPath;
@end

@interface PrinterCollectionViewCell : UICollectionViewCell

@property (nonatomic, weak) IBOutlet UILabel *nameLabel;
@property (nonatomic, weak) IBOutlet UILabel *ipAddressLabel;
@property (nonatomic, weak) IBOutlet UILabel *portLabel;
@property (nonatomic, weak) IBOutlet UISwitch *defaultSwitch;
@property (nonatomic, weak) IBOutlet PrinterStatusView *statusView;
@property (weak, nonatomic) IBOutlet UIView *cellHeader;

@property (nonatomic, weak) id <PrinterCollectionViewCellDelegate> delegate;
@property (nonatomic, weak) NSIndexPath *indexPath;
-(void) setAsDefaultPrinterCell:(BOOL) isDefaultPrinterCell;
@end
