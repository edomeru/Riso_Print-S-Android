//
//  PrintSettingsPrinterViewController.h
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Controller for the screen displaying the currently selected 
 * printer and the list of print settings.
 * This also serves as the container for the PrintSettingsTableViewController.\n
 * If this is for the "Print Settings" screen, a print button is displayed at the top.\n
 * If this is for the "Default Print Settings" screen, there is no print button.\n\n
 * Selecting the printer triggers a transition to the PrintSettingsPrinterListViewController.\n
 */
@interface PrintSettingsPrinterViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

/**
 * Stores the list index of the printer.
 * This is only used for the "Default Print Settings" screen, which transitions
 * from either the PrintersIpadViewController or the PrintersIphoneViewController.\n
 * In both controllers, the list index indicates which printer was selected.\n\n
 * If coming from the "Print Preview" screen, this is nil.
 */
@property (nonatomic, strong) NSNumber *printerIndex;

@end
