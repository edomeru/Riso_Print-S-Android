//
//  PrinterCollectionViewCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterCollectionViewCell.h"
#import "UIColor+Theme.h"
#import "DeleteButton.h"

typedef enum {
    kPrinterCollectionCellTypeNormal,
    kPrinterCollectionCellTypeDefault,
    kPrinterCollectionCellTypeDelete
}kPrinterCollectionCellType;

@interface PrinterCollectionViewCell()

/**
 * Flag if the cell is the default printer.
 */
@property BOOL isDefaultPrinterCell;

/**
 * The label of the default print settings
 */
@property (weak, nonatomic) IBOutlet UILabel *defaultSettingsRowLabel;

/**
 * Sets the cell header color theme.
 * @param cellFormat one of the defined kPrinterCollectionCellType values
 */
- (void)setCellHeaderFormat:(kPrinterCollectionCellType)cellFormat;

@end

@implementation PrinterCollectionViewCell

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}


- (void)setAsDefaultPrinterCell:(BOOL)isDefaultPrinterCell
{
    [self.defaultPrinterSelection setTitle:NSLocalizedString(IDS_LBL_YES, @"YES") forSegmentAtIndex:0];
    [self.defaultPrinterSelection setTitle:NSLocalizedString(IDS_LBL_NO, @"NO") forSegmentAtIndex:1];
    
    self.isDefaultPrinterCell = isDefaultPrinterCell;
    if(isDefaultPrinterCell == YES)
    {
        [self.defaultPrinterSelection setEnabled:NO forSegmentAtIndex:1];
        [self setCellHeaderFormat:kPrinterCollectionCellTypeDefault];
    }
    else
    {
        [self.defaultPrinterSelection setEnabled:YES forSegmentAtIndex:1];
        [self.defaultPrinterSelection setSelectedSegmentIndex:1];
        [self setCellHeaderFormat:kPrinterCollectionCellTypeNormal];

    }
}

- (void)setCellHeaderFormat:(kPrinterCollectionCellType)cellFormat
{
    switch(cellFormat)
    {
        case kPrinterCollectionCellTypeDefault:
        {
            [self.cellHeader setBackgroundColor:[UIColor gray4ThemeColor]];
            [self.nameLabel setTextColor:[UIColor whiteThemeColor]];
        }
            break;
        case kPrinterCollectionCellTypeDelete:
        {
            UIColor *bgColor = [UIColor purple2ThemeColor];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor whiteThemeColor]];
        }
            break;
        default:
        {
            UIColor *bgColor = [UIColor gray3ThemeColor];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor blackThemeColor]];
        }
            break;
    }
}

- (void)setCellToBeDeletedState:(BOOL)isCellForDelete
{
    if(isCellForDelete == YES)
    {
        [self.deleteButton setHidden:NO];
        [self setCellHeaderFormat:kPrinterCollectionCellTypeDelete];
    }
    else
    {
        [self.deleteButton setHidden:YES];
        if(self.isDefaultPrinterCell)
        {
            [self setCellHeaderFormat:kPrinterCollectionCellTypeDefault];
        }
        else
        {
            [self setCellHeaderFormat:kPrinterCollectionCellTypeNormal];
        }
    }
}

- (void)setDefaultSettingsRowToSelected:(BOOL)isSelected;
{
    if(isSelected)
    {
        [self.defaultSettingsRow setBackgroundColor:[UIColor purple2ThemeColor]];
        [self.defaultSettingsRowLabel setTextColor:[UIColor whiteThemeColor]];
    }
    else
    {
       [self.defaultSettingsRow setBackgroundColor:[UIColor gray2ThemeColor]];
        [self.defaultSettingsRowLabel setTextColor:[UIColor blackThemeColor]];
    }
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
