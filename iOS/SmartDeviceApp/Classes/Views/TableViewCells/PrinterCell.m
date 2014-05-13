//
//  PrinterCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterCell.h"
#import "UIColor+Theme.h"
#import "DeleteButton.h"

#define DELETE_BUTTON_TAG   99

@interface PrinterCell()
@property BOOL isDefaultPrinterCell;
@property (weak, nonatomic) IBOutlet UIButton *deleteButton; //hidden placeholder
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



- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated
{
    [super setHighlighted:highlighted animated:animated];
    
    if (highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple2ThemeColor];
    }
    else
    {
        if(self.isDefaultPrinterCell)
        {
            self.contentView.backgroundColor = [UIColor gray4ThemeColor];
        }
        else
        {
            self.contentView.backgroundColor = [UIColor gray1ThemeColor];
        }
    }
    
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
    [self.contentView setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor whiteThemeColor]];
    [self.ipAddress setTextColor:[UIColor whiteThemeColor]];
    [self.disclosureImage setHidden: YES];
    [self.separator setBackgroundColor:[UIColor purple2ThemeColor]];
    
    // initial position offscreen
    CGRect startPos = CGRectMake(self.frame.size.width,
                                 5.0f,
                                 self.deleteButton.frame.size.width,
                                 self.deleteButton.frame.size.height);
    // final position onscreen
    CGRect endPos = CGRectMake(self.deleteButton.frame.origin.x,
                               5.0f,
                               self.deleteButton.frame.size.width,
                               self.deleteButton.frame.size.height);
    DeleteButton* deleteButton = [DeleteButton createAtOffscreenPosition:startPos
                                                    withOnscreenPosition:endPos];
    deleteButton.tag = DELETE_BUTTON_TAG;
    [deleteButton addTarget:self
                     action:@selector(tappedDeletePrinter:)
           forControlEvents:UIControlEventTouchUpInside];
    [self.contentView addSubview:deleteButton];
    [deleteButton animateOnscreen:nil];
}

- (void) setCellStyleForDefaultCell
{
    self.isDefaultPrinterCell = YES;
    [self.contentView setBackgroundColor:[UIColor gray4ThemeColor]];
    [self.printerName setTextColor:[UIColor whiteThemeColor]];
    [self.ipAddress setTextColor:[UIColor whiteThemeColor]];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor gray4ThemeColor]];
    [self cancelDeleteButton];
}

-(void) setCellStyleForNormalCell
{
    self.isDefaultPrinterCell = NO;
    UIColor *bgColor = [UIColor gray1ThemeColor];
    [self.contentView setBackgroundColor:bgColor];
    [self.printerName setTextColor:[UIColor blackThemeColor]];
    [self.ipAddress setTextColor:[UIColor blackThemeColor]];
    [self.disclosureImage setHidden: NO];
    [self.separator setBackgroundColor:[UIColor whiteThemeColor]];
    [self cancelDeleteButton];
}

- (void)tappedDeletePrinter:(UIButton*)button
{
    [self.delegate didTapDeleteButton];
}

- (void)cancelDeleteButton
{
    DeleteButton* deleteButton = (DeleteButton*)[self.contentView viewWithTag:DELETE_BUTTON_TAG];
    if (deleteButton != nil)
    {
        [deleteButton animateOffscreen:^(BOOL finished)
         {
             [deleteButton removeFromSuperview];
         }];
    }
}

@end
