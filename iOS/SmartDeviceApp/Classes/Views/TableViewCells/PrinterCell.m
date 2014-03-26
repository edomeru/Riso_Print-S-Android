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
@property (weak, nonatomic) IBOutlet UIImageView *disclosureImage;
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

-(void) setCellToBeDeletedState:(BOOL) isCellForDelete
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

- (void) setCellStyleForToDeleteCell
{
    //TODO: this only works for iOS7
    UIColor *bgColor = [UIColor colorWithRed:82.0/255.0 green:7.0/255.0 blue:182.0/255.0 alpha:1.0];
    [self setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor whiteColor]];
    [self.deleteButton setHidden: NO];
    [self.disclosureImage setHidden: YES];
    [self.separator setBackgroundColor:[UIColor colorWithRed:82.0/255.0
                                                       green:7.0/255.0
                                                        blue:182.0/255.0
                                                       alpha:1.0]];
}

- (void) setCellStyleForDefaultCell
{
    self.isDefaultPrinterCell = YES;
    [self setBackgroundColor:[UIColor colorWithRed:36.0/255.0 green:36.0/255.0 blue:36.0/255.0 alpha:1.0]];
    [self.printerName setTextColor:[UIColor whiteColor]];
    [self.deleteButton setHidden: YES];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor colorWithRed:36.0/255.0
                                                       green:36.0/255.0
                                                        blue:36.0/255.0
                                                       alpha:1.0]];
}

-(void) setCellStyleForNormalCell
{
    self.isDefaultPrinterCell = NO;
    UIColor *bgColor = [UIColor colorWithRed:205.0/255.0 green:205.0/255.0 blue:205.0/255.0 alpha:1.0];
    [self setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor blackColor]];
    [self.deleteButton setHidden: YES];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor colorWithRed:255.0/255.0
                                                       green:255.0/255.0
                                                        blue:255.0/255.0
                                                       alpha:1.0]];
}

@end
