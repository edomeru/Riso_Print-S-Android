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
#import "DeleteButton.h"

@interface PrintJobHistoryViewController ()

#pragma mark - UI Properties

/** Main Menu button on the Header. */
@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;

/** The UI for displaying the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet UICollectionView* groupsView;

/** The custom UICollectionViewLayout for arranging the PrintJobHistoryGroups. */
@property (weak, nonatomic) IBOutlet PrintJobHistoryLayout* groupsViewLayout;

/** Reference to the Delete All button. */
@property (weak, nonatomic) DeleteButton* tappedDeleteButton;

#pragma mark - Data Properties

/** The data source for the list PrintJobHistoryGroup objects. */
@property (strong, nonatomic) NSMutableArray* listPrintJobHistoryGroups;

/** Keeps track of the index of the group that has the delete button. */
@property (strong, nonatomic) NSIndexPath* groupWithDelete;

/** Keeps track of the index of the group marked for deletion. */
@property (assign, nonatomic) NSInteger groupToDeleteIndex;

/** Keeps track of the index of the job marked for deletion. */
@property (assign, nonatomic) NSInteger jobToDeleteIndex;

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
    [self.groupsViewLayout invalidateColumnAssignments];
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
    
    self.groupsView.bounces = NO; //switch in storyboard does not disable the bounce
    
    UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(tappedCollection:)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.numberOfTouchesRequired = 1;
    tapGesture.delaysTouchesBegan = NO;
    tapGesture.delaysTouchesEnded = NO;
    [self.groupsView addGestureRecognizer:tapGesture];
    
    UIPanGestureRecognizer* panGesture = [[UIPanGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(swipedNotLeftCollection:)];
    panGesture.minimumNumberOfTouches = 1;
    panGesture.delegate = self;
    [panGesture requireGestureRecognizerToFail:tapGesture];
    [self.groupsView addGestureRecognizer:panGesture];
    
    self.groupWithDelete = nil;
    self.groupToDeleteIndex = -1;
    self.jobToDeleteIndex = -1;
    
    self.tappedDeleteButton = nil;
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
    [groupCell clearHeader];
    
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
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobCtrl] item=%ld group=[%@] printjobs=%lu",
          (long)indexPath.item, group.groupName, (unsigned long)group.countPrintJobs);
#endif
    
    if (group.isCollapsed)
        return 0; //no need to display any jobs
    else
        return group.countPrintJobs;
}

#pragma mark - PrintJobHistoryGroupCellDelegate

- (BOOL)shouldHighlightGroupHeader
{
    if (self.groupWithDelete != nil)
        return NO;
    else
        return YES;
}

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
        NSLog(@"[INFO][PrintJobCtrl] tapped header [%@],[%ld]", group.groupIP, groupIndex);
#endif
        
        // toggle collapsed/expanded
        [group collapse:!group.isCollapsed];
        
        // redraw the view
        NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
        [self.groupsView reloadItemsAtIndexPaths:@[groupIndexPath]];
    }
}

- (BOOL)shouldHighlightDeleteGroupButton
{
    if (self.groupWithDelete != nil)
        return NO;
    else
        return YES;
}

- (void)didTapDeleteGroupButton:(DeleteButton*)button ofGroup:(NSUInteger)groupTag
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
        self.groupToDeleteIndex = groupIndex;
        
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
        NSLog(@"[INFO][PrintJobCtrl] tapped delete group [%@],[%ld]", group.groupIP, groupIndex);
#endif
        
        [button keepHighlighted:YES];
        [button setHighlighted:YES];
        self.tappedDeleteButton = button;
        
        __weak PrintJobHistoryViewController* weakSelf = self;
        
        void (^cancelled)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
        {
            [alertView dismiss];
            
            [weakSelf.tappedDeleteButton keepHighlighted:NO];
            [weakSelf.tappedDeleteButton setHighlighted:NO];
            weakSelf.tappedDeleteButton = nil;
            
            weakSelf.groupToDeleteIndex = -1;
        };

        void (^confirmed)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
        {
            [alertView dismiss];
            
            [weakSelf.tappedDeleteButton keepHighlighted:NO];
            [weakSelf.tappedDeleteButton setHighlighted:NO];
            weakSelf.tappedDeleteButton = nil;
            
            // get the group
            NSInteger groupIndex = weakSelf.groupToDeleteIndex;
            PrintJobHistoryGroup* group = [weakSelf.listPrintJobHistoryGroups objectAtIndex:groupIndex];
            
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
                [weakSelf.listPrintJobHistoryGroups removeObjectAtIndex:groupIndex];
                
                // remove the cell from the view
                NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
                [weakSelf.groupsViewLayout prepareForDelete:groupIndexPath];
                [weakSelf.groupsView deleteItemsAtIndexPaths:@[groupIndexPath]];
            }
            else
            {
                [AlertHelper displayResult:kAlertResultErrDelete
                                 withTitle:kAlertTitlePrintJobHistory
                               withDetails:nil];
            }
            

            weakSelf.groupToDeleteIndex = -1;
        };
        
        [AlertHelper displayConfirmation:kAlertConfirmationDeleteAllJobs
                       withCancelHandler:cancelled
                      withConfirmHandler:confirmed];
    }
}

- (BOOL)shouldPutDeleteJobButton:(NSUInteger)groupTag
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

- (void)didTapDeleteJobButton:(DeleteButton*)button ofJob:(NSUInteger)jobTag ofGroup:(NSUInteger)groupTag
{
    // get the group to be modified
    PrintJobHistoryGroup* group;
    NSInteger groupIndex;
    [self findGroupWithTag:groupTag outIndex:&groupIndex outGroup:&group];
    
#if DEBUG_LOG_PRINT_JOB_HISTORY_SCREEN
        NSLog(@"[INFO][PrintJobCtrl] tapped delete job [%@],[%ld]", group.groupIP, groupIndex);
#endif
    
    self.groupToDeleteIndex = groupIndex;
    self.jobToDeleteIndex = jobTag;
    
    [button keepHighlighted:YES];
    [button setHighlighted:YES];
    self.tappedDeleteButton = button;

    __weak PrintJobHistoryViewController* weakSelf = self;

    void (^cancelled)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];

        [weakSelf.tappedDeleteButton keepHighlighted:NO];
        [weakSelf.tappedDeleteButton setHighlighted:NO];
        weakSelf.tappedDeleteButton = nil;
        
        [weakSelf removeDeleteButton];
        
        weakSelf.groupToDeleteIndex = -1;
        weakSelf.jobToDeleteIndex = -1;
    };
    void (^confirmed)(CXAlertView*, CXAlertButtonItem*) = ^void(CXAlertView* alertView, CXAlertButtonItem* button)
    {
        [alertView dismiss];
        
        [weakSelf.tappedDeleteButton keepHighlighted:NO];
        [weakSelf.tappedDeleteButton setHighlighted:NO];
        weakSelf.tappedDeleteButton = nil;
        
        // get the group
        NSInteger groupIndex = weakSelf.groupToDeleteIndex;
        PrintJobHistoryGroup* group = [weakSelf.listPrintJobHistoryGroups objectAtIndex:groupIndex];
        
        // remove the print job
        BOOL bRemovedJob = [group removePrintJobAtIndex:weakSelf.jobToDeleteIndex];
        if (bRemovedJob)
        {
            NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:groupIndex inSection:0];
            
            if ([group countPrintJobs] == 0)
            {
                // no more jobs for this group
                // remove the group from data source and the view
                [weakSelf.listPrintJobHistoryGroups removeObjectAtIndex:groupIndex];
                [weakSelf.groupsView deleteItemsAtIndexPaths:@[groupIndexPath]];
            }
            else
            {
                // group still has jobs
                // reload the view
                [weakSelf.groupsView reloadItemsAtIndexPaths:@[groupIndexPath]];
            }
            
            weakSelf.groupWithDelete = nil;
        }
        else
        {
            [AlertHelper displayResult:kAlertResultErrDelete
                             withTitle:kAlertTitlePrintJobHistory
                           withDetails:nil];
            
            [weakSelf removeDeleteButton];
        }
        
        weakSelf.groupToDeleteIndex = -1;
        weakSelf.jobToDeleteIndex = -1;
    };

    [AlertHelper displayConfirmation:kAlertConfirmationDeleteJob
                   withCancelHandler:cancelled
                  withConfirmHandler:confirmed];
}

- (BOOL)shouldHighlightJob
{
    if (self.groupWithDelete != nil)
        return NO;
    else
        return YES;
}

#pragma mark - Actions

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer
{
    if ([gestureRecognizer isKindOfClass:[UIPanGestureRecognizer class]])
    {
        if (self.groupWithDelete == nil)
        {
            // block the panning gesture when there is no delete button
            // let the view scroll instead
            return NO;
        }
        else
        {
            // cancel the delete button for the first swipe motion
            return YES;
        }
    }
    
    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    if ([gestureRecognizer isKindOfClass:[UIPanGestureRecognizer class]])
    {
        // allow both the UIScrollView's PanGesture and our PanGesture to react at the same time
        // (allows simultaneous hiding of the delete button and scrolling)
        return YES;
    }
    else
    {
        return NO;
    }
}

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

- (void)swipedNotLeftCollection:(UIGestureRecognizer*)gestureRecognizer
{
    // method will only be called when gestureRecognizerShouldBegin returns YES,
    // which will only happen when there is a delete button to be cancelled
    
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

#pragma mark - Segue

- (IBAction)unwindToPrintJobHistory:(UIStoryboardSegue*)sender
{
    [self.mainMenuButton setEnabled:YES];
}

#pragma mark - Scrolling

- (void)scrollViewDidScroll:(UIScrollView*)scrollView
{
    if (self.groupWithDelete != nil)
        [self removeDeleteButton];
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
