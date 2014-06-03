//
//  PrintJobHistoryLayout.m
//  SmartDeviceApp
//
//  Reference:
//  http://skeuo.com/uicollectionview-custom-layout-tutorial
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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

/** Stores the fixed frame width of each group */
@property (assign, nonatomic) CGFloat groupWidth;

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
@property (strong, nonatomic) NSMutableDictionary* groupFrames;

/**
 Stores the current height of each column.
 */
@property (strong, nonatomic) NSMutableArray* columnHeights;

@property (strong, nonatomic) NSMutableDictionary* columnAssignmentsPort;
@property (strong, nonatomic) NSMutableDictionary* columnAssignmentsLand;
@property (strong, nonatomic) NSMutableDictionary* columnAssignments;

@property (assign, nonatomic) BOOL relayoutForDelete;
@property (strong, nonatomic) NSIndexPath* deletedItem;
@property (assign, nonatomic) CGFloat deletedItemHeight;
@property (assign, nonatomic) NSInteger affectedColumn;

#pragma mark - Methods

/**
 Calculates the (x,y) origin and the (height,width) size of
 a group at the specified index path.
 @param indexPath
 */
- (CGRect)newFrameForGroupAtIndexPath:(NSIndexPath*)indexPath;
- (CGRect)shiftedFrameForGroupAtIndexPath:(NSIndexPath*)indexPath;

@end

@implementation PrintJobHistoryLayout

#pragma mark - Lifecycle

- (id)init
{
    self = [super init];
    if (self)
    {
        [self invalidateColumAssignments];
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
        [self invalidateColumAssignments];
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
        self.groupWidth = 320.0f;
        
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

        if (UIInterfaceOrientationIsLandscape(orientation))
        {
            CGRect screenRect = [[UIScreen mainScreen] bounds];
            self.groupWidth = screenRect.size.height;
            
            self.interGroupSpacingY = 2.0f;
            self.interGroupSpacingX = 0.0f;
            self.numberOfColumns = 1;
            self.groupInsets = UIEdgeInsetsMake(0.0f,  //T
                                                0.0f,  //L
                                                0.0f,  //B
                                                0.0f); //R
        }
        else
        {
            self.groupWidth = 320.0f;
            
            self.interGroupSpacingY = 2.0f;
            self.interGroupSpacingX = 0.0f;
            self.numberOfColumns = 1;
            self.groupInsets = UIEdgeInsetsMake(0.0f,  //T
                                                0.0f,  //L
                                                0.0f,  //B
                                                0.0f); //R
        }
    }
    
    // reset tracker for column heights
    self.columnHeights = [NSMutableArray arrayWithCapacity:self.numberOfColumns];
    for (int col = 0; col < self.numberOfColumns; col++)
    {
        [self.columnHeights insertObject:[NSNumber numberWithFloat:0.0f]
                                 atIndex:col];
    }
    
    if (UIInterfaceOrientationIsLandscape(orientation))
        self.columnAssignments = self.columnAssignmentsLand;
    else
        self.columnAssignments = self.columnAssignmentsPort;
    
    self.relayoutForDelete = NO;
    self.deletedItem = nil;
    
    [self invalidateLayout];
}

#pragma mark - UICollectionViewLayout Layout Calculation

- (void)prepareLayout
{
    NSMutableDictionary* groupLayoutInfo = [NSMutableDictionary dictionary];
    
    NSInteger section = 0; //expecting only one section for all the groups
    NSInteger groupCount = [self.collectionView numberOfItemsInSection:section];
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] sectionCount=%ld, groupCount=%ld", (long)section, (long)groupCount);
#endif
    
    // for each group in the section
    for (NSInteger group = 0; group < groupCount; group++)
    {
        // create UICollectionViewLayoutAttributes for the group
        NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:group inSection:section];
        UICollectionViewLayoutAttributes* groupAttributes =
            [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:groupIndexPath];
        
        if (self.relayoutForDelete)
        {
            // shift frames below the deleted group
            groupAttributes.frame = [self shiftedFrameForGroupAtIndexPath:groupIndexPath];
        }
        else
        {
            // generate a new frame
            groupAttributes.frame = [self newFrameForGroupAtIndexPath:groupIndexPath];
        }
        
        // add this group's attributes to the dictionary
        groupLayoutInfo[groupIndexPath] = groupAttributes;
    }
    
    self.relayoutForDelete = NO;
    self.deletedItem = nil;
    
    self.groupLayoutInfo = groupLayoutInfo;
}

- (CGRect)newFrameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    // determine the correct row
    NSUInteger row = indexPath.item / self.numberOfColumns;

    // determine the correct column
    //NSUInteger col = indexPath.item % self.numberOfColumns;
    NSUInteger col;
    NSNumber* pos = [self.columnAssignments valueForKey:[NSString stringWithFormat:@"%ld", (long)indexPath.item]];
    if (pos == nil)
    {
        NSNumber* minColumnHeight = [self.columnHeights valueForKeyPath:@"@min.self"];
        col = [self.columnHeights indexOfObject:minColumnHeight];
        [self.columnAssignments setValue:[NSNumber numberWithUnsignedInteger:col]
                                  forKey:[NSString stringWithFormat:@"%ld", (long)indexPath.item]];
    }
    else
    {
        col = [pos unsignedIntegerValue];
    }
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] row=%lu, col=%lu", (unsigned long)row, (unsigned long)col);
#endif
    
    // determine the group size
    // group height = header height + (number of jobs * height per job)
    CGFloat groupHeight = 45.0f + ([self.delegate numberOfJobsForGroupAtIndexPath:indexPath] * 45.0f);
    // group width = fixed frame width
    CGFloat groupWidth = self.groupWidth;
    CGSize groupSize = CGSizeMake(groupWidth, groupHeight);
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobCtrl] h=%f,w=%f", groupHeight, groupWidth);
#endif
    
    // set the x-origin pt.
    CGFloat originX = floorf(self.groupInsets.left + (groupSize.width + self.interGroupSpacingX) * col);
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] originX=%f", originX);
#endif
    
    // set the y-origin pt.
    CGFloat originY = 0.0f;
    CGFloat currentColumnHeight = [[self.columnHeights objectAtIndex:col] floatValue];
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
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] originY=%f", originY);
#endif

    // save the new column height
    currentColumnHeight = originY + groupSize.height;
    [self.columnHeights replaceObjectAtIndex:col withObject:[NSNumber numberWithFloat:currentColumnHeight]];
    
    // return the (x-origin, y-origin, width, height) for the group
    return CGRectMake(originX, originY, groupSize.width, groupSize.height);
}

- (CGRect)shiftedFrameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    NSInteger currentItem = indexPath.item;
    NSInteger deletedItem = self.deletedItem.item;
    NSInteger currentCol = [[self.columnAssignments
                           valueForKey:[NSString stringWithFormat:@"%d", currentItem]] integerValue];
    
    if ((currentCol == self.affectedColumn) && (currentItem >= deletedItem))
    {
        // shift the old frame
        NSArray* oldFrameProps = [self.groupFrames objectForKey:[NSString stringWithFormat:@"%d", currentItem+1]];
        CGRect newFrame = CGRectMake([oldFrameProps[0] floatValue],
                                     [oldFrameProps[1] floatValue] - self.deletedItemHeight - self.interGroupSpacingY,
                                     [oldFrameProps[2] floatValue],
                                     [oldFrameProps[3] floatValue]);
        return newFrame;
    }
    else
    {
        NSInteger oldItemIndex;
        if (currentItem >= deletedItem)
        {
            oldItemIndex = currentItem+1;
        }
        else
        {
            oldItemIndex = currentItem;
        }
        
        // reuse the old frame
        NSArray* oldFrameProps = [self.groupFrames objectForKey:[NSString stringWithFormat:@"%d", oldItemIndex]];
        CGRect newFrame = CGRectMake([oldFrameProps[0] floatValue],
                                     [oldFrameProps[1] floatValue],
                                     [oldFrameProps[2] floatValue],
                                     [oldFrameProps[3] floatValue]);
        return newFrame;
    }
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
    NSNumber* maxColumnHeight = [self.columnHeights valueForKeyPath:@"@max.self"]; //KVO-style
    CGFloat height = [maxColumnHeight floatValue] + 25.0f; //bottom space border
    
    // width is simply the width of the collection view itself
    CGFloat width = self.collectionView.bounds.size.width;
    
    return CGSizeMake(width, height);
}

#pragma mark - Controls

- (void)invalidateColumAssignments
{
    self.columnAssignments = nil;
    
    [self.columnAssignmentsPort removeAllObjects];
    self.columnAssignmentsPort = [NSMutableDictionary dictionary];
    
    [self.columnAssignmentsLand removeAllObjects];
    self.columnAssignmentsLand = [NSMutableDictionary dictionary];
}

- (void)prepareForDelete:(NSIndexPath*)itemToDelete
{
    self.relayoutForDelete = YES;
    self.deletedItem = itemToDelete;
    self.affectedColumn = [[self.columnAssignments
                              valueForKey:[NSString stringWithFormat:@"%d", itemToDelete.item]] integerValue];
    
    [self backupFrames];
    [self updateColumnAssignmentsForDeletedItem];
    
    NSArray* deletedItemFrame = [self.groupFrames objectForKey:[NSString stringWithFormat:@"%d", itemToDelete.item]];
    self.deletedItemHeight = [deletedItemFrame[3] floatValue];
}

#pragma mark - Utilities

- (void)backupFrames
{
    // backup current frames
    self.groupFrames = [NSMutableDictionary dictionary];
    [self.groupLayoutInfo enumerateKeysAndObjectsUsingBlock:^(NSIndexPath* key, id obj, BOOL *stop) {
        
        UICollectionViewLayoutAttributes* attr = (UICollectionViewLayoutAttributes*)obj;
        CGRect oldFrame = attr.frame;
        NSArray* props = @[[NSNumber numberWithFloat:oldFrame.origin.x],
                           [NSNumber numberWithFloat:oldFrame.origin.y],
                           [NSNumber numberWithFloat:oldFrame.size.width],
                           [NSNumber numberWithFloat:oldFrame.size.height]];
        
        NSString* keyStr = [NSString stringWithFormat:@"%d", key.item];
        [self.groupFrames setObject:props forKey:keyStr];
    }];
}

- (void)updateColumnAssignmentsForDeletedItem
{
    __block NSMutableDictionary* updatedAssignments;
    
    // update column assignments
    updatedAssignments = [NSMutableDictionary dictionary];
    [self.columnAssignments removeObjectForKey:[NSString stringWithFormat:@"%d", self.deletedItem.item]];
    [self.columnAssignments enumerateKeysAndObjectsUsingBlock:^(NSString* item, NSNumber* col, BOOL *stop) {
        if ([item intValue] > self.deletedItem.item)
        {
            [updatedAssignments setObject:col forKey:[NSString stringWithFormat:@"%d", [item intValue]-1]];
        }
        else
        {
            [updatedAssignments setObject:col forKey:item];
        }
    }];
    self.columnAssignments = updatedAssignments;
    
    // update column assignments (Port)
    updatedAssignments = [NSMutableDictionary dictionary];
    [self.columnAssignmentsPort removeObjectForKey:[NSString stringWithFormat:@"%d", self.deletedItem.item]];
    [self.columnAssignmentsPort enumerateKeysAndObjectsUsingBlock:^(NSString* item, NSNumber* col, BOOL *stop) {
        if ([item intValue] > self.deletedItem.item)
        {
            [updatedAssignments setObject:col forKey:[NSString stringWithFormat:@"%d", [item intValue]-1]];
        }
        else
        {
            [updatedAssignments setObject:col forKey:item];
        }
    }];
    self.columnAssignmentsPort = updatedAssignments;
    
    // update column assignments (Land)
    updatedAssignments = [NSMutableDictionary dictionary];
    [self.columnAssignmentsLand removeObjectForKey:[NSString stringWithFormat:@"%d", self.deletedItem.item]];
    [self.columnAssignmentsLand enumerateKeysAndObjectsUsingBlock:^(NSString* item, NSNumber* col, BOOL *stop) {
        if ([item intValue] > self.deletedItem.item)
        {
            [updatedAssignments setObject:col forKey:[NSString stringWithFormat:@"%d", [item intValue]-1]];
        }
        else
        {
            [updatedAssignments setObject:col forKey:item];
        }
    }];
    self.columnAssignmentsLand = updatedAssignments;
}

@end
