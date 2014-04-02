//
//  PrintJobHistoryLayout.m
//  SmartDeviceApp
//
//  Reference:
//  http://skeuo.com/uicollectionview-custom-layout-tutorial
//
//  Created by Gino Mempin on 4/1/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryLayout.h"

@interface PrintJobHistoryLayout ()

#pragma mark - Properties

/** Spacing between a group and the UICollectionView edges. */
@property (assign, nonatomic) UIEdgeInsets groupInsets;

/** Horizontal spacing between groups. */
@property (assign, nonatomic) CGFloat interGroupSpacingX;

/** Vertical spacing between groups. */
@property (assign, nonatomic) CGFloat interGroupSpacingY;

/** 
 The number of columns to be displayed.
 This layout assumes a vertical scrolling display, so the
 number of columns must fit the collection view width.
 */
@property (assign, nonatomic) NSInteger numberOfColumns;

/** 
 Stores the layout attributes for each group.
 This is required for custom UICollectionViewLayout classes.
 The group's index path is used as the key.
 */
@property (strong, nonatomic) NSDictionary* groupLayoutInfo;

/**
 Stores the current height of each column.
 The column number is used as the key.
 */
@property (strong, nonatomic) NSMutableDictionary* columnHeights;

#pragma mark - Methods

/**
 Calculates the (x,y) origin and the (height,width) size of
 a group at the specified index path.
 @param indexPath
 */
- (CGRect)frameForGroupAtIndexPath:(NSIndexPath*)indexPath;

/**
 Determines which of the existing columns has the shortest
 height so far. This is used when populating the layout, as
 the groups are placed depending on which column has the 
 most space available.
 */
- (NSString*)keyForShortestColumn;

/**
 Determines which of the existing columns has the longest
 height so far. This is used when determining the overall
 collection view content size.
 */
- (NSString*)keyForTallestColumn;

@end

@implementation PrintJobHistoryLayout

#pragma mark - Lifecycle

- (id)init
{
    self = [super init];
    if (self)
    {
        [self setupForOrientation:UIInterfaceOrientationPortrait
                        forDevice:UIUserInterfaceIdiomPhone];
    }
    return self;
}

- (id)initWithCoder:(NSCoder*)aDecoder
{
    self = [super init];
    if (self)
    {
        [self setupForOrientation:UIInterfaceOrientationPortrait
                        forDevice:UIUserInterfaceIdiomPhone];
    }
    return self;
}

#pragma mark - Setup

- (void)setupForOrientation:(UIInterfaceOrientation)orientation forDevice:(UIUserInterfaceIdiom)idiom
{
    if (idiom == UIUserInterfaceIdiomPad)
    {
        //iPad
        
        if (UIInterfaceOrientationIsLandscape(orientation))
        {
            self.interGroupSpacingY = 10.0f;
            self.interGroupSpacingX = 10.0f;
            self.numberOfColumns = 3;
            self.groupInsets = UIEdgeInsetsMake(25.0f,  //T
                                                25.0f,  //L
                                                15.0f,  //B
                                                25.0f); //R

        }
        else
        {
            self.interGroupSpacingY = 10.0f;
            self.interGroupSpacingX = 25.0f;
            self.numberOfColumns = 2;
            self.groupInsets = UIEdgeInsetsMake(25.0f,  //T
                                                55.0f,  //L
                                                15.0f,  //B
                                                55.0f); //R
        }
    }
    else
    {
        //iPhone
        //will only support portrait
        
        self.interGroupSpacingY = 5.0f;
        self.interGroupSpacingX = 0.0f;
        self.numberOfColumns = 1;
        self.groupInsets = UIEdgeInsetsMake(0.0f,  //T
                                            0.0f,  //L
                                            0.0f,  //B
                                            0.0f); //R
    }
    
    // reset tracker for column heights
    self.columnHeights = [NSMutableDictionary dictionaryWithCapacity:self.numberOfColumns];
    for (int col = 0; col < self.numberOfColumns; col++)
    {
        [self.columnHeights setValue:[NSNumber numberWithFloat:0.0f]
                             forKey:[NSString stringWithFormat:@"%d", col]];
    }
    
    [self invalidateLayout];
}

#pragma mark - UICollectionViewLayout Layout Calculation

- (void)prepareLayout
{
    NSMutableDictionary* groupLayoutInfo = [NSMutableDictionary dictionary];
    
    NSInteger section = 0; //expecting only one section for all the groups
    NSInteger groupCount = [self.collectionView numberOfItemsInSection:section];
    NSLog(@"[INFO][PrintJobLayout] sectionCount=%ld, groupCount=%ld", (long)section+1, (long)groupCount);
    
    // for each group in the section
    NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:0 inSection:section];
    for (NSInteger group = 0; group < groupCount; group++)
    {
        // create UICollectionViewLayoutAttributes for the group
        groupIndexPath = [NSIndexPath indexPathForItem:group inSection:section];
        UICollectionViewLayoutAttributes* groupAttributes =
            [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:groupIndexPath];
        
        // set the frame (origin and position) for the group
        groupAttributes.frame = [self frameForGroupAtIndexPath:groupIndexPath];
        
        // add this group's attributes to the dictionary
        groupLayoutInfo[groupIndexPath] = groupAttributes;
    }
    
    self.groupLayoutInfo = groupLayoutInfo;
}

- (CGRect)frameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    // determine the correct row
    NSInteger row = indexPath.item / self.numberOfColumns;

    // determine the correct column
    NSInteger col = indexPath.item % self.numberOfColumns;
    NSString* colKey = [NSString stringWithFormat:@"%ld", (long)col];
    
    NSLog(@"[INFO][PrintJobLayout] row=%ld, col=%ld, colKey=%@", (long)row, (long)col, colKey);
    
    // request for the size of the group from the data source
    CGSize groupSize = [self.delegate sizeForGroupAtIndexPath:indexPath];
    
    // set the x-origin pt.
    CGFloat originX = floorf(self.groupInsets.left + (groupSize.width + self.interGroupSpacingX) * col);
    NSLog(@"[INFO][PrintJobLayout] originX=%f", originX);
    
    // set the y-origin pt.
    CGFloat originY = 0.0f;
    CGFloat currentColumnHeight = [[self.columnHeights valueForKey:colKey] floatValue];
    if (row == 0)
    {
        // this is the first row
        // Y-position will depend only on top inset
        originY = floorf(self.groupInsets.top);
    }
    else
    {
        // from second row onwards
        // Y-position will depend on the existing column height + spacing
        originY = floorf(currentColumnHeight + self.interGroupSpacingY);
    }
    NSLog(@"[INFO][PrintJobLayout] originY=%f", originY);

    // save the new column height
    currentColumnHeight = originY + groupSize.height;
    [self.columnHeights setValue:[NSNumber numberWithFloat:currentColumnHeight] forKey:colKey];
    
    // return the (x-origin, y-origin, width, height) for the group
    return CGRectMake(originX, originY, groupSize.width, groupSize.height);
}

#pragma mark - UICollectionViewLayout Required Methods

- (NSArray*)layoutAttributesForElementsInRect:(CGRect)rect
{
    NSMutableArray* allAttributes = [NSMutableArray arrayWithCapacity:self.groupLayoutInfo.count];
    
    // check which groups are part of the passed CGRect
    [self.groupLayoutInfo enumerateKeysAndObjectsUsingBlock:^(NSIndexPath* indexPath,
                                                              UICollectionViewLayoutAttributes* attributes,
                                                              BOOL* innerStop)
    {
        if (CGRectIntersectsRect(rect, attributes.frame))
            [allAttributes addObject:attributes];
    }];
   
    return allAttributes;
}

- (UICollectionViewLayoutAttributes*)layoutAttributesForItemAtIndexPath:(NSIndexPath*)indexPath
{
    // simply return the group at the specified index path
    return self.groupLayoutInfo[indexPath];
}

- (CGSize)collectionViewContentSize
{
    // height is based on the tallest column
    NSString* maxColumnKey = [self keyForTallestColumn];
    CGFloat height = [[self.columnHeights valueForKey:maxColumnKey] floatValue];
    
    // width is simply the width of the collection view itself
    CGFloat width = self.collectionView.bounds.size.width;
    
    return CGSizeMake(width, height);
}

#pragma mark - Utilities

- (NSString*)keyForShortestColumn
{
    __block NSString* minColumnKey = @"0"; //by default, place in first column
    __block CGFloat minColumnHeight = MAXFLOAT;
    
    [self.columnHeights enumerateKeysAndObjectsUsingBlock:^(NSString* columnKey,
                                                            NSNumber* height,
                                                            BOOL* stop)
     {
         CGFloat columnHeight = [height floatValue];
         if (columnHeight < minColumnHeight)
         {
             minColumnHeight = columnHeight;
             minColumnKey = columnKey;
         }
     }];
    
    return minColumnKey;
}

- (NSString*)keyForTallestColumn
{
    __block NSString* maxColumnKey = @"0"; //by default, use first column
    __block CGFloat maxColumnHeight = 0.0f;
    
    [self.columnHeights enumerateKeysAndObjectsUsingBlock:^(NSString* columnKey,
                                                            NSNumber* height,
                                                            BOOL* stop)
     {
         CGFloat columnHeight = [height floatValue];
         if (columnHeight > maxColumnHeight)
         {
             maxColumnHeight = columnHeight;
             maxColumnKey = columnKey;
         }
     }];
    
    return maxColumnKey;
}

@end
