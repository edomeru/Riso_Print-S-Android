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
 Adds an icon to the cell to indicate that the search result
 has already been added.
 */
- (void)setCellAsOldResult;

/**
 Adds an icon to the cell to indicate that this search result
 can be added.
 */
- (void)setCellAsNewResult;

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
