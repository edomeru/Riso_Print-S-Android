//
//  PrinterCollectionViewCell.m
//  SmartDeviceApp
//
//  Created by Seph on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterCollectionViewCell.h"

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
    if(isDefaultPrinterCell == YES)
    {
        self.defaultSwitch.on = YES;
        [self.cellHeader setBackgroundColor:[UIColor blackColor]];
        [self.nameLabel setTextColor:[UIColor whiteColor]];
    }
    else
    {
        self.defaultSwitch.on = NO;
        UIColor *bgColor = [UIColor colorWithRed:145.0/255.0 green:145.0/255.0 blue:145.0/255.0 alpha:1.0];
        [self.cellHeader setBackgroundColor: bgColor];
        [self.nameLabel setTextColor:[UIColor blackColor]];
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
