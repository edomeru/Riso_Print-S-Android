//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DatabaseManager.h"

static NSManagedObjectContext* sharedManagedObjectContext = nil;

@interface DatabaseManager ()

/**
 Sets the shared NSManagedObjectContext object for use by DatabaseManager.
 This method should be called only once during the lifecycle of this class.
 */
+ (void)setSharedManagedObjectContext;

@end

@implementation DatabaseManager

#pragma mark - Context

+ (void)setSharedManagedObjectContext
{
    // get from AppDelegate
    id delegate = [[UIApplication sharedApplication] delegate];
    if ([delegate respondsToSelector:@selector(managedObjectContext)])
    {
        sharedManagedObjectContext = [delegate managedObjectContext];
    }
    
    // check if NSManagedObjectContext was successfully set
    if (sharedManagedObjectContext == nil)
    {
        NSLog(@"[ERROR][DBM] could not set NSManagedObjectContext");
        
        //TODO: to prevent possible crashes, set sharedManagedObjectContext to ??
    }
}

#pragma mark - Fetch

+ (NSArray*)getObjects:(NSString*)entityName;
{
    if (sharedManagedObjectContext == nil)
        [self setSharedManagedObjectContext];
    
    NSFetchRequest* fetchRequest = [[NSFetchRequest alloc] initWithEntityName:entityName];
    NSError *error;
    
    NSArray* results = [sharedManagedObjectContext executeFetchRequest:fetchRequest error:&error];
    if (results == nil)
    {
        NSLog(@"[ERROR][DBM] fetch error, (%@)", [error debugDescription]);
    }
    else if ([results count] == 0)
    {
        NSLog(@"[INFO][DBM] fetch for %@ returned empty", entityName);
    }
    else
    {
        NSLog(@"[INFO][DBM] fetch returned %u results", [results count]);
    }
    
    return results;
}

#pragma mark - Add

+ (NSManagedObject*)addObject:(NSString*)entityName
{
    if (sharedManagedObjectContext == nil)
        [self setSharedManagedObjectContext];
    
    return [NSEntityDescription insertNewObjectForEntityForName:entityName
                                         inManagedObjectContext:sharedManagedObjectContext];
}

#pragma mark - Delete

+ (BOOL)deleteObject:(NSManagedObject*)object
{
    if (sharedManagedObjectContext == nil)
        [self setSharedManagedObjectContext];
    
    [sharedManagedObjectContext deleteObject:object];
    
    return [self saveChanges];
}

#pragma mark - Save Changes

+ (BOOL)saveChanges
{
    if (sharedManagedObjectContext == nil)
        [self setSharedManagedObjectContext];
    
    NSError *error = nil;
    if (![sharedManagedObjectContext save:&error])
    {
        NSLog(@"[ERROR][DBM] save failed, (%@)", [error debugDescription]);
        return NO;
    }
    
    return YES;
}

#pragma mark - Discard Changes

+ (void)discardChanges
{
    if (sharedManagedObjectContext == nil)
        [self setSharedManagedObjectContext];
    
    [sharedManagedObjectContext rollback];
}

@end
