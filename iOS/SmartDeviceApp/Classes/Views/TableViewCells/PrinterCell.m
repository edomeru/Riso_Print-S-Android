//
//  PrinterCell.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterCell.h"

@interface PrinterCell()
@property BOOL isDefaultPrinterCell;
@property (weak, nonatomic) IBOutlet UIButton *deleteButton;
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

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void) setAsDefaultPrinterCell:(BOOL) isDefaultPrinterCell
{
    self.isDefaultPrinterCell = isDefaultPrinterCell;
    if(isDefaultPrinterCell == YES)
    {
        //TODO: this only works for iOS7
        [self setBackgroundColor:[UIColor blackColor]];
        [self.printerName setTextColor:[UIColor whiteColor]];
    }
    else
    {
        //TODO: this only works for iOS7
        UIColor *bgColor = [UIColor colorWithRed:173.0/255.0 green:173.0/255.0 blue:173.0/255.0 alpha:1.0];
        [self setBackgroundColor:bgColor];
        [self.printerName setTextColor:[UIColor blackColor]];
    }
}

-(void) setCellToBeDeletedState:(BOOL) isCellForDelete
{
    if(isCellForDelete == YES)
    {
        //TODO: this only works for iOS7
        UIColor *bgColor = [UIColor colorWithRed:82.0/255.0 green:7.0/255.0 blue:182.0/255.0 alpha:1.0];
        [self setBackgroundColor:bgColor];
        [self.printerName setTextColor:[UIColor whiteColor]];
        [self.deleteButton setHidden: NO];
    }
    else
    {
        [self setAsDefaultPrinterCell:self.isDefaultPrinterCell];
         [self.deleteButton setHidden: YES];
    }
}


@end
