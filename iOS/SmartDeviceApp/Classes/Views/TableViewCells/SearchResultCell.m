//
//  SearchResultCell.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/14/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "SearchResultCell.h"
#import "PrinterSearchViewController.h"

@interface SearchResultCell ()

@property (weak, nonatomic) IBOutlet UILabel* printerName;
@property (weak, nonatomic) IBOutlet UIView* separator;

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

- (void)putCheckmark
{
    self.accessoryView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"PrinterSearchResultsCheckIcon"]];
}

- (void)putPlusButton:(id<UIGestureRecognizerDelegate>)buttonOwner tapHandler:(SEL)actionOnTap
{
    UIImage* plusImage = [UIImage imageNamed:@"PrinterSearchResultsPlusIcon"];
    
    // create the button
    UIButton* plusButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [plusButton setImage:plusImage forState:UIControlStateNormal];
    [plusButton setEnabled:YES];
    plusButton.frame = CGRectMake(0, 0, plusImage.size.width, plusImage.size.height);
    
    // set the tap gesture handler
    [plusButton addTarget:buttonOwner
                   action:actionOnTap
         forControlEvents:UIControlEventTouchUpInside];
    
    self.accessoryView = plusButton;
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
