//
//  RenderCache.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RenderCacheItem;
@protocol RenderCacheDelegate;
@class PDFPageContentViewController;

/**
 * RenderCache class creates a cache of the loaded document.
 */
@interface RenderCache : NSObject

/**
 * Maximum number of items the cache should hold
 */
@property (nonatomic, readonly) NSInteger maxItemCount;

/**
 * The delegate that provides the current/active index
 */
@property (nonatomic, weak) id<RenderCacheDelegate>delegate;


/**
 Initialize a RenderCache object with maximum item count
 @param maxItemCount
        Maximum number of items the cache should hold
 @return RenderCache object
 */
- (id)initWithMaxItemCount:(NSInteger)maxItemCount;

/**
 Adds item to the cache
 @param item
        A RenderCacheItem object to be added
 @param index
        Index of the object
 */
- (void)addItem:(RenderCacheItem *)item withIndex:(NSUInteger)index;

/**
 Retrieves an item from the cache
 @param index
    Index of the object
 @return RenderCacheItem object
 */
- (RenderCacheItem *)itemWithIndex:(NSUInteger)index;

/**
 Removes all items in the cache
 */
- (void)removeAllItems;

@end

/**
 * RenderCacheItem class contains the image of the cached index.
 */
@interface RenderCacheItem : NSObject

/**
 View Controller of a UIPageViewController
 */
@property (nonatomic, strong) PDFPageContentViewController *viewController;

/**
 Image of the cached index
 */
@property (nonatomic, strong) UIImage *image;

@end

/**
 * RenderCacheDelegate protocol provides method to get the current index of the document.
 */
@protocol RenderCacheDelegate <NSObject>

@required

/**
 Gets the current index.
 @return The current index
 */
- (NSUInteger)currentIndex;

@end
