//
//  SearchResultCell.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SearchResultCell : UITableViewCell

/**
 Adds an icon to the cell to indicate that the search result has 
 already been processed.
 */
- (void)setCellToAdded;

/**
 Adds a button to the cell for adding the search result.
 The specified receiver should handle the button's tap gesture.

 @param tag
        unique identifier for the button
 @param receiver
        receiver of the button's tap gesture
        (should conform to UIGestureRecognizerDelegate)
 @param action
        method of the receiver triggered by the tap gesture
 */
- (void)setCellToNew:(NSUInteger)tag handledBy:(id<UIGestureRecognizerDelegate>)receiver usingAction:(SEL)action;

/**
 Sets the printer name as the cell's main text.
 @param printerName
        name of the printer
 */
- (void)setContents:(NSString*)printerName;

/**
 Sets the cell's UI/layout attributes.
 */
- (void)setStyle:(BOOL)isLastCell;

@end
