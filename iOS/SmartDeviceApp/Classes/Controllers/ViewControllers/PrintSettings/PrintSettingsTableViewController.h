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
 Interface for other controllers (i.e. PrintSettingsPrinterViewController with
 the Print button) to force close the keypad if it is still displayed (i.e. user 
 did not dismiss the keypad before pressing print button). This will enable the 
 textfield contents to be saved (i.e. pin code).
 */
- (void)endEditing;

@end
