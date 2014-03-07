//
//  DatabaseManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DatabaseManager.h"

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

+(void) saveDB
{
    NSError *error = nil;
    if (![[DatabaseManager getManagedObjectContext] save:&error])
    {
        NSLog(@"Error saving to DB %@ %@", error, [error localizedDescription]);
        return;
    }
}

@end
