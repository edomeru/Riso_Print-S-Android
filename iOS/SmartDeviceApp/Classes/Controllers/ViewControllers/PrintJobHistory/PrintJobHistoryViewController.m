//
//  PrintJobHistoryViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryGroup.h"
#import "PListHelper.h"

const float GROUP_HEADER_HEIGHT     = 45.0f;  //should match the value in storyboard
const float GROUP_FRAME_WIDTH       = 320.f;  //should match the value in storyboard
const float GROUP_MARGIN_BOTTOM     = 0.0f;   //TODO: how to properly set margin (iOS7 != iOS6)
const float PRINT_JOB_ITEM_HEIGHT   = 45.0f;  //should match the value in storyboard

@interface PrintJobHistoryViewController ()

#pragma mark - UI Properties

/** Main Menu button on the Header. */
@property (weak, nonatomic) IBOutlet UIButton* mainMenuButton;

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
@property (strong, nonatomic) NSIndexPath* deleteGroup;

#pragma mark - Methods

/** Tapping the Main Menu button displays the Main Menu panel. */
- (IBAction)mainMenuAction:(UIButton*)sender;

- (void)setupData;
- (IBAction)tappedPrinterHeader:(UIButton*)sender;
- (IBAction)tappedDeleteAllButton:(UIButton*)sender;
- (void)tappedDeleteOneButton:(UIButton*)button;
- (void)swipedLeft:(UIGestureRecognizer*)gestureRecognizer;
- (void)swipedRight:(UIGestureRecognizer*)gestureRecognizer;
- (void)tappedGroup:(UIGestureRecognizer*)gestureRecognizer;
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
    
    [self setupData];
    [self setupView];
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

#pragma mark - UICollectionView

- (void)setupView
{
    self.deleteGroup = nil;
    
    // put the swipe-left-to-delete gesture handler
    UISwipeGestureRecognizer* swipeLeft = [[UISwipeGestureRecognizer alloc] initWithTarget:self
                                                                                    action:@selector(swipedLeft:)];
    swipeLeft.direction = UISwipeGestureRecognizerDirectionLeft;
    [self.groupsView addGestureRecognizer:swipeLeft];
    
    // put the swipe-right-to-cancel-delete gesture handler
    UISwipeGestureRecognizer* swipeRight = [[UISwipeGestureRecognizer alloc] initWithTarget:self
                                                                                     action:@selector(swipedRight:)];
    swipeRight.direction = UISwipeGestureRecognizerDirectionRight;
    [self.groupsView addGestureRecognizer:swipeRight];
    
    // put the tap-anywhere-to-cancel-delete gesture handler
    UITapGestureRecognizer* tapCollection = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                    action:@selector(tappedGroup:)];
    tapCollection.numberOfTapsRequired = 1;
    tapCollection.numberOfTouchesRequired = 1;
    [self.groupsView addGestureRecognizer:tapCollection];
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
    [groupCell initWithTag:indexPath.row];
    [groupCell putGroupName:[NSString stringWithFormat:@"%@", group.groupName]];
    [groupCell putIndicator:group.isCollapsed];
    if (!group.isCollapsed)
    {
        // put the print jobs
        for (int i = 0; i < group.countPrintJobs; i++)
        {
            //TODO: the method getPrintJobAtIndex: should return a PrintJob object, not just a NSString
            //TODO: timestamp should be retrieved from the PrintJob object
            //TODO: result should be retrieved from the PrintJob object
            NSString* name = [group getPrintJobAtIndex:i];
            NSDateComponents* date = [[NSDateComponents alloc] init];
            NSCalendar* calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
            [date setDay:31];
            [date setMonth:3];
            [date setYear:2014];
            [date setHour:8];
            [date setMinute:indexPath.row+i];
            [groupCell putPrintJob:name
                        withResult:((i % 2) ? YES : NO)
                     withTimestamp:[calendar dateFromComponents:date]];
        }
    }
    
    // since we are using reusable cells, handle scrolling by forcing redraw of the cell
    [groupCell reloadContents];
    
    // check if a delete button was present while scrolling
    // (fixes bug when swiping-left on the top/bottom edges of the list then scrolling)
    if (self.deleteGroup != nil)
        [self removeDeleteButton];
    self.deleteGroup = nil;
    
    return groupCell;
}

- (BOOL)collectionView:(UICollectionView*)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath*)indexPath
{
    return NO;  //disables highlighting of the cell on tap
    //TODO: check if there is a property or storyboard setting to just turn off highlighting
}

#pragma mark - UICollectionViewLayout

- (CGSize)computeSizeForGroupAtIndexPath:(NSIndexPath*)indexPath
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

#pragma mark - Data

- (void)setupData
{
    self.listPrintJobHistoryGroups = [NSMutableArray arrayWithCapacity:7]; //TODO: capacity=DBcontents
    
    //TODO: addPrintJob should add a PrintJob object, not just a NSString
    //TODO: the group name should be retrieved from the PrintJob.Printer.name property
    //TODO: retain the test data for debugging, add result and timestamp
    
    BOOL usePrintJobTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
    if (!usePrintJobTestData)
    {
        //TODO: get the print job history items from DB
    }
    else
    {
        PrintJobHistoryGroup* group1 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 1"];
        [group1 addPrintJob:@"Print Job A"];
        [group1 addPrintJob:@"Print Job B"];
        [group1 addPrintJob:@"Print Job C"];
        [group1 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group1];
        
        PrintJobHistoryGroup* group2 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 2"];
        [group2 addPrintJob:@"Print Job D"];
        [group2 addPrintJob:@"Print Job E"];
        [group2 addPrintJob:@"Print Job F"];
        [group2 addPrintJob:@"Print Job G"];
        [group2 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group2];
        
        PrintJobHistoryGroup* group3 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 3"];
        [group3 addPrintJob:@"Print Job H"];
        [group3 addPrintJob:@"Print Job I"];
        [group3 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group3];
        
        PrintJobHistoryGroup* group4 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 4"];
        [group4 addPrintJob:@"Print Job J"];
        [group4 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group4];
        
        PrintJobHistoryGroup* group5 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 5"];
        [group5 addPrintJob:@"Print Job K"];
        [group5 addPrintJob:@"Print Job L"];
        [group5 addPrintJob:@"Print Job M"];
        [group5 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group5];
        
        PrintJobHistoryGroup* group6 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 6"];
        [group6 addPrintJob:@"Print Job N"];
        [group6 addPrintJob:@"Print Job O"];
        [group6 addPrintJob:@"Print Job P"];
        [group6 addPrintJob:@"Print Job Q"];
        [group6 addPrintJob:@"Print Job R"];
        [group6 addPrintJob:@"Print Job S"];
        [group6 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group6];
        
        PrintJobHistoryGroup* group7 = [PrintJobHistoryGroup initWithGroupName:@"RISO Printer 7"];
        [group7 addPrintJob:@"Print Job T"];
        [group7 addPrintJob:@"Print Job U"];
        [group7 collapse:NO];
        [self.listPrintJobHistoryGroups addObject:group7];
    }
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
    NSInteger groupIndex = [sender tag];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupIndex);
    
    // remove the group
    [self.listPrintJobHistoryGroups removeObjectAtIndex:groupIndex];
    
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
    NSUInteger itemTag = tag%TAG_FACTOR;
    NSLog(@"[INFO][PrintJobCtrl] will delete {group=%ld, item=%ld}",
          (unsigned long)groupTag, (unsigned long)itemTag);
    
    // delete the item from the model
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:groupTag];
    [group deletePrintJobAtIndex:itemTag];
    if ([group countPrintJobs] == 0)
        [self.listPrintJobHistoryGroups removeObjectAtIndex:groupTag];
    
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
    if ((self.deleteGroup != nil) && (self.deleteGroup != groupIndexPath))
        [self removeDeleteButton];

    self.deleteGroup = groupIndexPath;
    
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
    if (self.deleteGroup != nil)
        [self removeDeleteButton];
}

- (void)tappedGroup:(UIGestureRecognizer*)gestureRecognizer
{
    // get the group tapped
    CGPoint tappedArea = [gestureRecognizer locationInView:self.groupsView];
    NSIndexPath* groupIndexPath = [self.groupsView indexPathForItemAtPoint:tappedArea];
    NSLog(@"[INFO][PrintJobCtrl] tapped group=%ld", (long)groupIndexPath.row);
    
    // check if a delete button is present in any group
    if (self.deleteGroup != nil)
        [self removeDeleteButton];
}

- (void)removeDeleteButton
{
    NSLog(@"[INFO][PrintJobCtrl] canceling delete button for group=%ld", (long)self.deleteGroup.row);
    PrintJobHistoryGroupCell* groupCell = (PrintJobHistoryGroupCell*)[self.groupsView
                                                                      cellForItemAtIndexPath:self.deleteGroup];
    [groupCell removeDeleteButton];
    self.deleteGroup = nil;
}

#pragma mark - Segue

- (IBAction)unwindToPrintJobHistory:(UIStoryboardSegue*)sender
{
    [self.mainMenuButton setEnabled:YES];
}

@end
