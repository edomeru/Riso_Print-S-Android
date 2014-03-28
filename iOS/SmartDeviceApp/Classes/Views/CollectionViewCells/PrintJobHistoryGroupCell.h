//
//  PrintJobHistoryItemCell.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

#define GROUPCELL   @"PrintJobHistoryGroup"
#define ITEMCELL    @"PrintJobHistoryItem"

@interface PrintJobHistoryGroupCell : UICollectionViewCell <UITableViewDataSource, UITableViewDelegate>

- (void)setCellTag:(NSInteger)tag;
- (void)setCellGroupName:(NSString*)name;
- (void)setCellIndicator:(BOOL)isCollapsed;
- (void)setCellPrintJobs:(NSArray*)printJobs;
- (void)reloadContents;

@end
