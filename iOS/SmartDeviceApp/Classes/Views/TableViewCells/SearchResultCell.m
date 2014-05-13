//
//  SearchResultCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SearchResultCell.h"
#import "PrinterSearchViewController.h"
#import "UIColor+Theme.h"

@interface SearchResultCell ()

@property (weak, nonatomic) IBOutlet UILabel* printerIP;
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

- (void)setCellAsOldResult
{
//implementation #1 : create view programmatically
//    self.accessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_btn_search_printer_check"]];
    
//implementation #2 : create view in storyboard, then just set hidden attribute here
    [self.oldIcon setHidden:NO];
    [self.addIcon setHidden:YES];
    
    [self setSelectionStyle:UITableViewCellSelectionStyleNone]; //disable cell selection
    self.selectedBackgroundView = nil;
    self.printerName.highlightedTextColor = [UIColor blackThemeColor];
    self.printerIP.highlightedTextColor = [UIColor blackThemeColor];
}

- (void)setCellAsNewResult
{
//implementation #1 : create view programmatically
//    self.accessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_btn_search_printer_plus"]];

//implementation #2 : create view in storyboard, then just set hidden attribute here
    [self.oldIcon setHidden:YES];
    [self.addIcon setHidden:NO];
    
    UIView* highlightColor = [[UIView alloc] init];
    highlightColor.backgroundColor = [UIColor purple1ThemeColor]; //same color of oldIcon
    self.selectedBackgroundView = highlightColor;
    self.printerName.highlightedTextColor = [UIColor whiteThemeColor];
    self.printerIP.highlightedTextColor = [UIColor whiteThemeColor];
}

- (void)setContentsUsingName:(NSString*)printerName usingIP:(NSString*)printerIP
{
    if (printerName == nil || [printerName isEqualToString:@""])
        self.printerName.text = NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name");
    else
        self.printerName.text = printerName;
    self.printerIP.text = printerIP;
    
    //TODO: different fonts for ipad and iphone?
    self.printerName.font = [UIFont fontWithName:@"Helvetica Neue" size:17.0f];
    self.printerIP.font = [UIFont fontWithName:@"Helvetica Neue" size:12.0f];
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
