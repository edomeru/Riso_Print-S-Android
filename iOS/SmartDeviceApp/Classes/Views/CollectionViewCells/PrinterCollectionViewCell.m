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
@property BOOL isDefaultPrinterCell;

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

- (IBAction)defaultSwitchAction:(id)sender
{
    if (((UISwitch *) sender).on == YES)
    {
        [self.delegate setDefaultPrinterCell:YES forIndexPath:self.indexPath];
        [self setAsDefaultPrinterCell: YES];
    }
    else
    {
        [self.delegate setDefaultPrinterCell:NO forIndexPath:self.indexPath];
        [self setAsDefaultPrinterCell: NO];

    }
}

-(void) setAsDefaultPrinterCell:(BOOL) isDefaultPrinterCell
{
    self.isDefaultPrinterCell = isDefaultPrinterCell;
    if(isDefaultPrinterCell == YES)
    {
        self.defaultSwitch.on = YES;
        [self setCellHeaderFormat:kPrinterCollectionCellTypeDefault];
    }
    else
    {
        self.defaultSwitch.on = NO;
        [self setCellHeaderFormat:kPrinterCollectionCellTypeNormal];

    }
}

-(void) setCellHeaderFormat:(kPrinterCollectionCellType) cellFormat
{
    switch(cellFormat)
    {
        case kPrinterCollectionCellTypeDefault:
        {
            [self.cellHeader setBackgroundColor:[UIColor gray4ThemeColor]];
            [self.nameLabel setTextColor:[UIColor whiteThemeColor]];
            [self.ipAddressLabel setTextColor:[UIColor whiteThemeColor]];
        }
            break;
        case kPrinterCollectionCellTypeDelete:
        {
            UIColor *bgColor = [UIColor purple2ThemeColor];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor whiteThemeColor]];
            [self.ipAddressLabel setTextColor:[UIColor whiteThemeColor]];
        }
            break;
        default:
        {
            UIColor *bgColor = [UIColor gray3ThemeColor];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor blackThemeColor]];
            [self.ipAddressLabel setTextColor:[UIColor blackThemeColor]];
        }
            break;
    }
}

-(void) setCellToBeDeletedState:(BOOL) isCellForDelete
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

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
