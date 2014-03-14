//
//  PrinterCollectionViewCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrinterCollectionViewCell : UICollectionViewCell

@property (nonatomic, weak) IBOutlet UILabel *nameLabel;
@property (nonatomic, weak) IBOutlet UILabel *ipAddressLabel;
@property (nonatomic, weak) IBOutlet UILabel *portLabel;
@property (nonatomic, weak) IBOutlet UISwitch *defaultSwitch;

@end
