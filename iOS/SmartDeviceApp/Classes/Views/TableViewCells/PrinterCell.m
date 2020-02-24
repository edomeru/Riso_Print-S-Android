//
//  PrinterCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterCell.h"
#import "UIColor+Theme.h"

@interface PrinterCell()

@property (assign, nonatomic) BOOL isDefaultPrinterCell;
@property (weak, nonatomic) IBOutlet DeleteButton *deleteButton;
@property (weak, nonatomic) IBOutlet UIImageView *disclosureImage;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *spacePrinterNameToDisclosureButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *spaceDeleteButtonToSuperview;

/**
 * Shows the delete button.
 */
- (void)putDeleteButton;

/**
 * Removes the delete button.
 */
- (void)cancelDeleteButton;

@end

@implementation PrinterCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated
{
    [super setHighlighted:highlighted animated:animated];
    
    if (highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple2ThemeColor];
        [self.printerName setTextColor:[UIColor whiteThemeColor]];
        [self.ipAddress setTextColor:[UIColor whiteThemeColor]];
        [self.separator setBackgroundColor:[UIColor purple2ThemeColor]];
    }
    else
    {
        if(self.isDefaultPrinterCell)
        {
            [self setCellStyleForDefaultCell];
        }
        else
        {
             [self setCellStyleForNormalCell];
        }
    }
    
}

- (void)setCellToBeDeletedState:(BOOL)isCellForDelete
{
    if(isCellForDelete == YES)
    {
        [self setCellStyleForToDeleteCell];
    }
    else
    {
        if(self.isDefaultPrinterCell)
        {
            [self setCellStyleForDefaultCell];
        }
        else
        {
            [self setCellStyleForNormalCell];
        }
    }
}

- (void)setCellStyleForToDeleteCell
{
    UIColor *bgColor = [UIColor purple2ThemeColor];
    [self.contentView setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor whiteThemeColor]];
    [self.ipAddress setTextColor:[UIColor whiteThemeColor]];
    [self.disclosureImage setHidden: YES];
    [self.separator setBackgroundColor:[UIColor purple2ThemeColor]];
    [self putDeleteButton];
}

- (void)setCellStyleForDefaultCell
{
    self.isDefaultPrinterCell = YES;

    if (@available(iOS 13.0, *)) {
        [self.contentView setBackgroundColor:[UIColor colorNamed:@"color_gray5_gray3"]];
        [self.printerName setTextColor:[UIColor colorNamed:@"color_white_black"]];
        [self.ipAddress setTextColor:[UIColor colorNamed:@"color_white_black"]];
        [self.separator setBackgroundColor:[UIColor colorNamed:@"color_gray5_gray3"]];
    } else {
        [self.contentView setBackgroundColor:[UIColor gray4ThemeColor]];
        [self.printerName setTextColor:[UIColor whiteThemeColor]];
        [self.ipAddress setTextColor:[UIColor whiteThemeColor]];
        [self.separator setBackgroundColor:[UIColor gray4ThemeColor]];
    }


    [self.disclosureImage setHidden: NO];
    [self cancelDeleteButton];
}

- (void)setCellStyleForNormalCell
{
    self.isDefaultPrinterCell = NO;
    UIColor *bgColor = [UIColor gray1ThemeColor];

    if (@available(iOS 13.0, *)) {
        bgColor = [UIColor colorNamed:@"color_gray1_gray6"];
        [self.printerName setTextColor:[UIColor colorNamed:@"color_black_white"]];
        [self.ipAddress setTextColor:[UIColor colorNamed:@"color_black_white"]];
    } else {
        [self.printerName setTextColor:[UIColor blackThemeColor]];
        [self.ipAddress setTextColor:[UIColor blackThemeColor]];
    }

    [self.contentView setBackgroundColor:bgColor];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor whiteThemeColor]];
    [self cancelDeleteButton];
}

#pragma mark - Delete Button

- (void)setDeleteButtonLayout
{
    [self.deleteButton setTitle:[NSLocalizedString(IDS_LBL_DELETE, @"Delete") uppercaseString]
                       forState:UIControlStateNormal];
    
    self.deleteButton.highlightedColor = [UIColor purple1ThemeColor];
    self.deleteButton.highlightedTextColor = [UIColor whiteThemeColor];
}

- (void)putDeleteButton
{
    self.spacePrinterNameToDisclosureButton.constant += self.deleteButton.frame.size.width;
    self.spaceDeleteButtonToSuperview.constant = 16.0f;
    
    [self.printerName setNeedsUpdateConstraints];
    [self.deleteButton setNeedsUpdateConstraints];
    
    [UIView animateWithDuration:0.2f animations:^{
        [self layoutIfNeeded];
    }];
}

- (void)cancelDeleteButton
{
    if (self.spaceDeleteButtonToSuperview.constant < 0) //already hidden
    {
        return;
    }
    else
    {
        self.spacePrinterNameToDisclosureButton.constant = 16.0f; //from storyboard
        self.spaceDeleteButtonToSuperview.constant = -self.deleteButton.frame.size.width;
        
        [self.printerName setNeedsUpdateConstraints];
        [self.deleteButton setNeedsUpdateConstraints];
        
        [UIView animateWithDuration:0.2f animations:^{
            [self layoutIfNeeded];
            
        }];
    }
}

@end
