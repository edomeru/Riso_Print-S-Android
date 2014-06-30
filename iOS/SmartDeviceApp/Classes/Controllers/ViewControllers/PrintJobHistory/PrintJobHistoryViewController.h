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

/**
 * Controller for the "Print Job History" screen (phone and tablet).
 */
@interface PrintJobHistoryViewController : UIViewController <UICollectionViewDataSource, UICollectionViewDelegate, PrintJobHistoryGroupCellDelegate, PrintJobHistoryLayoutDelegate, UIScrollViewDelegate, UIGestureRecognizerDelegate>

@end
