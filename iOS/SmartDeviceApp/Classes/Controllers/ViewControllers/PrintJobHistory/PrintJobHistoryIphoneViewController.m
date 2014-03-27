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
#define PRINTER_NAME_ROW_HEIGHT     45.0f  //should match the value in storyboard
#define PRINT_JOB_ITEM_ROW_HEIGHT   45.0f  //should match the value in storyboard
#define PRINT_JOB_ITEM_ROW_WIDTH    320.f  //should match the value in storyboard
#define CELL_BOTTOM_MARGIN          -5.0f  //TODO: how to properly set margin (iOS7 != iOS6)

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

#pragma mark - Methods

- (void)initData;

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
    return 5;
}

- (UICollectionViewCell*)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    PrintJobHistoryGroup* cell = [collectionView dequeueReusableCellWithReuseIdentifier:GROUPCELL
                                                                           forIndexPath:indexPath];
    
    // get the group
    NSArray* printJobHistoryGroup = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    
    // set cell contents
    [cell setCellGroupName:[NSString stringWithFormat:@"%@", [printJobHistoryGroup objectAtIndex:0]]];
    [cell setCellPrintJobs:[printJobHistoryGroup subarrayWithRange:
                            NSMakeRange(1, [printJobHistoryGroup count]-1)]];
    
    // since we are using reusable cells, handle scrolling by forcing redraw of the cell
    [cell reloadContents];
    
    return cell;
}

#pragma mark - CollectionViewFlowLayout

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    // get the group
    NSArray* printJobHistoryGroup = [self.listPrintJobHistoryGroups objectAtIndex:indexPath.row];
    
    CGFloat printJobListHeight = ([printJobHistoryGroup count]-1) * PRINT_JOB_ITEM_ROW_HEIGHT;
    CGFloat cellHeight = PRINTER_NAME_ROW_HEIGHT + printJobListHeight + CELL_BOTTOM_MARGIN;
    CGFloat cellWidth = PRINT_JOB_ITEM_ROW_WIDTH;
    
    CGSize cellSize = CGSizeMake(cellWidth, cellHeight);
    
    return cellSize;
}

#pragma mark - Data

- (void)initData
{
    self.listPrintJobHistoryGroups = [NSMutableArray array];
    
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                             @"RISO Printer 1",
                                             @"Print Job A",
                                             @"Print Job B",
                                             @"Print Job C",
                                              nil]];

    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 2",
                                              @"Print Job D",
                                              @"Print Job E",
                                              @"Print Job F",
                                              @"Print Job G",
                                              nil]];
    
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 3",
                                              @"Print Job H",
                                              @"Print Job I",
                                              nil]];
    
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 4",
                                              @"Print Job J",
                                              nil]];
    
    [self.listPrintJobHistoryGroups addObject:[NSArray arrayWithObjects:
                                              @"RISO Printer 5",
                                              @"Print Job K",
                                              @"Print Job L",
                                              @"Print Job M",
                                              nil]];
}

@end
