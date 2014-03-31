//
//  PrintSettingsHeaderCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsHeaderCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *groupLabel;
@property (nonatomic) BOOL expanded;

@end
