//
//  PrinterCollectionViewCell.m
//  SmartDeviceApp
//
//  Created by Seph on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterCollectionViewCell.h"

typedef enum {
    NORMAL,
    DEFAULT_PRINTER,
    DELETE
}T_CELLFORMAT;

@interface PrinterCollectionViewCell()
@property (weak, nonatomic) IBOutlet UIButton *deleteButton;
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
        [self setCellHeaderFormat:DEFAULT_PRINTER];
    }
    else
    {
        self.defaultSwitch.on = NO;
        [self setCellHeaderFormat:NORMAL];

    }
}

-(void) setCellHeaderFormat:(T_CELLFORMAT) cellFormat
{
    switch(cellFormat)
    {
        case DEFAULT_PRINTER:
        {
            [self.cellHeader setBackgroundColor:[UIColor colorWithRed:36.0/255.0 green:36.0/255.0 blue:36.0/255.0 alpha:1.0]];
            [self.nameLabel setTextColor:[UIColor whiteColor]];
        }
            break;
        case DELETE:
        {
            UIColor *bgColor = [UIColor colorWithRed:82.0/255.0 green:7.0/255.0 blue:182.0/255.0 alpha:1.0];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor whiteColor]];
        }
            break;
        default:
        {
            UIColor *bgColor = [UIColor colorWithRed:145.0/255.0 green:145.0/255.0 blue:145.0/255.0 alpha:1.0];
            [self.cellHeader setBackgroundColor: bgColor];
            [self.nameLabel setTextColor:[UIColor blackColor]];
        }
            break;
    }
}

-(void) setCellToBeDeletedState:(BOOL) isCellForDelete
{
    if(isCellForDelete == YES)
    {
        [self.deleteButton setHidden:NO];
        [self setCellHeaderFormat:DELETE];
    }
    else
    {
        [self.deleteButton setHidden:YES];
        if(self.isDefaultPrinterCell)
        {
            [self setCellHeaderFormat:DEFAULT_PRINTER];
        }
        else
        {
            [self setCellHeaderFormat:NORMAL];
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
