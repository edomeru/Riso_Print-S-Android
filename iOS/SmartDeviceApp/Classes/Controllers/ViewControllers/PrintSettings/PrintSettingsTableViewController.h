//
//  PrintSettingsTableViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsTableViewController : UITableViewController<UITextFieldDelegate>
- (IBAction)unwindToPrintSettings:(UIStoryboardSegue *)sender;
@end
