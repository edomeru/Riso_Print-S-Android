//
//  SearchResultCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SearchResultCell.h"
#import "PrinterSearchViewController.h"

@interface SearchResultCell ()

@property (weak, nonatomic) IBOutlet UILabel* printerName;
@property (weak, nonatomic) IBOutlet UIView* separator;
@property (weak, nonatomic) IBOutlet UIButton* oldIcon;
@property (weak, nonatomic) IBOutlet UIButton* addIcon;

@end

@implementation SearchResultCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

- (void)setCellAsOldResult
{
//implementation #1 : create view programmatically
//    self.accessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_btn_search_printer_check"]];
    
//implementation #2 : create view in storyboard, then just set hidden attribute here
    [self.oldIcon setHidden:NO];
    [self.addIcon setHidden:YES];
}

- (void)setCellAsNewResult
{
//implementation #1 : create view programmatically
//    self.accessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_btn_search_printer_plus"]];

//implementation #2 : create view in storyboard, then just set hidden attribute here
    [self.oldIcon setHidden:YES];
    [self.addIcon setHidden:NO];
}

- (void)setContents:(NSString*)printerName
{
    self.printerName.text = printerName;
    
    //TODO: different fonts for ipad and iphone?
    self.printerName.font = [UIFont fontWithName:@"Helvetica Neue" size:17];
}

- (void)setStyle:(BOOL)isLastCell
{
    //fix for the bugged always-white cell in iPad iOS7
    self.backgroundColor = [UIColor clearColor];
    
    if (isLastCell)
        [self.separator setHidden:YES];
    else
        [self.separator setHidden:NO];
}

@end
