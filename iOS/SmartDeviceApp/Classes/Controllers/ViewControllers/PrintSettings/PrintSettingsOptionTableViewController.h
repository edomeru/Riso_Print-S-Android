//
//  PrintSettingsOptionTableViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PreviewSetting;

/**
 * Controller for the screen listing the possible values of a print setting.
 * Selecting a print setting value automatically updates the preview.
 */
@interface PrintSettingsOptionTableViewController : UITableViewController

/**
 * Reference to the print setting and its possible values.
 */
@property (nonatomic, weak) NSDictionary *setting;

/**
 * Reference to the preview settings currently applied to the document object.
 */
@property (nonatomic, weak) PreviewSetting *previewSetting;

/**
 * Reference to the printer name.
 */
@property (nonatomic, weak) NSString *printerName;

@end
