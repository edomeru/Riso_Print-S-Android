//
//  DeleteButton.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Classes that use the DeleteButton can conform to this protocol
 * to be notified of button events.
 */
@protocol DeleteButtonDelegate <NSObject>

@optional

/**
 * Asks the delegate if the button should be highlighted.
 * This is triggered when the button is tapped.
 * 
 * @param YES if it should be highlighted, NO otherwise
 */
- (BOOL)shouldHighlightButton;

@end

/**
 * Custom view for the "Delete" buttons.
 * This is used in the following screens:
 *  - "Printers" (Delete)
 *  - "Print Job History" (Delete)
 *  - "Print Job History" (Delete All)
 */
@interface DeleteButton : UIButton

/**
 * Reference to the class using the DeleteButton.
 */
@property (weak, nonatomic) id<DeleteButtonDelegate> delegate;

/**
 * Stores the background color to be used when the button is highlighted.
 */
@property (strong, nonatomic) UIColor* highlightedColor;

/**
 * Stores the text color to be used when the button is highlighted.
 */
@property (strong, nonatomic) UIColor* highlightedTextColor;

/**
 * Indicates whether the button should remain highlighted.
 * 
 * @param enable YES if it should remain highlighted, NO otherwise
 */
- (void)keepHighlighted:(BOOL)enable;

@end
