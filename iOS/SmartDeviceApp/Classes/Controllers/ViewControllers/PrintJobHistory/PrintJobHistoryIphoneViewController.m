//
//  PrintJobHistoryIphoneViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryIphoneViewController.h"
#import "PrintJobHistoryGroup.h"

//TODO: check if values can be retrieved programmatically
const float GROUP_HEADER_HEIGHT     = 45.0f;  //should match the value in storyboard
const float PRINT_JOB_ITEM_HEIGHT   = 45.0f;  //should match the value in storyboard
const float PRINT_JOB_ITEM_WIDTH    = 320.f;  //should match the value in storyboard
const float GROUP_MARGIN_BOTTOM     = -5.0f;  //TODO: how to properly set margin (iOS7 != iOS6)

@interface PrintJobHistoryIphoneViewController ()

#pragma mark - UI Properties

/** 
 The UI responsible for displaying the list of print job history items.
 */
@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;

#pragma mark - Data Properties

/**
 The data source for the list of print job history items.
 Each group corresponds to one printer (as specified by the printer name),
 and each item in the group represents one print job (as specified by the
 print job name). Other details of the print job include the status and the
 date of creation (which is used to sort each item in the group).
 This data should be provided by the PrintJobHistoryManager.
 */
@property (strong, nonatomic) NSMutableArray* listPrintJobHistoryGroups;

/**
 Flag array use to determine whether a group is collapsed or expanded.
 A YES value here means that the group is collapsed.
 Initially, all groups are expanded, so all the values here will be NO.
 */
@property (strong, nonatomic) NSMutableArray* listCollapsedFlags;

#pragma mark - Methods

- (void)initData;
- (IBAction)tappedPrinterHeader:(UIButton*)sender;
- (IBAction)tappedDeleteAllButton:(UIButton*)sender;

@end

@implementation PrintJobHistoryIphoneViewController

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
    
    [self initData];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
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
    PrintJobHistoryGroup* cell = [collectionView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                           forIndexPath:indexPath];
    
    NSInteger cellTag = indexPath.row;
    
    // get the group
    NSArray* printJobHistoryGroup = [self.listPrintJobHistoryGroups objectAtIndex:cellTag];
    
    // check if the group is collapsed/expanded
    BOOL isCollapsed = [[self.listCollapsedFlags objectAtIndex:cellTag] boolValue];
    
    // set cell contents
    [cell setCellTag:cellTag];
    [cell setCellGroupName:[NSString stringWithFormat:@"%@", [printJobHistoryGroup objectAtIndex:0]]];
    [cell setCellIndicator:isCollapsed];
    [cell setCellPrintJobs:[printJobHistoryGroup subarrayWithRange:
                            NSMakeRange(1, [printJobHistoryGroup count]-1)]];
    
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
    NSArray* printJobHistoryGroup = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    NSLog(@"[INFO][PrintJob] setting size for %@", [printJobHistoryGroup objectAtIndex:0]);
    NSLog(@"[INFO][PrintJob] num print jobs = %ld", (long)[printJobHistoryGroup count]-1);
    
    // set the list height
    CGFloat printJobListHeight;
    BOOL isCollapsed = [[self.listCollapsedFlags objectAtIndex:indexPath.row] boolValue];
    if (isCollapsed)
    {
        // collapsed
        // the height occupied by the print jobs will be zero
        printJobListHeight = 0;
    }
    else
    {
        // expanded
        // the height occupied by the print jobs will be (number of print jobs)*(height for each print job)
        printJobListHeight = ([printJobHistoryGroup count]-1) * PRINT_JOB_ITEM_HEIGHT;
        printJobListHeight += GROUP_MARGIN_BOTTOM;
    }
    
    // finalize the cell dimensions
    CGFloat cellHeight = GROUP_HEADER_HEIGHT + printJobListHeight;
    CGFloat cellWidth = PRINT_JOB_ITEM_WIDTH;
    NSLog(@"[INFO][PrintJob] h=%f,w=%f", cellHeight, cellWidth);
    CGSize cellSize = CGSizeMake(cellWidth, cellHeight);
    
    return cellSize;
}

#pragma mark - Data

- (void)initData
{
    self.listPrintJobHistoryGroups = [NSMutableArray array];
    self.listCollapsedFlags = [NSMutableArray array];

    [self.listCollapsedFlags addObject:[NSNumber numberWithBool:NO]];
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                             @"RISO Printer 1",
                                             @"Print Job A",
                                             @"Print Job B",
                                             @"Print Job C",
                                              nil]];

    [self.listCollapsedFlags addObject:[NSNumber numberWithBool:NO]];
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 2",
                                              @"Print Job D",
                                              @"Print Job E",
                                              @"Print Job F",
                                              @"Print Job G",
                                              nil]];
    
    [self.listCollapsedFlags addObject:[NSNumber numberWithBool:NO]];
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 3",
                                              @"Print Job H",
                                              @"Print Job I",
                                              nil]];
    
    [self.listCollapsedFlags addObject:[NSNumber numberWithBool:NO]];
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 4",
                                              @"Print Job J",
                                              nil]];
    
    [self.listCollapsedFlags addObject:[NSNumber numberWithBool:NO]];
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 5",
                                              @"Print Job K",
                                              @"Print Job L",
                                              @"Print Job M",
                                              nil]];
}

#pragma mark - Actions


- (IBAction)tappedPrinterHeader:(UIButton*)sender
{
    // get the cell tapped
    NSInteger cellIndex = [sender tag];
    NSLog(@"[INFO][PrintJob] tapped cell=%ld", (long)cellIndex);
    
    // toggle collapsed/expanded
    BOOL isCollapsed = [[self.listCollapsedFlags objectAtIndex:cellIndex] boolValue];
    [self.listCollapsedFlags setObject:[NSNumber numberWithBool:!isCollapsed]
                    atIndexedSubscript:cellIndex];
    
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
    [self.listCollapsedFlags removeObjectAtIndex:cellIndex];
    
    // force redraw
    // with animation (not smooth)
    //[self.collectionView reloadItemsAtIndexPaths:@[index]];
    // without animation //TODO: should have some animation
    [self.collectionView reloadData];
}

@end
