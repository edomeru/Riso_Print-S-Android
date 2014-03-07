//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DatabaseManager.h"

extern BOOL isDummyDataEnabled; //TODO REMOVE! For Debugging Purposes Only

@implementation DatabaseManager

+(NSManagedObjectContext *) getManagedObjectContext
{
    id delegate = [[UIApplication sharedApplication] delegate];
    NSManagedObjectContext* context = [delegate managedObjectContext];
    return context;
}

+ (BOOL)addToDB:(id)entity forEntityName:(NSString*)name;
{
    //TODO
    
    return YES;
}

+(BOOL) saveDB
{
    //TODO REMOVE! For Debugging Purposes Only
    if(isDummyDataEnabled){
        return YES; //Don't execute save if dummy data is enabled
    }
    
    NSError *error = nil;
    if (![[DatabaseManager getManagedObjectContext] save:&error])
    {
        NSLog(@"Error saving to DB %@ %@", error, [error localizedDescription]);
        return NO;
    }
    return YES;
}

+(BOOL) deleteFromDB:(NSManagedObject *) object
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    [context deleteObject:object];
    return[DatabaseManager saveDB];
}

@end
