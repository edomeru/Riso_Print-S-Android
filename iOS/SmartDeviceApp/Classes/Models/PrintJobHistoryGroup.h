//
//  PrintJobHistoryGroup.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PrintJobHistoryGroup : NSObject

#pragma mark - Properties

/**
 Name of the Printer object under which all the PrintJob objects belong to.
 This will be displayed in the group header.
 */
@property (readonly, strong, nonatomic) NSString* groupName;

/**
 Stores the number of PrintJob objects.
 */
@property (readonly, assign, nonatomic) NSUInteger countPrintJobs;

/**
 If YES, this print job group will be displayed in collapsed form.
 If NO, the print job group is expanded to display the list of
 print jobs.
 */
@property (readonly, assign, nonatomic) BOOL isCollapsed;

#pragma mark - Methods

/**
 Class constructor.
 The group is set to use a specified name and is initially
 marked as expanded.
 @param name
        name that will be displayed in the group header
 */
+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name;

/**
 Adds a PrintJob object to the list of print jobs.
 */
//TODO: this should add a PrintJob object, not just a NSString
- (void)addPrintJob:(NSString*)printJob;

/**
 Removes a PrintJob object from the list of print jobs.
 @param index
 */
- (void)deletePrintJobAtIndex:(NSUInteger)index;

/**
 Retrieves a PrintJob object from the list of print jobs.
 @param index
 */
//TODO: this should return a PrintJob object, not just a NSString
- (NSString*)getPrintJobAtIndex:(NSUInteger)index;

/**
 Sets whether the group is displayed as collapsed or expanded.
 @param isCollapsed
        YES if collapsed, NO for expanded
 */
- (void)collapse:(BOOL)isCollapsed;

/**
 Sorts the PrintJob objects according to their timestamps,
 with the most recent items placed at the start of the list.
 */
- (void)sortPrintJobs;

@end
