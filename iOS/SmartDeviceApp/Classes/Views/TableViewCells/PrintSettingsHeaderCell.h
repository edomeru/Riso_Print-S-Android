//
//  PrintSettingsHeaderCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * PrintSettingsHeaderCell class is used to display the print settings header.
 */
@interface PrintSettingsHeaderCell : UITableViewCell

/**
 * The label of the group setting
 */
@property (nonatomic, weak) IBOutlet UILabel *groupLabel;

/**
 * Line separator of the headers when collapsed.
 */
@property (nonatomic, weak) IBOutlet UIView *separator;

/**
 * Flag if the group is collapsed or expanded.
 * The setter of this automatically changes the value of the expansion label
 * to + if the group is collapsed and - if expanded.
 */
@property (nonatomic) BOOL expanded;

@end
