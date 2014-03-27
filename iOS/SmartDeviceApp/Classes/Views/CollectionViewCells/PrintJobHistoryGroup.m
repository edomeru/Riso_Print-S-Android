//
//  PrintJobHistoryItemCell.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryGroup.h"

@interface PrintJobHistoryGroup ()

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UILabel* groupName;
@property (weak, nonatomic) IBOutlet UITableView* tableView;

#pragma mark - Data Properties

@property (strong, nonatomic) NSArray* listPrintJobs;

#pragma mark - Methods

@end

@implementation PrintJobHistoryGroup

#pragma mark - Lifecycle

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.listPrintJobs count];
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:ITEMCELL
                                                            forIndexPath:indexPath];
    
    // get print job details
    NSString* printJobName = [self.listPrintJobs objectAtIndex:indexPath.row];
    
    // set cell contents
    cell.textLabel.text = [NSString stringWithFormat:@"%@", printJobName];
    if (indexPath.row%2 == 0)
        cell.imageView.image = [UIImage imageNamed:@"img_btn_job_status_ng"];
    else
        cell.imageView.image = [UIImage imageNamed:@"img_btn_job_status_ok"];
    cell.detailTextLabel.text = [NSString stringWithFormat:@"2014/3/27 12:3%d", indexPath.row];
    
    return cell;
}

#pragma mark - Cell Contents

- (void)setCellGroupName:(NSString*)name
{
    self.groupName.text = name;
}

- (void)setCellPrintJobs:(NSArray*)printJobs
{
    self.listPrintJobs = printJobs;
}

#pragma mark - Cell Actions

- (void)reloadContents
{
    [self.tableView reloadData];
}

@end
