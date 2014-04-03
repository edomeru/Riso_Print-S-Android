//
//  PrintJobHistoryGroupCell.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryGroupCell.h"
#import "UIColor+Theme.h"

#define TEXT_GROUP_COLLAPSED    @"+"
#define TEXT_GROUP_EXPANDED     @"-"

#define IMAGE_JOB_STATUS_OK     @"img_btn_job_status_ok"
#define IMAGE_JOB_STATUS_NG     @"img_btn_job_status_ng"

#define IDX_NAME                0
#define IDX_RESULT              1
#define IDX_TIMESTAMP           2

#define TAG_SEPARATOR           5

#define FORMAT_DATE_TIME        @"yyyy/MM/dd HH:mm"
#define FORMAT_ZONE             @"GMT"

@interface PrintJobHistoryGroupCell ()

#pragma mark - UI Properties

@property (weak, nonatomic) IBOutlet UIButton* groupName;
@property (weak, nonatomic) IBOutlet UIButton* groupIndicator;
@property (weak, nonatomic) IBOutlet UIButton* deleteAllButton;
@property (weak, nonatomic) IBOutlet UITableView* printJobsView;

/** Keeps track of the index of the print job that has the delete button. */
@property (strong, nonatomic) NSIndexPath* jobWithDelete;

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
    UITableViewCell* printJobCell = [tableView dequeueReusableCellWithIdentifier:PRINTJOBCELL
                                                                forIndexPath:indexPath];
    
    // get print job details (name, result, timestamp)
    NSArray* printJob = [self.listPrintJobs objectAtIndex:indexPath.row];
    
    // print job name
    printJobCell.textLabel.text = [NSString stringWithFormat:@"%@", printJob[IDX_NAME]];
    
    // print job result
    BOOL result = [printJob[IDX_RESULT] boolValue];
    if (result)
        printJobCell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_OK];
    else
        printJobCell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_NG];
    
    // print job timestamp
    NSDateFormatter* timestampFormat = [[NSDateFormatter alloc] init];
    [timestampFormat setDateFormat:FORMAT_DATE_TIME];
    [timestampFormat setTimeZone:[NSTimeZone timeZoneWithName:FORMAT_ZONE]]; //TODO: should depend on localization?
    printJobCell.detailTextLabel.text = [timestampFormat stringFromDate:printJob[IDX_TIMESTAMP]];
    printJobCell.detailTextLabel.hidden = NO;
    printJobCell.accessoryView = nil;
    
    // clear tracker for the delete button
    self.jobWithDelete = nil;
    
    return printJobCell;
}

- (void)tableView:(UITableView*)tableView willDisplayCell:(UITableViewCell*)cell forRowAtIndexPath:(NSIndexPath*)indexPath
{
    // unified version-independent fix for the buggy UITableViewCell background color
    //  -- for iOS6 (always clear)
    //  -- for iOS7 (always white) 
    // colors set to default in storyboard, set programmatically here instead
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        [cell setBackgroundColor:[UIColor gray2ThemeColor]]; //set to be darker than background
    else
        [cell setBackgroundColor:[UIColor gray1ThemeColor]];
    
    // if this is the last cell, hide the separator
    if (indexPath.row == [self.listPrintJobs count]-1)
        [[cell.contentView viewWithTag:TAG_SEPARATOR] setHidden:YES];
    else
        [[cell.contentView viewWithTag:TAG_SEPARATOR] setHidden:NO];
}

#pragma mark - Cell Data

- (void)initWithTag:(NSInteger)tag
{
    // set cell tags
    self.groupName.tag = tag;
    self.groupIndicator.tag = tag;
    self.deleteAllButton.tag = tag;
    self.printJobsView.tag = tag;
    
    // prepare container for print jobs list
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

#pragma mark - Cell UI

- (void)putDeleteButton:(UIGestureRecognizer*)gesture handledBy:(id<PrintJobHistoryGroupCellDelegate>)receiver usingActionOnTap:(SEL)actionOnTap
{
    // get the specific item swiped
    CGPoint swipedItem = [gesture locationInView:self.printJobsView];
    NSIndexPath* itemIndexPath = [self.printJobsView indexPathForRowAtPoint:swipedItem];
    if (itemIndexPath != nil)
    {
        NSLog(@"[INFO][PrintJobCell] swiped left on item=%ld", (long)itemIndexPath.row);
        
        // check if there is already an item with a delete button
        if ((self.jobWithDelete != nil) && (self.jobWithDelete.row != itemIndexPath.row))
        {
            NSLog(@"[INFO][PrintJobCell] canceling delete button for item=%ld", (long)self.jobWithDelete.row);
            UITableViewCell* printJobCell = [self.printJobsView cellForRowAtIndexPath:self.jobWithDelete];
            printJobCell.accessoryView = nil;
            printJobCell.detailTextLabel.hidden = NO;
        }
        self.jobWithDelete = itemIndexPath;
        
        // check if this item already has a delete button
        UITableViewCell* printJobCell = [self.printJobsView cellForRowAtIndexPath:itemIndexPath];
        if (printJobCell.accessoryView != nil)
        {
            NSLog(@"[INFO][PrintJobCell] already has delete button, ignore");
            return;
        }
        
        // create the delete button
        UIButton* deleteButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [deleteButton setTitle:@"DELETE" forState:UIControlStateNormal];
        [deleteButton setTitleEdgeInsets:UIEdgeInsetsMake(10.0f, 15.0f, 10.0f, 15.0f)];
        [deleteButton setBackgroundColor:[UIColor redThemeColor]];
        [deleteButton setUserInteractionEnabled:YES];
        deleteButton.titleLabel.font = [UIFont systemFontOfSize:13.0f];
        deleteButton.tag = (self.printJobsView.tag * TAG_FACTOR) + itemIndexPath.row; //<group>00<row>
        deleteButton.frame = CGRectMake(self.deleteAllButton.frame.origin.x,
                                        0,
                                        self.deleteAllButton.frame.size.width-15.0f,
                                        printJobCell.frame.size.height-10.0f);
        
        // set the handler for the tap action
        [deleteButton addTarget:receiver
                         action:actionOnTap
               forControlEvents:UIControlEventTouchUpInside];
        
        // add to the view
        printJobCell.accessoryView = deleteButton;
        printJobCell.detailTextLabel.hidden = YES;
    }
    else
    {
        // swiped outside of the UITableView
        NSLog(@"[INFO][PrintJobCell] swiped left outside of table");
        return;
    }
}

- (void)removeDeleteButton
{
    NSLog(@"[INFO][PrintJobCell] canceling delete button for item=%ld", (long)self.jobWithDelete.row);
    
    UITableViewCell* printJobCell = [self.printJobsView cellForRowAtIndexPath:self.jobWithDelete];
    printJobCell.accessoryView = nil;
    printJobCell.detailTextLabel.hidden = NO;
    self.jobWithDelete = nil;
}

- (void)reloadContents
{
    [self.printJobsView reloadData];
}

@end
