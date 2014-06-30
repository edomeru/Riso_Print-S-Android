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

/**
 * Controller for the screen listing the currently supported print settings.
 * Selecting a print setting triggers a transition to the PrintSettingsOptionTableViewController.
 */
@interface PrintSettingsTableViewController : UITableViewController<UITextFieldDelegate>

/**
 * Stores the list index of the printer.
 * This is only used for the "Default Print Settings" screen, which transitions
 * from either the PrintersIpadViewController or the PrintersIphoneViewController.\n
 * In both controllers, the list index indicates which printer was selected.\n\n
 * If coming from the "Print Preview" screen, this is nil.
 */
@property (nonatomic, strong) NSNumber *printerIndex;

/**
 * Interface for other controllers (i.e. PrintSettingsPrinterViewController with
 * the Print button) to force close the keypad if it is still displayed (i.e. user
 * did not dismiss the keypad before pressing the print button). This will enable
 * the textfield contents to be saved (i.e. Pin Code).
 */
- (void)endEditing;

@end
