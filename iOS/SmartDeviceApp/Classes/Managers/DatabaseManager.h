//
//  DatabaseManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DatabaseManager : NSObject

/**
 Retrieves the pre-defined NSManagedObjectContext from the AppDelegate.
 
 @return NSManagedObjectContext*
 **/
+ (NSManagedObjectContext*)getManagedObjectContext;

/**
 Retrieves objects from the database.
 @param entityName
        name as specified in the Core Data model
 @return NSArray* of the matching results, nil otherwise.
 **/
+ (NSArray*)getObjects:(NSString*)entityName;

/**
 Inserts a new object into the database
 A call to saveChanges: must be made after this to
 make the inserted object permanent.
 @param entityName
        name as specified in the Core Data model
 @return the inserted object if successful, nil otherwise.
 **/
+ (NSManagedObject*)addObject:(NSString*)entityName;

/**
 Removes an object from the database.
 @param object
        the NSManagedObject to be removed
 @return YES if successful, NO otherwise
 **/
+ (BOOL)deleteObject:(NSManagedObject*)object;

/**
 Saves all the changes made to the database.
 This includes all previous insertions and deletions.

 @return YES if successful, NO otherwise.
 **/
+ (BOOL)saveChanges;

/**
 Discards all the changes made to the database.
 This includes all previous insertions and deletions.
 
 @return YES if successful, NO otherwise.
 **/
+ (void)discardChanges;

@end
