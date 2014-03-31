//
//  PrintJobHistoryItemCell.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryGroupCell.h"

#define TEXT_GROUP_COLLAPSED    @"+"
#define TEXT_GROUP_EXPANDED     @"-"

#define IMAGE_JOB_STATUS_OK     @"img_btn_job_status_ok"
#define IMAGE_JOB_STATUS_NG     @"img_btn_job_status_ng"

#define IDX_NAME                0
#define IDX_RESULT              1
#define IDX_TIMESTAMP           2

#define FORMAT_DATE_TIME        @"yyyy/MM/dd HH:mm"
#define FORMAT_ZONE             @"GMT"

@interface PrintJobHistoryGroupCell ()

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UIButton* groupName;
@property (weak, nonatomic) IBOutlet UIButton* groupIndicator;
@property (weak, nonatomic) IBOutlet UIButton* deleteAllButton;
@property (weak, nonatomic) IBOutlet UITableView* tableView;

#pragma mark - Data Properties

@property (strong, nonatomic) NSMutableArray* listPrintJobs;

#pragma mark - Methods

@end

@implementation PrintJobHistoryGroupCell

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
    
    // get print job details (name, result, timestamp)
    NSArray* printJob = [self.listPrintJobs objectAtIndex:indexPath.row];
    
    // print job name
    cell.textLabel.text = [NSString stringWithFormat:@"%@", printJob[IDX_NAME]];
    
    // print job result
    BOOL result = [printJob[IDX_RESULT] boolValue];
    if (result)
        cell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_OK];
    else
        cell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_NG];
    
    // print job timestamp
    NSDateFormatter* timestampFormat = [[NSDateFormatter alloc] init];
    [timestampFormat setDateFormat:FORMAT_DATE_TIME];
    [timestampFormat setTimeZone:[NSTimeZone timeZoneWithName:FORMAT_ZONE]]; //TODO: should depend on localization?
    cell.detailTextLabel.text = [timestampFormat stringFromDate:printJob[IDX_TIMESTAMP]];
    
    //fix for the bugged always-white cell in iPad iOS7
    cell.backgroundColor = [UIColor clearColor];
    
    return cell;
}

#pragma mark - Cell Contents

- (void)initWithTag:(NSInteger)tag
{
    self.groupName.tag = tag;
    self.groupIndicator.tag = tag;
    self.deleteAllButton.tag = tag;
    
    self.listPrintJobs = [NSMutableArray array];
}

- (void)putGroupName:(NSString*)name
{
    [self.groupName setTitle:name forState:UIControlStateNormal];
}

- (void)putIndicator:(BOOL)isCollapsed
{
    if (isCollapsed)
        [self.groupIndicator setTitle:TEXT_GROUP_COLLAPSED forState:UIControlStateNormal];
    else
        [self.groupIndicator setTitle:TEXT_GROUP_EXPANDED forState:UIControlStateNormal];
}

- (void)putPrintJob:(NSString*)name withResult:(BOOL)result withTimestamp:(NSDate*)timestamp
{
    // store the print job details in a ordered array
    // [0] print job name
    // [1] print job result
    // [2] print job timestamp
    NSArray* printJob = [NSArray arrayWithObjects:name,
                                                  [NSNumber numberWithInt:(result ? 1 : 0)],
                                                  timestamp,
                                                  nil];
    [self.listPrintJobs addObject:printJob];
}

#pragma mark - Cell Actions

- (void)reloadContents
{
    [self.tableView reloadData];
}

@end
