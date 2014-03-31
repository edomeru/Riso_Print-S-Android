//
//  PrintJobHistoryViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryViewController.h"
#import "PrintJobHistoryGroupCell.h"
#import "PrintJobHistoryGroup.h"
#import "PListHelper.h"

//TODO: check if values can be retrieved programmatically
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

#pragma mark - Methods

/** Tapping the Main Menu button displays the Main Menu panel. */
- (IBAction)mainMenuAction:(UIButton*)sender;

- (void)setupData;
- (IBAction)tappedPrinterHeader:(UIButton*)sender;
- (IBAction)tappedDeleteAllButton:(UIButton*)sender;

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

#pragma mark - CollectionView

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
    PrintJobHistoryGroupCell* cell = [collectionView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                               forIndexPath:indexPath];
    
    // get the model
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    
    // put the model contents into the view
    [cell initWithTag:indexPath.row];
    [cell putGroupName:[NSString stringWithFormat:@"%@", group.groupName]];
    [cell putIndicator:group.isCollapsed];
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
            [cell putPrintJob:name
                   withResult:((i % 2) ? YES : NO)
                withTimestamp:[calendar dateFromComponents:date]];
        }
    }
    
    // since we are using reusable cells, handle scrolling by forcing redraw of the cell
    [cell reloadContents];
    
    return cell;
}

- (BOOL)collectionView:(UICollectionView*)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath*)indexPath
{
    return NO;  //disables highlighting of the cell on tap
    //TODO: check if there is a property or storyboard setting to just turn off highlighting
}

#pragma mark - CollectionViewFlowLayout

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    // get the group
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    NSLog(@"[INFO][PrintJob] setting size for %@", group.groupName);
    NSLog(@"[INFO][PrintJob] num print jobs = %lu", (unsigned long)group.countPrintJobs);
    
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
    
    // finalize the cell dimensions
    CGFloat cellHeight = GROUP_HEADER_HEIGHT + heightPrintJobsList;
    CGFloat cellWidth = GROUP_FRAME_WIDTH;
    NSLog(@"[INFO][PrintJob] h=%f,w=%f", cellHeight, cellWidth);
    CGSize cellSize = CGSizeMake(cellWidth, cellHeight);
    
    return cellSize;
}

#pragma mark - Data

- (void)setupData
{
    self.listPrintJobHistoryGroups = [NSMutableArray array];
    
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
    }
}

#pragma mark - Actions

- (IBAction)tappedPrinterHeader:(UIButton*)sender
{
    // get the cell tapped
    NSInteger cellIndex = [sender tag];
    NSLog(@"[INFO][PrintJob] tapped cell=%ld", (long)cellIndex);
    
    // toggle collapsed/expanded
    PrintJobHistoryGroup* group = [self.listPrintJobHistoryGroups objectAtIndex:cellIndex];
    [group collapse:!group.isCollapsed];
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.collectionView reloadData];
}

- (IBAction)tappedDeleteAllButton:(UIButton*)sender
{
    // get the cell tapped
    NSInteger cellIndex = [sender tag];
    NSLog(@"[INFO][PrintJob] tapped cell=%ld", (long)cellIndex);
    
    // remove the group
    [self.listPrintJobHistoryGroups removeObjectAtIndex:cellIndex];
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.collectionView reloadData];
}

#pragma mark - Segue

- (IBAction)unwindToPrintJobHistory:(UIStoryboardSegue*)sender
{
    [self.mainMenuButton setEnabled:YES];
}

@end
