//
//  PrintSettingsOptionTableViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PreviewSetting;

@interface PrintSettingsOptionTableViewController : UITableViewController

@property (nonatomic, weak) NSDictionary *setting;
@property (nonatomic, weak) PreviewSetting *previewSetting;

@end
