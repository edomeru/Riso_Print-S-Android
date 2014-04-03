//
//  PrintJobHistoryViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryHelper.h"
#import "PrintJobHistoryGroup.h"
#import "PrintJob.h"
#import "PListHelper.h"
#import "AlertHelper.h"

const float GROUP_HEADER_HEIGHT     = 45.0f;  //should match the value in storyboard
const float GROUP_FRAME_WIDTH       = 320.f;  //should match the value in storyboard
const float GROUP_MARGIN_BOTTOM     = 0.0f;   //TODO: how to properly set margin (iOS7 != iOS6)
const float PRINT_JOB_ITEM_HEIGHT   = 45.0f;  //should match the value in storyboard

@interface PrintJobHistoryViewController ()

#pragma mark - UI Properties

/** Main Menu button on the Header. */
@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;

/** The UI for displaying the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet UICollectionView* groupsView;

/** The custom UICollectionViewLayout for arranging the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet PrintJobHistoryLayout* groupsViewLayout;

#pragma mark - Data Properties

/**
 The data source for the list of print job history items.
 All items are arranged by group, wherein each group corresponds to one
 printer (as specified by the printer name) and each item in the group
 represents one print job (as specified by the print job name).
 Other details of the print job include the status and the
 date of creation (which is used to sort each item in the group).
 */
@property (strong, nonatomic) NSMutableArray* listPrintJobHistoryGroups;

/** Keeps track of the index of the group that has the delete button. */
@property (strong, nonatomic) NSIndexPath* groupWithDelete;

#pragma mark - Methods

/** Tapping the Main Menu button displays the Main Menu panel. */
- (IBAction)mainMenuAction:(UIButton*)sender;

/** Tapping the printer name collapses/expands the group. */
- (IBAction)tappedPrinterHeader:(UIButton*)sender;

/** Tapping the DELETE ALL button removes the entire group from the display and from the database. */
- (IBAction)tappedDeleteAllButton:(UIButton*)sender;

/** Tapping the DELETE button on a print job removes the print job from the display and from the database. */
- (void)tappedDeleteOneButton:(UIButton*)button;

/** Swiping left on a print job displays the DELETE button. */
- (IBAction)swipedLeft:(UIGestureRecognizer*)gestureRecognizer;

/** Swiping right on the UICollectionView hides any DELETE button displayed. */
- (IBAction)swipedRight:(UIGestureRecognizer*)gestureRecognizer;

/** Tapping anywhere on the UICollectionView hides any DELETE button displayed. */
- (IBAction)tappedGroup:(UIGestureRecognizer*)gestureRecognizer;

/** Removes a displayed DELETE button from a group. */
- (void)removeDeleteButton;

@end

@implementation PrintJobHistoryViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.listPrintJobHistoryGroups = [PrintJobHistoryHelper preparePrintJobHistoryGroups];
    
    self.groupsViewLayout.delegate = self;
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        [self.groupsViewLayout setupForOrientation:self.interfaceOrientation
                                         forDevice:UIUserInterfaceIdiomPad];
    }
    else
    {
        [self.groupsViewLayout setupForOrientation:self.interfaceOrientation
                                         forDevice:UIUserInterfaceIdiomPhone];
    }
    
    self.groupWithDelete = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Header

- (IBAction)mainMenuAction:(UIButton*)sender
{
    [self.mainMenuButton setEnabled:NO];
    [self performSegueTo:[HomeViewController class]];
}

#pragma mark - UICollectionViewDataSource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView*)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [self.listPrintJobHistoryGroups count];
}

- (UICollectionViewCell*)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    // get the view
    PrintJobHistoryGroupCell* groupCell = [collectionView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                                    forIndexPath:indexPath];
    
    // get the model
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    
    // put the model contents into the view
    // avoid passing the actual PrintJob object into the view
    //  -- if the view changes, only controller->view needs to update
    //  -- if the model changes, only the controller<-model needs to update
    [groupCell initWithTag:indexPath.row];
    [groupCell putGroupName:[NSString stringWithFormat:@"%@", group.groupName]];
    [groupCell putIndicator:group.isCollapsed];
    if (!group.isCollapsed)
    {
        // put the print jobs one-by-one
        for (int i = 0; i < group.countPrintJobs; i++)
        {
            PrintJob* job = [group getPrintJobAtIndex:i];
            if (job == nil)
                continue;
            [groupCell putPrintJob:job.name
                        withResult:([job.result boolValue] ? YES : NO)
                     withTimestamp:job.date];
        }
    }
    
    // since we are using reusable cells, handle scrolling by forcing redraw of the cell
    [groupCell reloadContents];
    
    // check if a delete button was present while scrolling
    // (fixes bug when swiping-left on the top/bottom edges of the list then scrolling)
    if (self.groupWithDelete != nil)
        [self removeDeleteButton];
    self.groupWithDelete = nil;
    
    return groupCell;
}

- (BOOL)collectionView:(UICollectionView*)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath*)indexPath
{
    return NO;  //disables highlighting of the cell on tap
    //TODO: check if there is a property or storyboard setting to just turn off highlighting
}

#pragma mark - PrintJobHistoryLayoutDelegate

- (CGSize)sizeForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    // get the group
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    NSLog(@"[INFO][PrintJobCtrl] setting size for %@", group.groupName);
    NSLog(@"[INFO][PrintJobCtrl] num print jobs = %lu", (unsigned long)group.countPrintJobs);
    
    // set the list height
    CGFloat heightPrintJobsList;
    if (group.isCollapsed)
    {
        // collapsed
        // the height occupied by the print jobs will be zero
        heightPrintJobsList = 0;
    }
    else
    {
        // expanded
        // the height occupied by the print jobs will be (number of print jobs)*(height for each print job)
        heightPrintJobsList = group.countPrintJobs * PRINT_JOB_ITEM_HEIGHT;
        heightPrintJobsList += GROUP_MARGIN_BOTTOM;
    }
    
    // finalize the group dimensions
    CGFloat groupCellHeight = GROUP_HEADER_HEIGHT + heightPrintJobsList;
    CGFloat groupCellWidth = GROUP_FRAME_WIDTH;
    NSLog(@"[INFO][PrintJobCtrl] h=%f,w=%f", groupCellHeight, groupCellWidth);
    CGSize groupCellSize = CGSizeMake(groupCellWidth, groupCellHeight);
    
    return groupCellSize;
}

#pragma mark - Actions

- (IBAction)tappedPrinterHeader:(UIButton*)sender
{
    // get the group tapped
    NSInteger groupIndex = [sender tag];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupIndex);
    
    // toggle collapsed/expanded
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:groupIndex];
    [group collapse:!group.isCollapsed];
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.groupsView reloadData];
}

- (IBAction)tappedDeleteAllButton:(UIButton*)sender
{
    // get the group tapped
    NSInteger groupTag = [sender tag];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupTag);
    
    // remove the group (and all its print jobs)
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:groupTag];
    BOOL bRemovedAllJobs = NO;
    while (group.countPrintJobs != 0)
    {
        bRemovedAllJobs = [group removePrintJobAtIndex:0];
        if (!bRemovedAllJobs)
            break; // avoids corrupting the list and/or DB
    }
    if (bRemovedAllJobs)
    {
        [self.listPrintJobHistoryGroups removeObjectAtIndex:groupTag];
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDefault
                         withTitle:kAlertTitlePrintJobHistory
                       withDetails:nil];
    }
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.groupsView reloadData];
}

- (void)tappedDeleteOneButton:(UIButton*)button
{
    NSInteger tag = [button tag];
    NSUInteger groupTag = tag/TAG_FACTOR;
    NSUInteger jobTag = tag%TAG_FACTOR;
    NSLog(@"[INFO][PrintJobCtrl] will delete {group=%ld, job=%ld}",
          (unsigned long)groupTag, (unsigned long)jobTag);
    
    // remove the print job
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:groupTag];
    BOOL bRemovedJob = [group removePrintJobAtIndex:jobTag];
    if (bRemovedJob)
    {
        if ([group countPrintJobs] == 0)
            [self.listPrintJobHistoryGroups removeObjectAtIndex:groupTag];
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDefault
                         withTitle:kAlertTitlePrintJobHistory
                       withDetails:nil];
    }
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.groupsView reloadData];
}

- (void)swipedLeft:(UIGestureRecognizer*)gestureRecognizer
{
    // get the group swiped
    CGPoint swipedArea = [gestureRecognizer locationInView:self.groupsView];
    NSIndexPath* groupIndexPath = [self.groupsView indexPathForItemAtPoint:swipedArea];
    NSLog(@"[INFO][PrintJobCtrl] swiped left on group=%ld", (long)groupIndexPath.row);
    
    // check if another group has a delete button
    if ((self.groupWithDelete != nil) && (self.groupWithDelete != groupIndexPath))
        [self removeDeleteButton];

    self.groupWithDelete = groupIndexPath;
    
    // add a delete button to the swiped group
    PrintJobHistoryGroupCell* groupCell = (PrintJobHistoryGroupCell*)[self.groupsView
                                                                      cellForItemAtIndexPath:groupIndexPath];
    [groupCell putDeleteButton:gestureRecognizer
                     handledBy:self
              usingActionOnTap:@selector(tappedDeleteOneButton:)];
}

- (void)swipedRight:(UIGestureRecognizer*)gestureRecognizer
{
    // get the group swiped
    CGPoint swipedArea = [gestureRecognizer locationInView:self.groupsView];
    NSIndexPath* groupIndexPath = [self.groupsView indexPathForItemAtPoint:swipedArea];
    NSLog(@"[INFO][PrintJobCtrl] swiped right on group=%ld", (long)groupIndexPath.row);
    
    // check if a delete button is present in any group
    if (self.groupWithDelete != nil)
        [self removeDeleteButton];
}

- (void)tappedGroup:(UIGestureRecognizer*)gestureRecognizer
{
    // get the group tapped
    CGPoint tappedArea = [gestureRecognizer locationInView:self.groupsView];
    NSIndexPath* groupIndexPath = [self.groupsView indexPathForItemAtPoint:tappedArea];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupIndexPath.row);
    
    // check if a delete button is present in any group
    if (self.groupWithDelete != nil)
        [self removeDeleteButton];
}

- (void)removeDeleteButton
{
    NSLog(@"[INFO][PrintJobCtrl] canceling delete button for group=%ld", (long)self.groupWithDelete.row);
    PrintJobHistoryGroupCell* groupCell = (PrintJobHistoryGroupCell*)[self.groupsView
                                                                      cellForItemAtIndexPath:self.groupWithDelete];
    [groupCell removeDeleteButton];
    self.groupWithDelete = nil;
}

#pragma mark - Segue

- (IBAction)unwindToPrintJobHistory:(UIStoryboardSegue*)sender
{
    [self.mainMenuButton setEnabled:YES];
}

#pragma mark - Rotation

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        [self.groupsViewLayout setupForOrientation:toInterfaceOrientation
                                         forDevice:UIUserInterfaceIdiomPad];
    }
    else
    {
        [self.groupsViewLayout setupForOrientation:toInterfaceOrientation
                                         forDevice:UIUserInterfaceIdiomPhone];
    }
}

@end
