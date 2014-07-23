//
//  SettingsViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Controller for the "Settings" screen (phone and tablet).
 */
@interface SettingsViewController : UIViewController <UITextFieldDelegate>

/**
 * Unwind segue back to the "Settings" screen.
 * Called when transitioning back to the "Settings"
 * screen from the the Main Menu panel.
 *
 * @param sender the segue object
 */
- (IBAction)unwindToSettings:(UIStoryboardSegue *)sender;

@end
