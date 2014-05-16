//
//  PrintJobItemCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

#define PRINTJOBCELL    @"PrintJobItem"

@interface PrintJobItemCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIView* separator;
@property (weak, nonatomic) IBOutlet UIImageView* result;
@property (weak, nonatomic) IBOutlet UILabel* name;
@property (weak, nonatomic) IBOutlet UILabel* timestamp;

- (void)setBackgroundColors;
- (void)markForDeletion:(BOOL)marked;

@end