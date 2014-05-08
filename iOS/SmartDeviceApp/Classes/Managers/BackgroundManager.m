//
//  BackgroundManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "BackgroundManager.h"

@interface BackgroundManager()

@property(nonatomic, strong) NSMutableArray *objects;

@end

@implementation BackgroundManager

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        _objects = [[NSMutableArray alloc] init];
    }
    return self;
}

+ (BackgroundManager *)sharedManager
{
    static BackgroundManager *_sharedManager = nil;
    
    static dispatch_once_t token;
    
    dispatch_once(&token, ^{
        _sharedManager = [[BackgroundManager alloc] init];
    });
    
    return _sharedManager;
}

- (void)addCancellableObject:(id<BackgroundManagerCancellable>)object
{
    [self.objects addObject:object];
}

- (void)removeCancellableObject:(id<BackgroundManagerCancellable>)object
{
    NSUInteger index = [self.objects indexOfObject:object];
    if (index != NSNotFound)
    {
        [self.objects removeObjectAtIndex:index];
    }
}

- (void)cancelAll
{
    NSMutableIndexSet *indexSet = [[NSMutableIndexSet alloc] init];
    
    NSUInteger index = 0;
    for (id<BackgroundManagerCancellable>object in self.objects)
    {
        [object cancelToBackground];
        
        if ([object shouldResumeOnEnterForeground] == NO)
        {
            [indexSet addIndex:index];
        }
        
        index++;
    }
    
    [self.objects removeObjectsAtIndexes:indexSet];
}

- (void)resumeAll
{
    for (id<BackgroundManagerCancellable>object in self.objects)
    {
        [object resumeFromBackground];
    }
}

@end
