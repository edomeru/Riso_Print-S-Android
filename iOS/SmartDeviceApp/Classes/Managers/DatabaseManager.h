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
 Adds a NSManagedObject to the database.
 This can be used for adding the following objects to
 the DB: "Printer", "PrintJob", and "DefaultPrinter".
 
 @param entity
        The object to be added.
 @param name
        The entity name as specified in the Core Data model.
 
 @return YES if successful, NO otherwise.
 **/
+ (BOOL)addToDB:(id)entity forEntityName:(NSString*)name;

/**
 Returns the NSManagedObjectContext.
 This is the context used for DB transactions.
 
 @return NSManagedObjectContext*
 **/

+(NSManagedObjectContext *) getManagedObjectContext;


/**
 Call to the DB to save the changes done to the managed objects

 **/
+(void) saveDB;
@end
