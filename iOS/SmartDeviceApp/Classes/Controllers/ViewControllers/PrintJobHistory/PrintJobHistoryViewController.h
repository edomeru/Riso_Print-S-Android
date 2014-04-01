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

@interface PrintJobHistoryViewController : UIViewController <UICollectionViewDataSource, UICollectionViewDelegate, PrintJobHistoryGroupCellDelegate>

/** The UI responsible for displaying the list of print job history items. */
@property (weak, nonatomic) IBOutlet UICollectionView* groupsView;

/** 
 Provides the exact size (width and height) of the group at the specified index path. 
 This is mainly dependent on the number of print jobs that the group will contain.
 */
- (CGSize)computeSizeForGroupAtIndexPath:(NSIndexPath*)indexPath;

@end
