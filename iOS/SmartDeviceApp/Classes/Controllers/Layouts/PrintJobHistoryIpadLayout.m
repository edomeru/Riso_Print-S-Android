//
//  PrintJobHistoryIpadLayout.m
//  SmartDeviceApp
//
//  Reference:
//  http://skeuo.com/uicollectionview-custom-layout-tutorial
//
//  Created by Gino Mempin on 4/1/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryIpadLayout.h"

const float EDGE_INSET_LAND_T = 25.0f;
const float EDGE_INSET_LAND_B = 15.0f;
const float EDGE_INSET_LAND_L = 25.0f;
const float EDGE_INSET_LAND_R = 25.0f;

const float EDGE_INSET_PORT_T = 25.0f;
const float EDGE_INSET_PORT_B = 15.0f;
const float EDGE_INSET_PORT_L = 55.0f;
const float EDGE_INSET_PORT_R = 55.0f;

const float GROUP_SPACING_LAND_X = 10.0f;
const float GROUP_SPACING_PORT_X = 25.0f;
const float GROUP_SPACING_Y = 10.0f;

const unsigned int NUM_COLUMNS_LAND = 3;
const unsigned int NUM_COLUMNS_PORT = 2;

@interface PrintJobHistoryIpadLayout ()

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
 The group's index path is used as the key.
 */
@property (strong, nonatomic) NSDictionary* groupLayoutInfo;

/**
 Stores the current height of each column.
 The column number is used as the key and the height is the value.
 */
@property (strong, nonatomic) NSMutableDictionary* columnHeight;

/**
 Calculates the (x,y) origin and the (height,width) size of
 a group at the specified index path.
 @param indexPath
 */
- (CGRect)frameForGroupAtIndexPath:(NSIndexPath*)indexPath;

@end

@implementation PrintJobHistoryIpadLayout

#pragma mark - Lifecycle

- (id)init
{
    self = [super init];
    if (self)
    {
        [self setupForOrientation:UIInterfaceOrientationPortrait];
    }
    return self;
}

- (id)initWithCoder:(NSCoder*)aDecoder
{
    self = [super init];
    if (self)
    {
        [self setupForOrientation:UIInterfaceOrientationPortrait];
    }
    return self;
}

#pragma mark - Setup

- (void)setupForOrientation:(UIInterfaceOrientation)orientation
{
    self.interGroupSpacingY = GROUP_SPACING_Y;
    
    if (UIInterfaceOrientationIsLandscape(orientation))
    {
        self.numberOfColumns = NUM_COLUMNS_LAND;
        self.groupInsets = UIEdgeInsetsMake(EDGE_INSET_LAND_T,
                                            EDGE_INSET_LAND_L,
                                            EDGE_INSET_LAND_B,
                                            EDGE_INSET_LAND_R);
        self.interGroupSpacingX = GROUP_SPACING_LAND_X;
    }
    else
    {
        self.numberOfColumns = NUM_COLUMNS_PORT;
        self.groupInsets = UIEdgeInsetsMake(EDGE_INSET_PORT_T,
                                            EDGE_INSET_PORT_L,
                                            EDGE_INSET_PORT_B,
                                            EDGE_INSET_PORT_R);
        self.interGroupSpacingX = GROUP_SPACING_PORT_X;
    }
    
    self.columnHeight = [NSMutableDictionary dictionary];
    [self.columnHeight setValue:[NSNumber numberWithFloat:0.0f] forKey:@"0"];
    [self.columnHeight setValue:[NSNumber numberWithFloat:0.0f] forKey:@"1"];
    [self.columnHeight setValue:[NSNumber numberWithFloat:0.0f] forKey:@"2"];
    
    [self invalidateLayout];
}

#pragma mark - UICollectionViewLayout Layout Calculation

- (void)prepareLayout
{
    NSMutableDictionary* groupLayoutInfo = [NSMutableDictionary dictionary];
    
    NSInteger section = 0; //expecting only one section for all the groups
    NSInteger itemCount = [self.collectionView numberOfItemsInSection:section];
    NSLog(@"[INFO][PrintJobLayout] sectionCount=%ld, itemCount=%ld", (long)section+1, (long)itemCount);
    
    // for each group in the section
    NSIndexPath* indexPath = [NSIndexPath indexPathForItem:0 inSection:section];
    for (NSInteger item = 0; item < itemCount; item++)
    {
        // create UICollectionViewLayoutAttributes for the group
        indexPath = [NSIndexPath indexPathForItem:item inSection:section];
        UICollectionViewLayoutAttributes* itemAttributes =
            [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:indexPath];
        
        // set the frame (origin and position) for the group
        itemAttributes.frame = [self frameForGroupAtIndexPath:indexPath];
        
        // add this group's attributes to the dictionary
        groupLayoutInfo[indexPath] = itemAttributes;
    }
    
    self.groupLayoutInfo = groupLayoutInfo;
}

- (CGRect)frameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    // determine the correct row and column for the item
    NSInteger row = indexPath.item / self.numberOfColumns;
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
    CGFloat currentColumnHeight = [[self.columnHeight valueForKey:colKey] floatValue];
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
    [self.columnHeight setValue:[NSNumber numberWithFloat:currentColumnHeight] forKey:colKey];
    
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
    __block CGFloat maxHeight = 0.0f;
    [self.columnHeight enumerateKeysAndObjectsUsingBlock:^(NSString* key,
                                                           NSNumber* value,
                                                           BOOL* stop)
    {
        CGFloat columnHeight = [value floatValue];
        if (columnHeight > maxHeight)
            maxHeight = columnHeight;
    }];
    
    // width is simply the width of the collection view itself
    CGFloat width = self.collectionView.bounds.size.width;
    
    return CGSizeMake(width, maxHeight);
}

@end
