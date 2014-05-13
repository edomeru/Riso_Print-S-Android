//
//  PrintJobHistoryViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryHelper.h"
#import "PrintJobHistoryGroup.h"
#import "PrintJob.h"
#import "PListHelper.h"
#import "AlertHelper.h"
#import "UIColor+Theme.h"

@interface PrintJobHistoryViewController ()

#pragma mark - UI Properties

/** Main Menu button on the Header. */
@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;

/** The UI for displaying the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet UICollectionView* groupsView;

/** The custom UICollectionViewLayout for arranging the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet PrintJobHistoryLayout* groupsViewLayout;

/** Reference to the Delete All button. */
@property (weak, nonatomic) UIButton* tappedDeleteAllButton;

#pragma mark - Data Properties

/** The data source for the list PrintJobHistoryGroup objects. */
@property (strong, nonatomic) NSMutableArray* listPrintJobHistoryGroups;

/** Keeps track of the index of the group that has the delete button. */
@property (strong, nonatomic) NSIndexPath* groupWithDelete;

#pragma mark - Methods

/** Tapping the Main Menu button displays the Main Menu panel. */
- (IBAction)mainMenuAction:(UIButton*)sender;

/** Tapping anywhere on the UICollectionView hides any DELETE button displayed. */
- (void)tappedCollection:(UIGestureRecognizer*)gestureRecognizer;

/** Removes a displayed DELETE button from a group. */
- (void)removeDeleteButton;

/** 
 Searches the list of PrintJobHistoryGroups for a group with the specified tag.
 Returns the group index and the actual group as out parameters.
 */
- (void)findGroupWithTag:(NSInteger)tag outIndex:(NSInteger*)index outGroup:(PrintJobHistoryGroup**)group;

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
    
    UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(tappedCollection:)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.numberOfTouchesRequired = 1;
    [self.groupsView addGestureRecognizer:tapGesture];
    
    self.groupWithDelete = nil;
    
    self.tappedDeleteAllButton = nil;
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
    groupCell.delegate = self;
    
    // get the model
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.item];
    
    // put the model contents into the view
    
    [groupCell initWithTag:group.tag]; // use a tag that is independent of the list or view position
                                       // (to support deleting groups later without need for reloading)
    
    [groupCell putGroupName:group.groupName];
    [groupCell putGroupIP:group.groupIP];
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
    return NO; //fix highlight
}

#pragma mark - PrintJobHistoryLayoutDelegate

- (NSUInteger)numberOfJobsForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.item];
    
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
    NSLog(@"[INFO][PrintJobCtrl] group=%ld printjobs=%lu", (long)group.tag, (unsigned long)group.countPrintJobs);
#endif
    
    if (group.isCollapsed)
        return 0; //no need to display any jobs
    else
        return group.countPrintJobs;
}

#pragma mark - PrintJobHistoryGroupCellDelegate

- (void)didTapGroupHeader:(NSUInteger)groupTag
{
    // check if there is a delete button present
    if (self.groupWithDelete != nil)
    {
        // cancel the delete button instead
        [self removeDeleteButton];
    }
    else
    {
        // get the group to be modified
        PrintJobHistoryGroup* group;
        NSInteger groupIndex;
        [self findGroupWithTag:groupTag outIndex:&groupIndex outGroup:&group];
        
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
        NSLog(@"[INFO][PrintJobCtrl] tapped header=%ld", (long)groupIndex);
#endif
        
        // toggle collapsed/expanded
        [group collapse:!group.isCollapsed];
        
        // redraw the view
        NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
        [self.groupsView reloadItemsAtIndexPaths:@[groupIndexPath]];
    }
}

- (BOOL)shouldHighlightDeleteAllButton
{
    if (self.groupWithDelete != nil)
        return NO;
    else
        return YES;
}

- (void)didTapDeleteAllButton:(UIButton*)button ofGroup:(NSUInteger)groupTag
{
    // check if there is a delete button present
    if (self.groupWithDelete != nil)
    {
        // cancel the delete button instead
        [self removeDeleteButton];
    }
    else
    {
        // get the group to be modified
        PrintJobHistoryGroup* group;
        NSInteger groupIndex;
        [self findGroupWithTag:groupTag outIndex:&groupIndex outGroup:&group];
        
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
        NSLog(@"[INFO][PrintJobCtrl] tapped delete all button=%ld", (long)groupIndex);
#endif
        
        [button setBackgroundColor:[UIColor purple2ThemeColor]];
        [button setTitleColor:[UIColor whiteThemeColor] forState:UIControlStateNormal];
        self.tappedDeleteAllButton = button;
        
        [AlertHelper displayConfirmation:kAlertConfirmationDeleteAllJobs
                               forScreen:self
                             withDetails:@[[NSNumber numberWithInteger:groupIndex], group.groupName]];
    }
}

- (BOOL)shouldPutDeleteButton:(NSUInteger)groupTag
{
    // get the group to be modified
    PrintJobHistoryGroup* group;
    NSInteger groupIndex;
    [self findGroupWithTag:groupTag outIndex:&groupIndex outGroup:&group];
    NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
    
    if ((self.groupWithDelete != nil) && (self.groupWithDelete != groupIndexPath))
    {
        [self removeDeleteButton];
        return NO;
    }
    else
    {
        self.groupWithDelete = groupIndexPath;
        return YES;
    }
}

- (void)didTapDeleteJobButton:button ofJob:(NSUInteger)jobTag ofGroup:(NSUInteger)groupTag
{
    // get the group to be modified
    PrintJobHistoryGroup* group;
    NSInteger groupIndex;
    [self findGroupWithTag:groupTag outIndex:&groupIndex outGroup:&group];
    
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
    NSLog(@"[INFO][PrintJobCtrl] tapped delete button=%ld", (long)groupIndex);
#endif
    
    // remove the print job
    BOOL bRemovedJob = [group removePrintJobAtIndex:jobTag];
    if (bRemovedJob)
    {
        NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
        
        if ([group countPrintJobs] == 0)
        {
            // no more jobs for this group
            
            // remove this group from the data source
            [self.listPrintJobHistoryGroups removeObjectAtIndex:groupIndex];
            
            // remove the cell from the view
            [self.groupsView deleteItemsAtIndexPaths:@[groupIndexPath]];
        }
        else
        {
            // group still has jobs
            
            // just reload the view
            [self.groupsView reloadItemsAtIndexPaths:@[groupIndexPath]];
        }
    }
    else
    {
        [AlertHelper displayResult:kAlertResultErrDefault
                         withTitle:kAlertTitlePrintJobHistory
                       withDetails:nil];
    }
}

#pragma mark - Actions

- (void)tappedCollection:(UIGestureRecognizer*)gestureRecognizer
{
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
    // get the group tapped
    CGPoint tappedArea = [gestureRecognizer locationInView:self.groupsView];
    NSIndexPath* groupIndexPath = [self.groupsView indexPathForItemAtPoint:tappedArea];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupIndexPath.item);
#endif
    
    // check if a delete button is present in any group
    if (self.groupWithDelete != nil)
        [self removeDeleteButton];
}

#pragma mark - Utilities

- (void)removeDeleteButton
{
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
    NSLog(@"[INFO][PrintJobCtrl] canceling delete button for group=%ld", (long)self.groupWithDelete.row);
#endif
    
    PrintJobHistoryGroupCell* groupCell = (PrintJobHistoryGroupCell*)[self.groupsView
                                                                      cellForItemAtIndexPath:self.groupWithDelete];
    [groupCell removeDeleteButton];
    self.groupWithDelete = nil;
}

- (void)findGroupWithTag:(NSInteger)tag outIndex:(NSInteger*)index outGroup:(PrintJobHistoryGroup**)group
{
    PrintJobHistoryGroup* grp;
    NSInteger idx;
    
    for (idx = 0; idx < [self.listPrintJobHistoryGroups count]; idx++)
    {
        grp = [self.listPrintJobHistoryGroups objectAtIndex:idx];
        if (grp.tag == tag)
            break;
    }
    
    *index = idx;
    *group = grp;
}

#pragma mark - Delete Confirmation

- (void)alertView:(UIAlertView*)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == [alertView firstOtherButtonIndex])
    {
        // get the group
        NSInteger groupIndex = alertView.tag;
        PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:groupIndex];
        
        // remove each job from the group
        BOOL bRemovedAllJobs = NO;
        while (group.countPrintJobs != 0)
        {
            bRemovedAllJobs = [group removePrintJobAtIndex:0];
            if (!bRemovedAllJobs)
                break; // avoids corrupting the list and/or DB
        }
        if (bRemovedAllJobs)
        {
            // remove this group from the data source
            [self.listPrintJobHistoryGroups removeObjectAtIndex:groupIndex];
            
            // remove the cell from the view
            NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
            [self.groupsView deleteItemsAtIndexPaths:@[groupIndexPath]];
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDefault
                             withTitle:kAlertTitlePrintJobHistory
                           withDetails:nil];
        }
    }
    
    [self.tappedDeleteAllButton setBackgroundColor:[UIColor whiteThemeColor]];
    [self.tappedDeleteAllButton setTitleColor:[UIColor blackThemeColor] forState:UIControlStateNormal];
    self.tappedDeleteAllButton = nil;
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
