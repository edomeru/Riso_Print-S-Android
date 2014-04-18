//
//  PrintSettingsHeaderCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSettingsHeaderCell.h"
#import "UIColor+Theme.h"

@interface PrintSettingsHeaderCell()

@property (nonatomic, weak) IBOutlet UILabel *expansionLabel;

@end

@implementation PrintSettingsHeaderCell

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

    self.contentView.backgroundColor = [UIColor blackThemeColor];
}

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated
{
    [super setHighlighted:highlighted animated:animated];
    
    if (self.highlighted)
    {
        self.contentView.backgroundColor = [UIColor purple1ThemeColor];
    }
    else
    {
        self.contentView.backgroundColor = [UIColor blackThemeColor];
    }
}

- (void)setExpanded:(BOOL)expanded
{
    _expanded = expanded;
    if (_expanded == YES)
    {
        self.expansionLabel.text = @"-";
    }
    else
    {
        self.expansionLabel.text = @"+";
    }
}

@end
