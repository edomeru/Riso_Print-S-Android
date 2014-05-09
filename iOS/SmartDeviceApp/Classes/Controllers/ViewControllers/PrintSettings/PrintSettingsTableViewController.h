//
//  PrintSettingsTableViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PreviewSetting;
@class Printer;

@interface PrintSettingsTableViewController : UITableViewController<UITextFieldDelegate>
@property (nonatomic, strong) NSNumber *printerIndex;

/**
 Action when the printer list view controller unwinds to the print settings table
 */
- (IBAction)unwindToPrintSettings:(UIStoryboardSegue *)sender;
@end