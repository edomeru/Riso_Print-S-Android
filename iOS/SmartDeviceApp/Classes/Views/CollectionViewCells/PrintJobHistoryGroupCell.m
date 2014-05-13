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
#import "DeleteButton.h"

#define IMAGE_JOB_STATUS_OK     @"img_btn_job_status_ok"
#define IMAGE_JOB_STATUS_NG     @"img_btn_job_status_ng"

#define IDX_RESULT              0
#define IDX_NAME                1
#define IDX_TIMESTAMP           2

@interface PrintJobHistoryGroupCell ()

#pragma mark - UI Properties

/** 
 Displays the name of the group (printer name) and acts as
 the toggle switch for collapsing/expanding the group.
 */
@property (weak, nonatomic) IBOutlet UIButton* groupName;

/**
 Displays the IP address of the printer and acts as
 the toggle switch for collapsing/expanding the group.
 */
@property (weak, nonatomic) IBOutlet UIButton* groupIP;

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

- (void)putDeleteButton:(UIGestureRecognizer*)gesture;
- (void)colorHeader;
- (void)clearHeader;
- (void)tappedHeader;
- (void)colorDeleteAll;
- (void)clearDeleteAll;
- (void)tappedDeleteAll;
- (void)tappedDeleteJob:(UIButton*)button;

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
    PrintJobItemCell* cell = [tableView dequeueReusableCellWithIdentifier:PRINTJOBCELL
                                                                 forIndexPath:indexPath];
    
    // get print job details (result, name, imestamp)
    NSArray* printJob = [self.listPrintJobs objectAtIndex:indexPath.row];
    
    // print job result
    BOOL result = [[printJob objectAtIndex:IDX_RESULT] boolValue];
    if (result)
        cell.result.image = [UIImage imageNamed:IMAGE_JOB_STATUS_OK];
    else
        cell.result.image = [UIImage imageNamed:IMAGE_JOB_STATUS_NG];
    
    // print job name
    cell.name.text = [NSString stringWithFormat:@"%@", [printJob objectAtIndex:IDX_NAME]];
    
    // print job timestamp
    cell.timestamp.text = [[printJob objectAtIndex:IDX_TIMESTAMP] formattedString];
    cell.timestamp.hidden = NO;
    
    UISwipeGestureRecognizer* swipeLeft = [[UISwipeGestureRecognizer alloc]
                                           initWithTarget:self                                                                                       action:@selector(putDeleteButton:)];
    swipeLeft.direction = UISwipeGestureRecognizerDirectionLeft;
    [cell addGestureRecognizer:swipeLeft];
    
    // clear tracker for the delete button
    self.jobWithDelete = nil;
    
    return cell;
}

- (void)tableView:(UITableView*)tableView willDisplayCell:(UITableViewCell*)cell forRowAtIndexPath:(NSIndexPath*)indexPath
{
    PrintJobItemCell* jobCell = (PrintJobItemCell*)cell;
    
    // unified version-independent fix for the buggy UITableViewCell background color
    //  -- for iOS6 (always clear)
    //  -- for iOS7 (always white) 
    // colors set to default in storyboard, set programmatically here instead
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        [jobCell setBackgroundColor:[UIColor gray2ThemeColor]]; //set to be darker than background
    else
        [jobCell setBackgroundColor:[UIColor gray1ThemeColor]];
    
    // if this is the last cell, hide the separator
    if (indexPath.row == [self.listPrintJobs count]-1)
        [jobCell.separator setHidden:YES];
    else
        [jobCell.separator setHidden:NO];
}

#pragma mark - Cell Initialization

- (void)initWithTag:(NSInteger)tag
{
    // set cell tags
    self.tag = tag;
    self.groupName.tag = tag;
    self.groupIP.tag = tag;
    self.groupIndicator.tag = tag;
    self.deleteAllButton.tag = tag;
    self.printJobsView.tag = tag;
    
    // prepare container for print jobs list
    self.listPrintJobs = [NSMutableArray array];
    
    // set group header events
    
    [self.groupName addTarget:self action:@selector(colorHeader)
             forControlEvents:UIControlEventTouchDown];
    [self.groupName addTarget:self action:@selector(clearHeader)
             forControlEvents:UIControlEventTouchDragOutside];
    [self.groupName addTarget:self action:@selector(tappedHeader)
             forControlEvents:UIControlEventTouchUpInside];
    
    [self.groupIP addTarget:self action:@selector(colorHeader)
           forControlEvents:UIControlEventTouchDown];
    [self.groupIP addTarget:self action:@selector(clearHeader)
           forControlEvents:UIControlEventTouchDragOutside];
    [self.groupIP addTarget:self action:@selector(tappedHeader)
           forControlEvents:UIControlEventTouchUpInside];
    
    [self.groupIndicator addTarget:self action:@selector(colorHeader)
                  forControlEvents:UIControlEventTouchDown];
    [self.groupIndicator addTarget:self action:@selector(clearHeader)
                  forControlEvents:UIControlEventTouchDragOutside];
    [self.groupIndicator addTarget:self action:@selector(tappedHeader)
                  forControlEvents:UIControlEventTouchUpInside];
    
    [self.deleteAllButton addTarget:self action:@selector(colorDeleteAll)
                   forControlEvents:UIControlEventTouchDown];
    [self.deleteAllButton addTarget:self action:@selector(clearDeleteAll)
                   forControlEvents:UIControlEventTouchDragOutside];
    [self.deleteAllButton addTarget:self action:@selector(tappedDeleteAll)
                   forControlEvents:UIControlEventTouchUpInside];
}

#pragma mark - Cell Setters

- (void)putGroupName:(NSString*)name
{
    if (name == nil || [name isEqualToString:@""])
        [self.groupName setTitle:NSLocalizedString(@"IDS_LBL_NO_NAME", @"No name") forState:UIControlStateNormal];
    else
        [self.groupName setTitle:name forState:UIControlStateNormal];
}

- (void)putGroupIP:(NSString*)ip
{
    [self.groupIP setTitle:ip forState:UIControlStateNormal];
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

#pragma mark - Cell Actions

- (void)putDeleteButton:(UIGestureRecognizer*)gesture
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
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
    NSLog(@"[INFO][PrintJobCell] swiped left on item=%ld", (long)jobIndexPath.row);
#endif
    
    // check if this is the same job
    if ((self.jobWithDelete != nil) && (self.jobWithDelete.row == jobIndexPath.row))
        return;
    
    // check first if there are other groups with a delete button
    if ([self.delegate shouldPutDeleteButton:self.tag])
    {
        self.jobWithDelete = jobIndexPath;
        
        // check if this item already has a delete button
        PrintJobItemCell* jobCell = (PrintJobItemCell*)[self.printJobsView cellForRowAtIndexPath:jobIndexPath];
        if ([[jobCell.contentView subviews] count] == 5) //has an extra delete button
        {
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
            NSLog(@"[INFO][PrintJobCell] already has delete button, ignoring swipe");
#endif
            return;
        }
        
        // create the delete button
        // initial position offscreen
        CGRect startPos = CGRectMake(jobCell.frame.size.width,
                                     5.0f,
                                     self.deleteAllButton.frame.size.width-15.0f,
                                     jobCell.frame.size.height-10.0f);
        // final position on top of timestamp
        CGRect endPos = CGRectMake(self.deleteAllButton.frame.origin.x+5.0f,
                                   5.0f,
                                   self.deleteAllButton.frame.size.width-15.0f,
                                   jobCell.frame.size.height-10.0f);
        DeleteButton* deleteButton = [DeleteButton createAtOffscreenPosition:startPos
                                                        withOnscreenPosition:endPos];
        deleteButton.tag = (self.printJobsView.tag * TAG_FACTOR) + jobIndexPath.row; //<group>00<row>
        [deleteButton addTarget:self
                         action:@selector(tappedDeleteJob:)
               forControlEvents:UIControlEventTouchUpInside];
        
        // add to the view
        [jobCell setDeleteState:YES];
        [jobCell.contentView addSubview:deleteButton]; //will be added at the end of the subviews list
        
        // slide the button from offscreen to its place over the timestamp
        [deleteButton animateOnscreen:nil];
    }
}

- (void)removeDeleteButton
{
#if DEBUG_LOG_PRINT_JOB_GROUP_VIEW
    NSLog(@"[INFO][PrintJobCell] canceling delete button for item=%ld", (long)self.jobWithDelete.row);
#endif

    // get the delete button
    PrintJobItemCell* jobCell = (PrintJobItemCell*)[self.printJobsView cellForRowAtIndexPath:self.jobWithDelete];
    DeleteButton* deleteButton = (DeleteButton*)[[jobCell.contentView subviews] lastObject];
    
    // slide the button to offscreen
    [deleteButton animateOffscreen:^(BOOL finished)
    {
        [deleteButton removeFromSuperview];
        [jobCell setDeleteState:NO];
    }];
    
    self.jobWithDelete = nil;
}

- (void)colorHeader
{
    UIColor* highlightColor = [UIColor purple2ThemeColor];
    [self.groupName setBackgroundColor:highlightColor];
    [self.groupIP setBackgroundColor:highlightColor];
    [self.groupIndicator setBackgroundColor:highlightColor];
}

- (void)clearHeader
{
    UIColor* normalColor = [UIColor blackThemeColor];
    [self.groupName setBackgroundColor:normalColor];
    [self.groupIP setBackgroundColor:normalColor];
    [self.groupIndicator setBackgroundColor:normalColor];
}

- (void)tappedHeader
{
    [self colorHeader];
    [self.delegate didTapGroupHeader:self.tag];
    [self clearHeader];
}

- (void)colorDeleteAll
{
    if ([self.delegate shouldHighlightDeleteAllButton])
    {
        [self.deleteAllButton setBackgroundColor:[UIColor purple2ThemeColor]];
        [self.deleteAllButton setTitleColor:[UIColor whiteThemeColor] forState:UIControlStateNormal];
    }
}

- (void)clearDeleteAll
{
    [self.deleteAllButton setBackgroundColor:[UIColor whiteThemeColor]];
    [self.deleteAllButton setTitleColor:[UIColor blackThemeColor] forState:UIControlStateNormal];
}

- (void)tappedDeleteAll
{
    [self.delegate didTapDeleteAllButton:(UIButton*)self.deleteAllButton ofGroup:self.tag];
}

- (void)tappedDeleteJob:(UIButton*)button
{
    NSInteger buttonTag = [button tag];
    NSUInteger groupTag = buttonTag/TAG_FACTOR;
    NSUInteger jobTag = buttonTag%TAG_FACTOR;
    
    [self.delegate didTapDeleteJobButton:button ofJob:jobTag ofGroup:groupTag];
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
