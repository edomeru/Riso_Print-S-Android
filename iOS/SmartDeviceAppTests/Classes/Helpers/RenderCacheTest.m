//
//  RenderCacheTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "RenderCache.h"

#define RENDERCACHE_TEST 1
#if RENDERCACHE_TEST

@interface RenderCache(Test)

@property (nonatomic, strong) NSMutableIndexSet *indexSet;
@property (nonatomic, strong) NSMutableDictionary *itemsDictionary;

@end

@interface RenderCacheTest : GHTestCase<RenderCacheDelegate>

@property (nonatomic, assign) NSUInteger currentIndex;

@end

@implementation RenderCacheTest

- (void)setUp
{
    self.currentIndex = 0;
}

- (void)testInit
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] init];
    
    // Verification
    GHAssertEquals([renderCache maxItemCount], (NSInteger)-1, @"maxItemCount must be -1.");
}

- (void)testInitWithMaxItemCount
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    
    // Verification
    GHAssertEquals([renderCache maxItemCount], (NSInteger)10, @"maxItemCount must be 10.");
}

- (void)testAdd
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:0];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)1, @"Render items must be 1.");
}

- (void)testAdd_Multiple
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:0];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:1];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)2, @"Render items must be 2.");
}

- (void)testAdd_Duplicate
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:0];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:0];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)1, @"Render items must be 1.");
}

- (void)testAdd_MaxBeforeFirst
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    renderCache.delegate = self;
    NSMutableIndexSet *indexSet = [[NSMutableIndexSet alloc] init];
    for (int i = 0; i < 10; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i + 10];
        [indexSet addIndex:i + 10];
    }
    self.currentIndex = 5;
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:5];
    [indexSet addIndex:5];
    [indexSet removeIndex:19];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)10, @"Render items must be 10.");
    [indexSet enumerateIndexesUsingBlock:^(NSUInteger index, BOOL *stop){
        RenderCacheItem *item = [renderCache itemWithIndex:index];
        GHAssertNotNil(item, @"Item with index %d must exist.", index);
    }];
}

- (void)testAdd_MaxAfterLast
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    renderCache.delegate = self;
    NSMutableIndexSet *indexSet = [[NSMutableIndexSet alloc] init];
    for (int i = 0; i < 10; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i + 10];
        [indexSet addIndex:i + 10];
    }
    self.currentIndex = 25;
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:25];
    [indexSet addIndex:25];
    [indexSet removeIndex:10];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)10, @"Render items must be 10.");
    [indexSet enumerateIndexesUsingBlock:^(NSUInteger index, BOOL *stop){
        RenderCacheItem *item = [renderCache itemWithIndex:index];
        GHAssertNotNil(item, @"Item with index %d must exist.", index);
    }];
}

- (void)testAdd_MaxNearFirst
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    renderCache.delegate = self;
    NSMutableIndexSet *indexSet = [[NSMutableIndexSet alloc] init];
    for (int i = 0; i < 5; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
        [indexSet addIndex:i];
    }
    for (int i = 15; i < 20; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
        [indexSet addIndex:i];
    }
    self.currentIndex = 6;
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:6];
    [indexSet addIndex:6];
    [indexSet removeIndex:19];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)10, @"Render items must be 10.");
    [indexSet enumerateIndexesUsingBlock:^(NSUInteger index, BOOL *stop){
        RenderCacheItem *item = [renderCache itemWithIndex:index];
        GHAssertNotNil(item, @"Item with index %d must exist.", index);
    }];
}

- (void)testAdd_MaxNearLast
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    renderCache.delegate = self;
    NSMutableIndexSet *indexSet = [[NSMutableIndexSet alloc] init];
    for (int i = 0; i < 5; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
        [indexSet addIndex:i];
    }
    for (int i = 15; i < 20; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
        [indexSet addIndex:i];
    }
    self.currentIndex = 14;
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:14];
    [indexSet addIndex:14];
    [indexSet removeIndex:0];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)10, @"Render items must be 10.");
    [indexSet enumerateIndexesUsingBlock:^(NSUInteger index, BOOL *stop){
        RenderCacheItem *item = [renderCache itemWithIndex:index];
        GHAssertNotNil(item, @"Item with index %d must exist.", index);
    }];
}

- (void)testAdd_NoMax
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] init];
    renderCache.delegate = self;
    for (int i = 0; i < 11; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
    }
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)11, @"Render items must be 11");
}

- (void)testMemoryWarning
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:0];
    [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:1];
    [[NSNotificationCenter defaultCenter] postNotificationName:UIApplicationDidReceiveMemoryWarningNotification object:nil];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)1, @"Render items count must be 1.");
}

- (void)testItemWithIndex
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    NSMutableArray *items = [[NSMutableArray alloc] init];
    for (int i = 0; i < 5; i++)
    {
        RenderCacheItem *item = [[RenderCacheItem alloc] init];
        item.viewController = nil;
        item.image = nil;
        [items addObject:item];
        [renderCache addItem:item withIndex:i];
    }
    
    // Verification
    for (int i = 0; i < 5; i++)
    {
        RenderCacheItem *item = [renderCache itemWithIndex:i];
        GHAssertEqualObjects(item, [items objectAtIndex:i], @"Items must match.");
    }
}

- (void)testRemoveAllItems
{
    // SUT
    RenderCache *renderCache = [[RenderCache alloc] initWithMaxItemCount:10];
    for (int i = 0; i < 11; i++)
    {
        [renderCache addItem:[[RenderCacheItem alloc] init] withIndex:i];
    }
    [renderCache removeAllItems];
    
    // Verification
    GHAssertEquals([renderCache.itemsDictionary count], (NSUInteger)0, @"Render items count must be 0.");
}

@end

/*
@interface RenderCache(Test)
@property (nonatomic) NSInteger maxItemCount;
@property (nonatomic, strong) NSMutableIndexSet *indexSet;
@property (nonatomic, strong) NSMutableDictionary *itemsDictionary;

- (void)removeItemAtIndex:(NSUInteger)index;
@end


@interface RenderCacheTest:GHTestCase <RenderCacheDelegate>

@property RenderCache *renderCache;

@end

@implementation RenderCacheTest
{
    NSInteger testMaxCount;
    NSUInteger index;
}
-(void) setUpClass
{
    testMaxCount = 10;
    self.renderCache = [[RenderCache alloc] initWithMaxItemCount:testMaxCount];
    self.renderCache.delegate = self;
    index = 0;
}

-(void)setUp
{
    index = 0;
}

 -(void)tearDown
{
    [self.renderCache removeAllItems];
}

-(void)test001_renderCacheInit
{
    GHAssertNotNil(self.renderCache, @"render cache init");
    GHAssertEquals(self.renderCache.maxItemCount, testMaxCount,@"");
    GHAssertNotNil(self.renderCache.indexSet, @"");
    GHAssertNotNil(self.renderCache.itemsDictionary, @"");
}

-(void)test002_addItem
{
    RenderCacheItem *initialCacheItem = [[RenderCacheItem alloc] init];

    index = 0;
    //add new item
    [self.renderCache addItem:initialCacheItem withIndex:index];
    GHAssertEquals((int)self.renderCache.indexSet.count, 1, @"");
    GHAssertEquals(self.renderCache.itemsDictionary.count, self.renderCache.indexSet.count, @"");
    GHAssertEqualObjects([self.renderCache itemWithIndex:index], initialCacheItem, @"");
    
    //replace initial item
    RenderCacheItem *firstCacheItem = [[RenderCacheItem alloc] init];
    [self.renderCache addItem:firstCacheItem withIndex:index];
    GHAssertEquals((int)self.renderCache.indexSet.count, 1, @"");
    GHAssertEquals(self.renderCache.itemsDictionary.count, self.renderCache.indexSet.count, @"");
     GHAssertNotEqualObjects([self.renderCache itemWithIndex:index], initialCacheItem, @"");
    GHAssertEqualObjects([self.renderCache itemWithIndex:index], firstCacheItem, @"");
    
    index++;
    RenderCacheItem *secondCacheItem = [[RenderCacheItem alloc] init];
    [self.renderCache addItem:secondCacheItem withIndex:1];
    
    //add many items until max count.
    index++;
    for(int i = index; i < testMaxCount; index+=2, i++)
    {
        RenderCacheItem *cacheItem = [[RenderCacheItem alloc] init];
        [self.renderCache addItem:cacheItem withIndex:index];
        GHAssertEquals((int)self.renderCache.indexSet.count, i+1, @"");
        GHAssertEquals(self.renderCache.itemsDictionary.count, self.renderCache.indexSet.count, @"");
        GHAssertEqualObjects([self.renderCache itemWithIndex:index], cacheItem, @"");
    }
    
    //the items is equal the maximum count
    GHAssertEquals((NSInteger)self.renderCache.indexSet.count, testMaxCount, @"");
    GHAssertEquals(self.renderCache.itemsDictionary.count, self.renderCache.indexSet.count, @"");
    
    //indexes are now 0,1,2,4,6,8,10,12,14,16,

    //Tests for adding items already in max count
    //Add item that is after last index :item at first index is removed
    RenderCacheItem *lastCacheItem = [[RenderCacheItem alloc] init];
    [self.renderCache addItem:lastCacheItem withIndex:index];
    //check item is succesfully added
    GHAssertEqualObjects([self.renderCache itemWithIndex:index], lastCacheItem, @"");
    //check that first item is removed
    GHAssertNil([self.renderCache itemWithIndex:0], @"");
    //the that second item becomes the first item
    GHAssertEquals((int)[self.renderCache.indexSet firstIndex] , 1, @"");
    GHAssertEqualObjects([self.renderCache itemWithIndex:[self.renderCache.indexSet firstIndex]], secondCacheItem, @"");
    
    //indexes are now 1,2,4,6,8,10,12,14,16,18
    
    //Add item that is before first item. last item is removed
    NSUInteger oldLastIndex = index;
    index = 0;
    [self.renderCache addItem:initialCacheItem withIndex:index];
    //added item becomes first item
    GHAssertEqualObjects([self.renderCache itemWithIndex:[self.renderCache.indexSet firstIndex]], initialCacheItem, @"");
    GHAssertEquals((NSUInteger)[self.renderCache.indexSet firstIndex] , index, @"");
    //previous old item is removed
    GHAssertNil([self.renderCache itemWithIndex:oldLastIndex], @"");
    
    //current last item is not the same as old last item
    RenderCacheItem *currentLastCacheItem = [self.renderCache itemWithIndex:[self.renderCache.indexSet lastIndex]];
    NSUInteger currentLastIndex = [self.renderCache.indexSet lastIndex];
    GHAssertNotEqualObjects(currentLastCacheItem, lastCacheItem, @"");
    GHAssertNotEquals(currentLastIndex , oldLastIndex, @"");
  
    //indexes are now 0,1,2,4,6,8,10,12,14,16
    
    RenderCacheItem *middleCacheItem = [[RenderCacheItem alloc] init];
    //add a middle item that is near the last index: first item is removed
    index = 11;
    [self.renderCache addItem:middleCacheItem withIndex:index];
    //first item is removed
    GHAssertNil([self.renderCache itemWithIndex:0], @"");
    //second item becomes first item
    GHAssertEquals((int)[self.renderCache.indexSet firstIndex] , 1, @"");
    GHAssertEqualObjects([self.renderCache itemWithIndex:[self.renderCache.indexSet firstIndex]], secondCacheItem, @"");
    //previous last item is still last item
     GHAssertEquals((NSUInteger)[self.renderCache.indexSet lastIndex] , currentLastIndex, @"");
    GHAssertEqualObjects(currentLastCacheItem, [self.renderCache itemWithIndex:[self.renderCache.indexSet lastIndex]], @"");
    //the added item is successfully added
    GHAssertNotNil([self.renderCache itemWithIndex:index], @"");
    
    //indexes are now 1,2,4,6,8,10,11,12,14,16,
    
    //add a middle item that is near the first index: last item is removed
    index = 3;
    [self.renderCache addItem:middleCacheItem withIndex:index];
    //previous first item is still the first item
    GHAssertEquals((int)[self.renderCache.indexSet firstIndex] , 1, @"");
    GHAssertEqualObjects([self.renderCache itemWithIndex:[self.renderCache.indexSet firstIndex]], secondCacheItem, @"");
    //old last item is removed
    GHAssertNotEquals((NSUInteger)[self.renderCache.indexSet lastIndex] , currentLastIndex, @"");
    GHAssertNil([self.renderCache itemWithIndex:currentLastIndex], @"");
    GHAssertNotEqualObjects(currentLastCacheItem, [self.renderCache itemWithIndex:[self.renderCache.indexSet lastIndex]], @"");
    //the added item is succesfully added
    GHAssertNotNil([self.renderCache itemWithIndex:index], @"");
    
    //indexes are now 1,2,3,4,6,8,10,11,12,14
}

-(void)test003_removeItemAtIndex
{
    for(NSUInteger i  = 0; i < testMaxCount; i++)
    {
        RenderCacheItem *cacheItem = [[RenderCacheItem alloc] init];
        [self.renderCache addItem:cacheItem withIndex:i];
    }
    
    //remove each added item
    for(NSUInteger i  = 0; i < testMaxCount; i++)
    {
        [self.renderCache removeItemAtIndex:i];
        GHAssertNil([self.renderCache itemWithIndex:i], @"");
        GHAssertFalse([self.renderCache.indexSet containsIndex:i], @"");
    }
    
    //check empty
    GHAssertEquals((int)self.renderCache.indexSet.count, 0, @"");
    GHAssertEquals((int)self.renderCache.itemsDictionary.count, 0, @"");
}


-(NSUInteger)currentIndex
{
    return index;
}
@end*/
#endif