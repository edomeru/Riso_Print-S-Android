//
//  PrintSettingsOptionTableViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PreviewSetting;

@interface PrintSettingsOptionTableViewController : UITableViewController

@property (nonatomic, weak) NSDictionary *setting;
@property (nonatomic, weak) PreviewSetting *previewSetting;

@end
