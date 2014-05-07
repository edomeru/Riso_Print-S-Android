//
//  PrintJobHistoryViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeViewController.h"
#import "UIViewController+Segue.h"
#import "PrintJobHistoryGroupCell.h"
#import "PrintJobHistoryLayout.h"

@interface PrintJobHistoryViewController : UIViewController <UICollectionViewDataSource, UICollectionViewDelegate, PrintJobHistoryGroupCellDelegate, PrintJobHistoryLayoutDelegate>

@end
