//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DatabaseManager.h"

static NSManagedObjectContext* sharedManagedObjectContext = nil;
static NSPersistentStoreCoordinator* sharedPersistentStoreCoordinator = nil;
static NSManagedObjectModel* sharedManagedObjectModel = nil;

@interface DatabaseManager ()

@end

@implementation DatabaseManager

#pragma mark - Fetch

+ (NSArray*)getObjects:(NSString*)entityName;
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    NSFetchRequest* fetchRequest = [[NSFetchRequest alloc] initWithEntityName:entityName];
    NSError *error;
    
    NSArray* results = [sharedManagedObjectContext executeFetchRequest:fetchRequest error:&error];

#if DEBUG_LOG_DATABASE_MANAGER
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
#endif
    
    return results;
}

#pragma mark - Add

+ (NSManagedObject*)addObject:(NSString*)entityName
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    return [NSEntityDescription insertNewObjectForEntityForName:entityName
                                         inManagedObjectContext:sharedManagedObjectContext];
}

#pragma mark - Delete

+ (BOOL)deleteObject:(NSManagedObject*)object
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    [sharedManagedObjectContext deleteObject:object];
    
    return [self saveChanges];
}

#pragma mark - Save Changes

+ (BOOL)saveChanges
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    NSError *error = nil;
    if (![sharedManagedObjectContext save:&error])
    {
#if DEBUG_LOG_DATABASE_MANAGER
        NSLog(@"[ERROR][DBM] save failed, (%@)", [error debugDescription]);
#endif
        return NO;
    }
    
    return YES;
}

#pragma mark - Discard Changes

+ (void)discardChanges
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    [sharedManagedObjectContext rollback];
}

#pragma mark - Core Data Stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound
 to the persistent store coordinator for the application.
 **/
+ (NSManagedObjectContext*)managedObjectContext
{
    if (sharedManagedObjectContext != nil)
    {
        return sharedManagedObjectContext;
    }
    
    NSPersistentStoreCoordinator* coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil)
    {
        sharedManagedObjectContext = [[NSManagedObjectContext alloc] init];
        [sharedManagedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    
    return sharedManagedObjectContext;
}

/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
+ (NSPersistentStoreCoordinator*)persistentStoreCoordinator
{
    if (sharedPersistentStoreCoordinator != nil)
    {
        return sharedPersistentStoreCoordinator;
    }
    
    NSURL* storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"SmartDeviceApp.sqlite"];
    
    NSError* error = nil;
    sharedPersistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc]
                                        initWithManagedObjectModel:[self managedObjectModel]];
    if (![sharedPersistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType
                                                        configuration:nil
                                                                  URL:storeURL
                                                              options:nil
                                                                error:&error])
    {
#if DEBUG_LOG_DATABASE_MANAGER
        NSLog(@"[ERROR][DBM] unresolved error %@, %@", error, [error userInfo]);
#endif
        abort();
    }
    
    return sharedPersistentStoreCoordinator;
}

/**
 Returns the managed object model for the application.
 If the model doesn't already exist, it is created from the application's model.
 */
+ (NSManagedObjectModel*)managedObjectModel
{
    if (sharedManagedObjectModel != nil)
    {
        return sharedManagedObjectModel;
    }
    
    NSURL* modelURL = [[NSBundle mainBundle] URLForResource:@"SmartDeviceApp" withExtension:@"momd"];
    sharedManagedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return sharedManagedObjectModel;
}

#pragma mark - Application's Documents Directory

/** Returns the URL to the application's Documents directory. **/
+ (NSURL*)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                                   inDomains:NSUserDomainMask] lastObject];
}

@end
