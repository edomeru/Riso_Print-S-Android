//
//  PrintSettingsHeaderCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsHeaderCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *groupLabel;
@property (nonatomic) BOOL expanded;

@end