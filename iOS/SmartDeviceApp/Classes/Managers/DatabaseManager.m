//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DatabaseManager.h"

static NSManagedObjectContext* sharedManagedObjectContext = nil;
static NSPersistentStoreCoordinator* sharedPersistentStoreCoordinator = nil;
static NSManagedObjectModel* sharedManagedObjectModel = nil;

@interface DatabaseManager ()

/**
 * Returns the managed object context for the application.
 * If the context doesn't exist yet, it is first created and bound to the
 * persistent store coordinator for the application.\n
 * The purpose of this method is to have a single instance of the managed
 * object context that will be used by each of the {@link DatabaseManager}'s
 * methods, for the entire lifecycle of the application.
 */
+ (NSManagedObjectContext*)managedObjectContext;

/**
 * Returns the persistent store coordinator for the application.
 * If the coordinator doesn't exist yet, it is first created and the 
 * application's persistent object store is added to it.\n
 * The purpose of this method is to have single instance of the persistent
 * store coordinator that will be used for the entire lifecylce of the
 * application.\n
 * In the event that the persistent store cannot be added to the coordinator
 * (ex. incompatible schema), the existing store is first deleted and a new,
 * one is created (no data migration is performed).
 */
+ (NSPersistentStoreCoordinator*)persistentStoreCoordinator;

/**
 * Returns the managed object model for the application.
 * If the model doesn't exist yet, it is first created from the application's 
 * Core Data model file (*.xcdatamodeld).\n
 * The purpose of this method is to have a single instance of the model
 * that will be used for the entire lifecycle of the application.
 */
+ (NSManagedObjectModel*)managedObjectModel;

/**
 * Returns the URL to the application's Documents directory.
 */
+ (NSURL*)applicationDocumentsDirectory;

@end

@implementation DatabaseManager

#pragma mark - Fetch

+ (NSArray*)getObjects:(NSString*)entityName;
{
    return [self getObjects:entityName usingFilter:nil];
}

+ (NSArray*)getObjects:(NSString*)entityName usingFilter:(NSString*)filter
{
    if (sharedManagedObjectContext == nil)
        sharedManagedObjectContext = [self managedObjectContext];
    
    NSFetchRequest* fetchRequest = [[NSFetchRequest alloc] initWithEntityName:entityName];
    NSPredicate* predicate = nil;
    if (filter != nil)
    {
        predicate = [NSPredicate predicateWithFormat:filter];
        [fetchRequest setPredicate:predicate];
    }
    
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
        NSLog(@"[ERROR][DBM] error creating database, possibly incompatible schema");
#endif
        
        // delete the existing store
        [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil];
        
        // create the database again
        // TODO: should migrate the DB instead (if possible)
        [sharedPersistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType
                                                       configuration:nil
                                                                 URL:storeURL
                                                             options:nil
                                                               error:&error];
    }
    
    return sharedPersistentStoreCoordinator;
}

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

+ (NSURL*)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                                   inDomains:NSUserDomainMask] lastObject];
}

@end
