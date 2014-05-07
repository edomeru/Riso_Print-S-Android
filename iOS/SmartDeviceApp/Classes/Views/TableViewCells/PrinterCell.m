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
    UIColor *bgColor = [UIColor purple2ThemeColor];
    [self setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor whiteThemeColor]];
    [self.deleteButton setHidden: NO];
    [self.disclosureImage setHidden: YES];
    [self.separator setBackgroundColor:[UIColor purple2ThemeColor]];
}

- (void) setCellStyleForDefaultCell
{
    self.isDefaultPrinterCell = YES;
    [self setBackgroundColor:[UIColor gray4ThemeColor]];
    [self.printerName setTextColor:[UIColor whiteThemeColor]];
    [self.deleteButton setHidden: YES];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor gray4ThemeColor]];
}

-(void) setCellStyleForNormalCell
{
    self.isDefaultPrinterCell = NO;
    UIColor *bgColor = [UIColor gray1ThemeColor];
    [self setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor blackThemeColor]];
    [self.deleteButton setHidden: YES];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor whiteThemeColor]];
}

@end
