//
//  SearchResultCell.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * SearchResultCell class is used to display the printers found on printer search.
 */
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
 @param printerIP
        IP address of the printer
 */
- (void)setContentsUsingName:(NSString*)printerName usingIP:(NSString*)printerIP;

/**
 Sets the cell's UI/layout attributes.
 */
- (void)setStyle:(BOOL)isLastCell;

@end
