//
//  PrintSettingsPrinterViewController.h
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PrintSettingsPrinterViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>
@property (nonatomic, strong) NSNumber *printerIndex;
@end
