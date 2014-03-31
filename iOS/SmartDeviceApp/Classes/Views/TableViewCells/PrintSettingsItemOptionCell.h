//
//  PrintSettingsItemOptionCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsItemOptionCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *settingLabel;
@property (nonatomic, weak) IBOutlet UILabel *valueLabel;
@property (nonatomic, weak) IBOutlet UIView *separator;

@end
