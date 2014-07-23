//
//  RenderCache.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "RenderCache.h"

@implementation RenderCacheItem

@end

@interface RenderCache ()

/**
 * Maximum number of cached page indices.
 */
@property (nonatomic) NSInteger maxItemCount;

/**
 * Array of page indices currently cached.
 */
@property (nonatomic, strong) NSMutableIndexSet *indexSet;

/**
 * Array of RenderCacheItem currently cached.
 */
@property (nonatomic, strong) NSMutableDictionary *itemsDictionary;

/**
 * Removes item from the cache
 * @param index
 *        Index of of the item
 */
- (void)removeItemAtIndex:(NSUInteger)index;

/**
 * Performs clean-up on low memory
 */
- (void)didReceiveMemoryWarning;

@end

@implementation RenderCache

#pragma mark - Public Methods

- (id)init
{
    return [self initWithMaxItemCount:-1];
}

- (id)initWithMaxItemCount:(NSInteger)maxItemCount
{
    self = [super init];
    if (self)
    {
        // Initialize
        _indexSet = [[NSMutableIndexSet alloc] init];
        _itemsDictionary = [[NSMutableDictionary alloc] init];
        _maxItemCount = maxItemCount;
        
        // Subscribe to low memory notification
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didReceiveMemoryWarning) name:UIApplicationDidReceiveMemoryWarningNotification object:nil];
    }
    return self;
}

- (void)dealloc
{
    // Unsubscribe to low memory notification
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidReceiveMemoryWarningNotification object:nil];
}

- (void)addItem:(RenderCacheItem *)item withIndex:(NSUInteger)index
{
    NSNumber *indexKey = [NSNumber numberWithUnsignedInteger:index];
    
    // Replace older item if it is available
    if ([self.indexSet containsIndex:index])
    {
        [self.itemsDictionary setObject:item forKey:indexKey];
        return;
    }
    
    // Free up space if item count is already at maximum
    if (self.maxItemCount > 0 && self.indexSet.count == self.maxItemCount)
    {
        // Get the current item from the delegate
        NSUInteger currentIndex = [self.delegate currentIndex];
        
        NSUInteger firstIndex = [self.indexSet firstIndex];
        NSUInteger lastIndex = [self.indexSet lastIndex];
        
        if (currentIndex < firstIndex)
        {
            // Remove the last cached item if the current item is before the first cached item
            [self removeItemAtIndex:lastIndex];
        }
        else if (currentIndex > lastIndex)
        {
            // Remove the first cached item if the current item is after the last cached item
            [self removeItemAtIndex:firstIndex];
        }
        else
        {
            // Remove the first or last cached item, whichever is farthest from the current item
            NSUInteger backwardDifference = currentIndex - firstIndex; NSUInteger forwardDifference = lastIndex - currentIndex;
            if (forwardDifference >= backwardDifference)
            {
                [self removeItemAtIndex:lastIndex];
            }
            else
            {
                [self removeItemAtIndex:firstIndex];
            }
        }
    }

    // Add the item to the cache
    [self.indexSet addIndex:index];
    [self.itemsDictionary setObject:item forKey:indexKey];
}

- (RenderCacheItem *)itemWithIndex:(NSUInteger)index
{
    return [self.itemsDictionary objectForKey:[NSNumber numberWithUnsignedInteger:index]];
}

- (void)removeAllItems
{
    [self.indexSet removeAllIndexes];
    [self.itemsDictionary removeAllObjects];
}

#pragma mark - Helper Methods

- (void)removeItemAtIndex:(NSUInteger)index
{
    NSNumber *indexKey = [NSNumber numberWithUnsignedInteger:index];
    [self.indexSet removeIndex:index];
    [self.itemsDictionary removeObjectForKey:indexKey];
}

#pragma mark - Notification Handler Methods

- (void)didReceiveMemoryWarning
{
    // Save the current item from deletion
    NSUInteger currentIndex = [self.delegate currentIndex];
    RenderCacheItem *item = [self.itemsDictionary objectForKey:[NSNumber numberWithUnsignedInteger:currentIndex]];
    
    [self removeAllItems];
    
    // Put back the current item to the cache
    [self.indexSet addIndex:currentIndex];
    [self.itemsDictionary setObject:item forKey:[NSNumber numberWithUnsignedInteger:currentIndex]];
}

@end
