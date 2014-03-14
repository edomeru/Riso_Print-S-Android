//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DatabaseManager.h"

@implementation DatabaseManager

#pragma mark - Context

+ (NSManagedObjectContext*)getManagedObjectContext
{
    NSManagedObjectContext* context = nil;
    
    id delegate = [[UIApplication sharedApplication] delegate];
    if ([delegate respondsToSelector:@selector(managedObjectContext)])
        context = [delegate managedObjectContext];
    else
        NSLog(@"[ERROR][DBM] could not get NSManagedObjectContext");
    
    return context;
}

#pragma mark - Fetch

+ (NSArray*)getObjects:(NSString*)entityName;
{
    NSManagedObjectContext* context = [self getManagedObjectContext];
    NSFetchRequest* fetchRequest = [[NSFetchRequest alloc] initWithEntityName:entityName];
    NSError *error;
    
    NSArray* results = [context executeFetchRequest:fetchRequest error:&error];
    if (results == nil)
        NSLog(@"[ERROR][DBM] fetch error, (%@)", [error debugDescription]);
    else if ([results count] == 0)
        NSLog(@"[INFO][DBM] fetch for %@ returned empty", entityName);
        
    return results;
}

#pragma mark - Add

+ (NSManagedObject*)addObject:(NSString*)entityName
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName
                                         inManagedObjectContext:[self getManagedObjectContext]];
}

#pragma mark - Delete

+ (BOOL)deleteObject:(NSManagedObject*)object
{
    NSManagedObjectContext* context = [self getManagedObjectContext];
    [context deleteObject:object];
    
    return [self saveChanges];
}

#pragma mark - Save Changes

+ (BOOL)saveChanges
{
    NSError *error = nil;
    if (![[self getManagedObjectContext] save:&error])
    {
        NSLog(@"[ERROR][DBM] save failed, (%@)", [error debugDescription]);
        return NO;
    }
    
    return YES;
}

#pragma mark - Discard Changes

+ (void)discardChanges
{
    [[self getManagedObjectContext] rollback];
}

@end
