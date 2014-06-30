//
//  SearchingIndicator.h
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

#define HEIGHT_WHILE_REFRESHING 60.0f

/**
 * Custom view for the searching indicator used in the "Printer Search" screen.
 * This is used to add a fix for the bug in UIRefreshControl
 * where the frame is not properly resized it starts refreshing.\n
 * The bug is visible especially when the UIRefreshControl has a
 * background color different than its container's background color.
 */
@interface SearchingIndicator : UIRefreshControl

/**
 * Notifies the view whether the frame should be resized.
 *
 * @param invalid YES if frame should be resized, NO otherwise
 */
- (void)setFrameIsInvalid:(BOOL)invalid;

@end
