//
//  SearchResultCell.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SearchResultCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel* printerName;

/**
 Adds a checkmark image as the accessory view of the cell.
 */
- (void)putCheckmark;

/**
 Adds a plus button as the accessory view of the cell.
 The specified buttonOwner should handle the tap gesture.
 
 @param buttonOwner
        receiver of the button's tap action
        (should conform to UIGestureRecognizerDelegate)
 @param tapHandler
        method of the buttonOwner for handling the tap action
 */
- (void)putPlusButton:(id<UIGestureRecognizerDelegate>)buttonOwner tapHandler:(SEL)actionOnTap;

/**
 Sets the printer name as the cell's main text.
 @param printerName
        name of the printer
 */
- (void)setContents:(NSString*)printerName;

/**
 Sets the cell's UI/layout attributes.
 */
- (void)setStyle;

@end
