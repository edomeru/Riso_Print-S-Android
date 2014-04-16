//
//  PrintSettingsTableViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PreviewSetting;
@class Printer;

@interface PrintSettingsTableViewController : UITableViewController<UITextFieldDelegate>
@property (nonatomic, strong) PreviewSetting *previewSetting;
@property (nonatomic, weak) Printer *printer;
@property (nonatomic) BOOL isDefaultSettingsMode;

/**
 Action when the printer list view controller unwinds to the print settings table
 */
- (IBAction)unwindToPrintSettings:(UIStoryboardSegue *)sender;
@end
