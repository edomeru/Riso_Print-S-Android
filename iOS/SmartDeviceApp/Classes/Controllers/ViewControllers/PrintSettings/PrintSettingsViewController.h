//
//  PrintSettingsViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

/**
 * Controller for both the "Default Print Settings" and the "Print Settings" screen.
 * The "Default Print Settings" screen is accessed from the "Printers" screen.\n
 * The "Print Settings" screen is accessed from the "Print Preview" screen.\n
 * This serves as the container for the PrintSettingsPrinterViewController.\n
 */
@interface PrintSettingsViewController : SlidingViewController

/**
 * Stores the list index of the printer.
 * This is only used for the "Default Print Settings" screen, which transitions
 * from either the PrintersIpadViewController or the PrintersIphoneViewController.\n
 * In both controllers, the list index indicates which printer was selected.\n\n
 * If coming from the "Print Preview" screen, this is nil.
 */
@property (nonatomic, strong) NSNumber *printerIndex;

@end
