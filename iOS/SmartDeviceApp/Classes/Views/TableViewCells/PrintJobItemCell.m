//
//  PrintJobItemCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobItemCell.h"
#import "UIColor+Theme.h"
#import "DeleteButton.h"

@interface PrintJobItemCell ()

/**
 * Constraint for the horizontal space of the delete button to the content view.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint* spaceDeleteButtonToSuperView;

/**
 * Shows the delete button.
 */
- (void)putDeleteButton;

/**
 * Removes the delete button.
 */
- (void)cancelDeleteButton;

@end

@implementation PrintJobItemCell

#pragma mark - Lifecycle

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
    }
    return self;
}

#pragma mark - UI Properties

- (void)setBackgroundColors
{
    UIView* normalBackground = [[UIView alloc] init];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        normalBackground.backgroundColor = [UIColor gray2ThemeColor];
    else
        normalBackground.backgroundColor = [UIColor gray1ThemeColor];
    self.backgroundView = normalBackground;
    
    UIView* highlightedBackground = [[UIView alloc] init];
    highlightedBackground.backgroundColor = [UIColor purple2ThemeColor];
    self.selectedBackgroundView = highlightedBackground;
}

- (void)setDeleteButtonLayout
{
    [self.deleteButton setTitle:[NSLocalizedString(IDS_LBL_DELETE, @"Delete") uppercaseString]
                       forState:UIControlStateNormal];
    
    self.deleteButton.highlightedColor = [UIColor purple1ThemeColor];
    self.deleteButton.highlightedTextColor = [UIColor whiteThemeColor];
}

- (void)markForDeletion:(BOOL)marked
{
    if (marked)
    {
        self.timestamp.hidden = YES;
        [self.name setTextColor:[UIColor whiteThemeColor]];
        [self.backgroundView setBackgroundColor:[UIColor purple2ThemeColor]];
        [self putDeleteButton];
    }
    else
    {
        self.timestamp.hidden = NO;
        [self.name setTextColor:[UIColor blackColor]];
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
            [self.backgroundView setBackgroundColor:[UIColor gray2ThemeColor]];
        else
            [self.backgroundView setBackgroundColor:[UIColor gray1ThemeColor]];
        [self cancelDeleteButton];
    }
}

#pragma mark - Delete Button 

- (void)putDeleteButton
{
    self.spaceDeleteButtonToSuperView.constant = 8.0f;
    [self.deleteButton setNeedsUpdateConstraints];
    
    [UIView animateWithDuration:0.2f animations:^{
        [self layoutIfNeeded];
    }];
}

- (void)cancelDeleteButton
{
    if (self.spaceDeleteButtonToSuperView.constant < 0) //already hidden
    {
        return;
    }
    else
    {
        self.spaceDeleteButtonToSuperView.constant = -self.deleteButton.frame.size.width;
        [self.deleteButton setNeedsUpdateConstraints];
        
        [UIView animateWithDuration:0.2f animations:^{
            [self layoutIfNeeded];
        }];
    }
}

@end
