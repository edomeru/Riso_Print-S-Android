//
//  PrintJobHistoryGroupCell.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobHistoryGroupCell.h"
#import "UIColor+Theme.h"
#import "NSDate+Format.h"

#define IMAGE_JOB_STATUS_OK     @"img_btn_job_status_ok"
#define IMAGE_JOB_STATUS_NG     @"img_btn_job_status_ng"

#define IDX_RESULT              0
#define IDX_NAME                1
#define IDX_TIMESTAMP           2

#define TAG_SEPARATOR           5

@interface PrintJobHistoryGroupCell ()

#pragma mark - UI Properties

/** 
 Displays the name of the group (printer name) and acts as
 the toggle switch for collapsing/expanding the group.
 */
@property (weak, nonatomic) IBOutlet UIButton* groupName;

/**
 Displays collapsed/expanded state of the group and acts as
 the toggle switch for collapsing/expanding the group.
 */
@property (weak, nonatomic) IBOutlet UIButton* groupIndicator;

/** Removes the entire group. */
@property (weak, nonatomic) IBOutlet UIButton* deleteAllButton;

/** The UI for displaying the list of print jobs. */
@property (weak, nonatomic) IBOutlet UITableView* printJobsView;

/** Keeps track of the index of the print job that has the delete button. */
@property (strong, nonatomic) NSIndexPath* jobWithDelete;

#pragma mark - Data Properties

/** The data source for the list of print jobs (result, name, timestamp). */
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
    
    // print job result
    BOOL result = [[printJob objectAtIndex:IDX_RESULT] boolValue];
    if (result)
        printJobCell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_OK];
    else
        printJobCell.imageView.image = [UIImage imageNamed:IMAGE_JOB_STATUS_NG];
    
    // print job name
    printJobCell.textLabel.text = [NSString stringWithFormat:@"%@", [printJob objectAtIndex:IDX_NAME]];
    
    // print job timestamp
    printJobCell.detailTextLabel.text = [[printJob objectAtIndex:IDX_TIMESTAMP] formattedString];
    printJobCell.detailTextLabel.hidden = NO;
    
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
        [self.groupIndicator setTitle:@"+" forState:UIControlStateNormal];
    else
        [self.groupIndicator setTitle:@"-" forState:UIControlStateNormal];
}

- (void)putPrintJob:(NSString*)name withResult:(BOOL)result withTimestamp:(NSDate*)timestamp
{
    // store the print job details in a ordered array
    // [0] print job result
    // [1] print job name
    // [2] print job timestamp
    NSArray* printJob = [NSArray arrayWithObjects:[NSNumber numberWithInt:(result ? 1 : 0)],
                                                  name,
                                                  timestamp,
                                                  nil];
    [self.listPrintJobs addObject:printJob];
}

#pragma mark - Cell UI

- (void)putDeleteButton:(UIGestureRecognizer*)gesture handledBy:(id<PrintJobHistoryGroupCellDelegate>)receiver usingActionOnTap:(SEL)actionOnTap
{
    // get the specific item swiped
    CGPoint swipedJob = [gesture locationInView:self.printJobsView];
    NSIndexPath* jobIndexPath = [self.printJobsView indexPathForRowAtPoint:swipedJob];
    if (jobIndexPath == nil)
    {
        // swiped outside of the UITableView
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
        NSLog(@"[INFO][PrintJobCell] swiped left outside of table");
#endif
        return;
    }
    else
    {
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
        NSLog(@"[INFO][PrintJobCell] swiped left on item=%ld", (long)jobIndexPath.row);
#endif
        
        // check if there is already a job with a delete button
        if ((self.jobWithDelete != nil) && (self.jobWithDelete.row != jobIndexPath.row))
        {
            [self removeDeleteButton];
        }
        self.jobWithDelete = jobIndexPath;
        
        // check if this item already has a delete button
        UITableViewCell* printJobCell = [self.printJobsView cellForRowAtIndexPath:jobIndexPath];
        if ([[printJobCell.contentView subviews] count] == 5) //has an extra delete button
        {
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
            NSLog(@"[INFO][PrintJobCell] already has delete button, ignoring swipe");
#endif
            return;
        }
        
        // create the delete button
        UIButton* deleteButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [deleteButton setTitle:NSLocalizedString(IDS_LBL_DELETE, @"Delete")
                      forState:UIControlStateNormal]; 
        [deleteButton setTitleColor:[UIColor blackThemeColor] forState:UIControlStateNormal];
        [deleteButton setTitleEdgeInsets:UIEdgeInsetsMake(10.0f, 15.0f, 10.0f, 15.0f)];
        [deleteButton setBackgroundColor:[UIColor whiteThemeColor]];
        [deleteButton setUserInteractionEnabled:YES];
        deleteButton.titleLabel.font = [UIFont systemFontOfSize:13.0f];
        deleteButton.tag = (self.printJobsView.tag * TAG_FACTOR) + jobIndexPath.row; //<group>00<row>
        deleteButton.frame = CGRectMake(printJobCell.frame.size.width, //initial position offscreen
                                        5.0f,
                                        self.deleteAllButton.frame.size.width-15.0f,
                                        printJobCell.frame.size.height-10.0f);
        
        // set the handler for the tap action
        [deleteButton addTarget:receiver
                         action:actionOnTap
               forControlEvents:UIControlEventTouchUpInside];
        
        // add to the view
        printJobCell.detailTextLabel.hidden = YES;
        [printJobCell.textLabel setTextColor:[UIColor whiteThemeColor]];
        [printJobCell setBackgroundColor:[UIColor purple2ThemeColor]];
        [printJobCell.contentView addSubview:deleteButton]; //will be added at the end of the subviews list
        
        // slide the button from offscreen to its place
        [UIView animateWithDuration:0.2 animations:^
        {
            deleteButton.frame = CGRectMake(self.deleteAllButton.frame.origin.x+5.0f, //final position onscreen
                                            5.0f,
                                            self.deleteAllButton.frame.size.width-15.0f,
                                            printJobCell.frame.size.height-10.0f);
        }];
    }
}

- (void)removeDeleteButton
{
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
    NSLog(@"[INFO][PrintJobCell] canceling delete button for item=%ld", (long)self.jobWithDelete.row);
#endif

    // get the delete button
    UITableViewCell* printJobCell = [self.printJobsView cellForRowAtIndexPath:self.jobWithDelete];
    UIButton* deleteButton = (UIButton*)[[printJobCell.contentView subviews] lastObject];
    
    // slide the button to offscreen
    __weak PrintJobHistoryGroupCell* weakSelf = self;
    [UIView animateWithDuration:0.2 animations:^
    {
         deleteButton.frame = CGRectMake(printJobCell.frame.size.width, //final position offscreen
                                         5.0f,
                                         weakSelf.deleteAllButton.frame.size.width-15.0f,
                                         printJobCell.frame.size.height-10.0f);
    } completion:^(BOOL finished)
    {
        [deleteButton removeFromSuperview];
        printJobCell.detailTextLabel.hidden = NO;
        [printJobCell.textLabel setTextColor:[UIColor blackThemeColor]];
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
            [printJobCell setBackgroundColor:[UIColor gray2ThemeColor]]; //set to be darker than background
        else
            [printJobCell setBackgroundColor:[UIColor gray1ThemeColor]];
    }];
    
    self.jobWithDelete = nil;
}

- (void)reloadContents
{
    // if there is a DELETE button displayed, cancel it first
    // (to avoid the DELETE button appearing on another cell, since the cells are reusable)
    if (self.jobWithDelete != nil)
        [self removeDeleteButton];
       
    [self.printJobsView reloadData];
}

@end
