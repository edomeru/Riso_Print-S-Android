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

/** Stores the current height of each column. */
@property (strong, nonatomic) NSMutableArray* columnHeights;

/** Stores the column positions of each group in the portrait orientation. */
@property (strong, nonatomic) NSMutableDictionary* columnAssignmentsPort;

/** Stores the column positions of each group in the landscape orientation. */
@property (strong, nonatomic) NSMutableDictionary* columnAssignmentsLand;

/** Reference to the column positions for either the portrait or landscape orientation */
@property (strong, nonatomic) NSMutableDictionary* columnAssignments;

/** 
 Flag that a column will be empty when the layout operation finishes.
 This flag is updated when a group is deleted while in landscape orientation.
 This is used when rotating from landscape to portrait.
 */
@property (assign, nonatomic) BOOL columnWillBeEmptyInLand;

/**
 Flag that a column will be empty when the layout operation finishes.
 This flag is updated when a group is deleted while in portrait orientation.
 This is used when rotating from portrait to landscape.
 */
@property (assign, nonatomic) BOOL columnWillBeEmptyInPort;

/** Flag that indicates that the layout was invalidated because a group was deleted. */
@property (assign, nonatomic) BOOL relayoutForDelete;

/** Reference to the deleted group. */
@property (strong, nonatomic) NSIndexPath* deletedItem;

/** Reference to the frame height of the deleted group. */
@property (assign, nonatomic) CGFloat deletedItemHeight;

/** Reference to the column where the deleted group was originally assigned. */
@property (assign, nonatomic) NSInteger affectedColumn;

/** Stores the current device orientation. */
@property (assign, nonatomic) UIInterfaceOrientation orientation;

#pragma mark - Methods

/**
 Calculates the (x,y) origin and the (height,width) size of
 a group at the specified index path based on the number of
 jobs and the current height of each column.
 @param indexPath
 */
- (CGRect)newFrameForGroupAtIndexPath:(NSIndexPath*)indexPath;

/**
 Adjusts the frames of groups below a deleted group in a column.
 For the other groups, returns their current frame.
 @param indexPath
 */
- (CGRect)shiftedFrameForGroupAtIndexPath:(NSIndexPath*)indexPath;

/**
 Removes the deleted group from the column positions tracker (for
 both landscape and portrait). The group indices are also shifted
 to the left to fill the vacated position.
 */
- (void)updateColumnAssignmentsForDeletedItem;

/** Clears the tracker for the column heights. */
- (void)invalidateColumnHeights;

/** Cancels the relayoutForDelete flag and clears any reference to the deleted group. */
- (void)setNotLayoutForDelete;

/** 
 Assigns the correct tracker for the column positions
 based on the current device orientation.
 */
- (void)assignColumnAssignmentsForOrientation;

@end

@implementation PrintJobHistoryLayout

#pragma mark - Lifecycle

- (id)init
{
    self = [super init];
    if (self)
    {
        [self invalidateColumnAssignments];
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
        [self invalidateColumnAssignments];
        [self setupForOrientation:UIInterfaceOrientationPortrait
                        forDevice:UIUserInterfaceIdiomPhone];
    }
    return self;
}

#pragma mark - Controls

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
    
    self.orientation = orientation;
    
    if ((UIInterfaceOrientationIsLandscape(orientation) && self.columnWillBeEmptyInLand)
        || (UIInterfaceOrientationIsPortrait(orientation) && self.columnWillBeEmptyInPort))
    {
        // screen has been loaded before
        // groups are already assigned to columns
        // a group was deleted in the previous orientation
        // need to relayout
        
        [self invalidateColumnAssignments];
    }
    else
    {
        // reuse existing column assignments
        
        [self assignColumnAssignmentsForOrientation];
    }
    
    [self invalidateColumnHeights];
    [self setNotLayoutForDelete];
    
    [self invalidateLayout];
}

- (void)invalidateColumnAssignments
{
    self.columnAssignments = nil;
    self.columnWillBeEmptyInPort = NO;
    self.columnWillBeEmptyInLand = NO;
    
    [self.columnAssignmentsPort removeAllObjects];
    self.columnAssignmentsPort = [NSMutableDictionary dictionary];
    
    [self.columnAssignmentsLand removeAllObjects];
    self.columnAssignmentsLand = [NSMutableDictionary dictionary];
    
    [self assignColumnAssignmentsForOrientation];
}

- (void)prepareForDelete:(NSIndexPath*)itemToDelete
{
    self.relayoutForDelete = YES;
    self.deletedItem = itemToDelete;
    self.affectedColumn = [[self.columnAssignments
                            valueForKey:[NSString stringWithFormat:@"%d", (int)itemToDelete.item]] integerValue];
    
    // get the height of the deleted item
    CGRect oldFrame = [(UICollectionViewLayoutAttributes*)self.groupLayoutInfo[itemToDelete] frame];
    self.deletedItemHeight = oldFrame.size.height;
    
    // update the tracker for column heights
    CGFloat currentColumnHeight = [[self.columnHeights objectAtIndex:self.affectedColumn] floatValue];
    CGFloat newColumnHeight = currentColumnHeight - self.deletedItemHeight - self.interGroupSpacingY;
    if (newColumnHeight <= self.groupInsets.top)
    {
        // column will be empty
        // need to relayout everything after delete
        
        [self setNotLayoutForDelete];
        [self invalidateColumnAssignments];
    }
    else
    {
        [self updateColumnAssignmentsForDeletedItem];
        [self.columnHeights replaceObjectAtIndex:self.affectedColumn
                                      withObject:[NSNumber numberWithFloat:newColumnHeight]];
    }
}

#pragma mark - UICollectionViewLayout Layout Calculation

- (void)prepareLayout
{
    if (!self.relayoutForDelete)
    {
        [self invalidateColumnHeights];
    }
    
    // initialize the container for the NEW group frames
    NSMutableDictionary* groupLayoutInfo = [NSMutableDictionary dictionary];
    
    NSInteger section = 0; //expecting only one section for all the groups
    NSInteger groupCount = [self.collectionView numberOfItemsInSection:section];
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] sectionCount=%ld, groupCount=%ld", (long)section, (long)groupCount);
#endif
    
    // for each group in the section
    for (NSInteger group = 0; group < groupCount; group++)
    {
#if DEBUG_LOG_PRINT_JOB_LAYOUT
        NSLog(@"[INFO][PrintJobLayout] group=%ld", (long)group);
#endif
        
        // create UICollectionViewLayoutAttributes for the group
        NSIndexPath* groupIndexPath = [NSIndexPath indexPathForItem:group inSection:section];
        UICollectionViewLayoutAttributes* groupAttributes =
            [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:groupIndexPath];
        
        if (self.relayoutForDelete)
        {
            // shift frames below the deleted group
            // re-use existing frames of unaffected groups
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
    
    [self setNotLayoutForDelete];
    
    // replace the container for the group frames
    self.groupLayoutInfo = groupLayoutInfo;
}

- (CGRect)newFrameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    // determine the correct column
    NSUInteger col;
    NSNumber* prevCol = [self.columnAssignments valueForKey:[NSString stringWithFormat:@"%d", (int)indexPath.item]];
    if (prevCol == nil)
    {
        NSNumber* minColumnHeight = [self.columnHeights valueForKeyPath:@"@min.self"];
        col = [self.columnHeights indexOfObject:minColumnHeight];
        [self.columnAssignments setValue:[NSNumber numberWithUnsignedInteger:col]
                                  forKey:[NSString stringWithFormat:@"%d", (int)indexPath.item]];
    }
    else
    {
        col = [prevCol unsignedIntegerValue];
    }
    
    // determine the group size
    // group height = header height + (number of jobs * height per job)
    CGFloat groupHeight = 45.0f + ([self.delegate numberOfJobsForGroupAtIndexPath:indexPath] * 45.0f);
    // group width = fixed frame width
    CGFloat groupWidth = self.groupWidth;
    CGSize groupSize = CGSizeMake(groupWidth, groupHeight);
    
#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] h=%f,w=%f", groupSize.height, groupSize.width);
#endif
    
    // determine the group origin
    // set the x-origin pt.
    CGFloat originX = floorf(self.groupInsets.left + (groupSize.width + self.interGroupSpacingX) * col);
    // set the y-origin pt.
    CGFloat originY = 0.0f;
    CGFloat currentColumnHeight = [[self.columnHeights objectAtIndex:col] floatValue];
    if (currentColumnHeight == 0.0f)
    {
        // first item in a column
        // Y-position will depend only on top inset
        originY = floorf(self.groupInsets.top);
    }
    else
    {
        // second item onwards per column
        // Y-position will depend on the existing column height + spacing
        originY = floorf(currentColumnHeight + self.interGroupSpacingY);
    }
    CGPoint groupOrigin = CGPointMake(originX, originY);

#if DEBUG_LOG_PRINT_JOB_LAYOUT
    NSLog(@"[INFO][PrintJobLayout] x=%f,y=%f", groupOrigin.x, groupOrigin.y);
#endif

    // save the new column height
    currentColumnHeight = originY + groupSize.height;
    [self.columnHeights replaceObjectAtIndex:col withObject:[NSNumber numberWithFloat:currentColumnHeight]];
    
    // return the (x-origin, y-origin, width, height) for the group
    return CGRectMake(groupOrigin.x, groupOrigin.y, groupSize.width, groupSize.height);
}

- (CGRect)shiftedFrameForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    NSInteger currentItem = indexPath.item;
    NSInteger deletedItem = self.deletedItem.item;
    NSInteger currentCol = [[self.columnAssignments
                           valueForKey:[NSString stringWithFormat:@"%d", (int)currentItem]] integerValue];
    
    if ((currentCol == self.affectedColumn) && (currentItem >= deletedItem))
    {
        // shift the old frame
        NSIndexPath* oldIndexPath = [NSIndexPath indexPathForItem:currentItem+1 inSection:0];
        CGRect oldFrame = [(UICollectionViewLayoutAttributes*)self.groupLayoutInfo[oldIndexPath] frame];
        CGRect newFrame = CGRectMake(oldFrame.origin.x,
                                     oldFrame.origin.y - self.deletedItemHeight - self.interGroupSpacingY,
                                     oldFrame.size.width,
                                     oldFrame.size.height);
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
        NSIndexPath* oldIndexPath = [NSIndexPath indexPathForItem:oldItemIndex inSection:0];
        CGRect oldFrame = [(UICollectionViewLayoutAttributes*)self.groupLayoutInfo[oldIndexPath] frame];
        return oldFrame;
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

#pragma mark - Utilities

- (void)updateColumnAssignmentsForDeletedItem
{
    __block NSMutableDictionary* updatedAssignments;
    
    // update column assignments (Port)
    if ([self.columnAssignmentsPort count] != 0)
    {
        updatedAssignments = [NSMutableDictionary dictionary];
        __block NSMutableIndexSet* columnsPort = [NSMutableIndexSet indexSet];
        [self.columnAssignmentsPort removeObjectForKey:[NSString stringWithFormat:@"%d", (int)self.deletedItem.item]];
        [self.columnAssignmentsPort enumerateKeysAndObjectsUsingBlock:^(NSString* item, NSNumber* col, BOOL *stop) {
            if ([item intValue] > self.deletedItem.item)
            {
                [updatedAssignments setObject:col forKey:[NSString stringWithFormat:@"%d", (int)[item intValue]-1]];
            }
            else
            {
                [updatedAssignments setObject:col forKey:item];
            }
            [columnsPort addIndex:[col unsignedIntegerValue]];
        }];
        self.columnAssignmentsPort = updatedAssignments;
        if ([columnsPort count] != 2) // expecting 2 columns for portrait
        {
            self.columnWillBeEmptyInPort = YES;
        }
    }
    
    // update column assignments (Land)
    if ([self.columnAssignmentsLand count] != 0)
    {
        updatedAssignments = [NSMutableDictionary dictionary];
        __block NSMutableIndexSet* columnsLand = [NSMutableIndexSet indexSet];
        [self.columnAssignmentsLand removeObjectForKey:[NSString stringWithFormat:@"%d", (int)self.deletedItem.item]];
        [self.columnAssignmentsLand enumerateKeysAndObjectsUsingBlock:^(NSString* item, NSNumber* col, BOOL *stop) {
            if ([item intValue] > self.deletedItem.item)
            {
                [updatedAssignments setObject:col forKey:[NSString stringWithFormat:@"%d", (int)[item intValue]-1]];
            }
            else
            {
                [updatedAssignments setObject:col forKey:item];
            }
            [columnsLand addIndex:[col unsignedIntegerValue]];
        }];
        self.columnAssignmentsLand = updatedAssignments;
        if ([columnsLand count] != 3) // expecting 3 columns for landscape
        {
            self.columnWillBeEmptyInLand = YES;
        }
    }
    
    [self assignColumnAssignmentsForOrientation];
}

- (void)invalidateColumnHeights
{
    self.columnHeights = [NSMutableArray arrayWithCapacity:self.numberOfColumns];
    for (int col = 0; col < self.numberOfColumns; col++)
    {
        [self.columnHeights insertObject:[NSNumber numberWithFloat:0.0f] atIndex:col];
    }
}

- (void)setNotLayoutForDelete
{
    self.relayoutForDelete = NO;
    self.deletedItem = nil;
    self.deletedItemHeight = -1;
    self.affectedColumn = -1;
}

- (void)assignColumnAssignmentsForOrientation
{
    if (UIInterfaceOrientationIsLandscape(self.orientation))
    {
        self.columnAssignments = self.columnAssignmentsLand;
    }
    else
    {
        self.columnAssignments = self.columnAssignmentsPort;
    }
}

@end
