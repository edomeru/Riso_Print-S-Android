//
//  PrintSettingsItemInputCell.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsItemInputCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *settingLabel;
@property (nonatomic, weak) IBOutlet UITextField *valueTextField;
@property (nonatomic, weak) IBOutlet UIView *separator;

@end
