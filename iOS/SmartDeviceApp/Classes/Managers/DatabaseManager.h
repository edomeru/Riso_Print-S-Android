//
//  DatabaseManager.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

#define E_PRINTER           @"Printer"
#define E_PRINTSETTING      @"PrintSetting"
#define E_PRINTJOB          @"PrintJob"
#define E_DEFAULTPRINTER    @"DefaultPrinter"

@interface DatabaseManager : NSObject

/**
 Retrieves objects of the specified entity from the database.
 @param entityName
        one of the following entities:
        E_PRINTER, E_PRINTSETTING,
        E_PRINTJOB, E_DEFAULTPRINTER
 @return NSArray* of the matching results, nil otherwise.
 */
+ (NSArray*)getObjects:(NSString*)entityName;

/**
 Inserts a new object of the specified entity into the database.
 This method does not save the NSManagedObjectContext as this new
 object may only be temporarily needed. A succeeding call to 
 saveChanges: must be made after this to make the inserted object
 permanent.
 @param entityName
        one of the following: 
        E_PRINTER, E_PRINTSETTING,
        E_PRINTJOB, E_DEFAULTPRINTER
 @return the inserted object if successful, nil otherwise.
 */
+ (NSManagedObject*)addObject:(NSString*)entityName;

/**
 Removes the specified object from the database.
 This method saves the NSManagedObjectContext, 
 making the deletion permanent.
 @param object
        the NSManagedObject to be removed
 @return YES if successful, NO otherwise
 */
+ (BOOL)deleteObject:(NSManagedObject*)object;

/**
 Saves all the changes made to the database.
 This includes all previous insertions and deletions.
 @return YES if successful, NO otherwise.
 */
+ (BOOL)saveChanges;

/**
 Discards all the changes made to the database.
 This includes all previous insertions and deletions.
 @return YES if successful, NO otherwise.
 */
+ (void)discardChanges;

@end
