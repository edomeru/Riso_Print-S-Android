//
//  PrintJobHistoryViewController.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeViewController.h"
#import "UIViewController+Segue.h"
#import "PrintJobHistoryGroupCell.h"
#import "PrintJobHistoryLayout.h"

@interface PrintJobHistoryViewController : UIViewController <UICollectionViewDataSource, UICollectionViewDelegate, PrintJobHistoryGroupCellDelegate, PrintJobHistoryLayoutDelegate>

@end
