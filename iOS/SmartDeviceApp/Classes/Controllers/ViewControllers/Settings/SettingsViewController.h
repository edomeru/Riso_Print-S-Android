//
//  SettingsViewController.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SettingsViewController : UIViewController <UITextFieldDelegate>

- (IBAction)unwindToSettings:(UIStoryboardSegue *)sender;
@end
