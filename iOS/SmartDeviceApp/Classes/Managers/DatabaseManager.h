//
//  DatabaseManager.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

#define E_PRINTER           @"Printer"
#define E_PRINTSETTING      @"PrintSetting"
#define E_PRINTJOB          @"PrintJob"
#define E_DEFAULTPRINTER    @"DefaultPrinter"

/**
 * Handler for all database-related operations.
 * This class provides the interface to the Core Data framework.
 * It contains the methods for adding, deleting, retrieving, and
 * updating database objects, where each object is identified 
 * using its entity name.
 * \n
 * The entity name is the string used in defining the entity in
 * the Core Data model file (*.xcdatamodeld). For convenience,
 * the following constants can be used when the entity name needs
 * to be specified:
 *  - E_PRINTER
 *  - E_PRINTSETTING
 *  - E_PRINTJOB
 *  - E_DEFAULTPRINTER
 *
 * This class is not required to be instantiated to be used, since
 * all it methods are declared as static.
 */
@interface DatabaseManager : NSObject

/**
 * Retrieves objects of the specified entity from the database.
 * For convenience, the following constants can be used as the value
 * for the entityName parameter:
 *  - E_PRINTER
 *  - E_PRINTSETTING
 *  - E_PRINTJOB
 *  - E_DEFAULTPRINTER
 *
 * @param entityName the string name as defined in the Core Data model file
 * @return an array containing any matching results (can be empty), nil if an error occurs
 */
+ (NSArray*)getObjects:(NSString*)entityName;

/**
 * Retrieves objects of the specified entity from the database.
 * Compared to the {@link getObjects:} method, a filter can be specified for
 * fetching specific entries for the given entity.\n For example, it 
 * is needed to fetch print jobs produced only by a certain printer.
 * \n
 * For convenience, the following constants can be used as the value
 * for the entityName parameter:
 *  - E_PRINTER
 *  - E_PRINTSETTING
 *  - E_PRINTJOB
 *  - E_DEFAULTPRINTER
 *
 * @param entityName the string name as defined in the Core Data model file
 * @param filter string of the format "attribute = 'value'"
                 (example: "printer.ip_address = '192.168.0.1'")
 * @return an array containing any matching results (can be empty), nil if an error occurs
 */
+ (NSArray*)getObjects:(NSString*)entityName usingFilter:(NSString*)filter;

/**
 * Inserts a new object of the specified entity into the database.
 * This method does not save the NSManagedObjectContext as this new
 * object may only be temporarily needed. A succeeding call to the
 * {@link saveChanges} method must be made after this to make the inserted
 * object permanent.
 * \n
 * For convenience, the following constants can be used as the value
 * for the entityName parameter:
 *  - E_PRINTER
 *  - E_PRINTSETTING
 *  - E_PRINTJOB
 *  - E_DEFAULTPRINTER
 *
 * @param entityName the string name as defined in the Core Data model file
 * @return the inserted object if successful, nil if an error occurs
 */
+ (NSManagedObject*)addObject:(NSString*)entityName;

/**
 * Removes the specified object from the database.
 * This method automatically calls the {@link saveChanges} method,
 * which makes the deletion permanent.
 *
 * @param object the NSManagedObject to be removed
 * @return YES if successful, NO if an error occurs
 */
+ (BOOL)deleteObject:(NSManagedObject*)object;

/**
 * Saves all the changes made to the database.
 * This includes all previous insertions made using {@link addObject:}
 * and all modifications made to any the objects retrieved
 * {@link getObjects:} or {@link getObjects:usingFilter:}.
 *
 * @return YES if successful, NO if an error occurs
 */
+ (BOOL)saveChanges;

/**
 * Discards all the changes made to the database.
 * This includes all previous insertions made using {@link addObject:}
 * and all modifications made to any the objects retrieved using 
 * {@link getObjects:} or {@link getObjects:usingFilter:}.
 */
+ (void)discardChanges;

@end
