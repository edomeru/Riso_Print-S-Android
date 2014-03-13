//
//  PrinterCell.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterCell.h"
@interface PrinterCell()
@property UIColor *normalBackgroundColor;
@end
@implementation PrinterCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        self.normalBackgroundColor = [self backgroundColor];
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
    if(isDefaultPrinterCell == YES)
    {
        [self setBackgroundColor:[UIColor blackColor]];
        [self.printerName setTextColor:[UIColor whiteColor]];
    }
    else
    {
        [self setBackgroundColor:self.normalBackgroundColor];
        [self.printerName setTextColor:[UIColor blackColor]];
    }
}

@end
