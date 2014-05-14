//
//  PrinterLayout.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterLayout.h"

@interface PrinterLayout()

@property (nonatomic) CGSize itemSize;
@property (nonatomic) CGFloat spacing;
@property (nonatomic) NSInteger numberOfColumns;
@property (nonatomic, strong) NSDictionary *layoutInfo;

@end

@implementation PrinterLayout

- (id)init
{
    self = [super init];
    if (self)
    {
        [self setup];
    }
    
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self setup];
    }
    
    return self;
}

- (void)setup
{
    self.itemSize = CGSizeMake(320.0f, 270.0f);
    self.spacing = 10.0f;
}

- (BOOL)shouldInvalidateLayoutForBoundsChange:(CGRect)newBounds
{
    return (CGRectGetWidth(newBounds) != CGRectGetWidth(self.collectionView.bounds));
}

- (void)prepareLayout
{
    if (self.collectionView && (self.collectionView.bounds.size.width > self.collectionView.bounds.size.height))
    {
        self.numberOfColumns = 3;
    }
    else
    {
        self.numberOfColumns = 2;
    }
    
    NSMutableDictionary *cellLayoutInfo = [NSMutableDictionary dictionary];
    
    NSInteger sectionCount = [self.collectionView numberOfSections];
    NSIndexPath *indexPath;
    
    for (NSInteger section = 0; section < sectionCount; section++)
    {
        NSInteger itemCount = [self.collectionView numberOfItemsInSection:section];
        for (NSInteger item = 0; item < itemCount; item++)
        {
            indexPath = [NSIndexPath indexPathForItem:item inSection:section];
            
            UICollectionViewLayoutAttributes *itemAttributes = [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:indexPath];
            itemAttributes.frame = [self frameForCellAtIndexPath:indexPath];
            
            cellLayoutInfo[indexPath] = itemAttributes;
        }
    }
    
    self.layoutInfo = cellLayoutInfo;
}

- (CGRect)frameForCellAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger row = indexPath.item / self.numberOfColumns;
    NSInteger column = indexPath.item % self.numberOfColumns;
    
    CGFloat contentWidth = self.numberOfColumns * self.itemSize.width + (self.numberOfColumns - 1) * self.spacing;
    CGPoint origin;
    origin.x = floorf((self.collectionView.bounds.size.width - contentWidth) / 2.0f);
    origin.y = self.spacing;
    
    CGFloat x = floorf(origin.x + (self.itemSize.width + self.spacing) * column);
    CGFloat y = floorf(origin.y + (self.itemSize.height + self.spacing) * row);
    
    return CGRectMake(x, y, self.itemSize.width, self.itemSize.height);
}

- (NSArray *)layoutAttributesForElementsInRect:(CGRect)rect
{
    NSMutableArray *allAttributes = [NSMutableArray arrayWithCapacity:self.layoutInfo.count];
    
    [self.layoutInfo enumerateKeysAndObjectsUsingBlock:^(NSIndexPath *indexPath,
                                                         UICollectionViewLayoutAttributes *attributes,
                                                         BOOL *stop) {
        if (CGRectIntersectsRect(rect, attributes.frame))
        {
            [allAttributes addObject:attributes];
        }
    }];
    return allAttributes;
}

- (UICollectionViewLayoutAttributes *)layoutAttributesForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return self.layoutInfo[indexPath];
}

- (CGSize)collectionViewContentSize
{
    NSInteger numberOfRows = [self.collectionView numberOfItemsInSection:0] / self.numberOfColumns;
    if ([self.collectionView numberOfItemsInSection:0] % self.numberOfColumns)
    {
        numberOfRows++;
    }
    
    CGFloat height = numberOfRows * self.itemSize.height + (numberOfRows + 1) * self.spacing;
    return CGSizeMake(self.collectionView.bounds.size.width, height);
}

@end
