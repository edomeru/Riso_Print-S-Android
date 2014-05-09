//
//  PrintJobHistoryGroup.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PrintJob;

@interface PrintJobHistoryGroup : NSObject

#pragma mark - Properties

/**
 Unique identifier for the group, that can be used to associate
 a group to its view.
 */
@property (readonly, assign, nonatomic) NSInteger tag;

/**
 Name of the Printer object under which all the PrintJob objects this group
 holds belong to. This will be displayed in the group header.
 */
@property (readonly, strong, nonatomic) NSString* groupName;

/**
 IP address of the Printer object under which all the PrintJob objects this 
 group holds belong to. This will be displayed in the group header.
 */
@property (readonly, strong, nonatomic) NSString* groupIP;

/**
 Stores the number of PrintJob objects held by this group.
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
 @param ip
        IP address that will be displayed in the group header
 @param tag
        unique, non-changeable tag to identify the group
 */
+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name withGroupIP:(NSString*)ip withGroupTag:(NSInteger)tag;

/**
 Adds a PrintJob object to the list of print jobs held by
 this group. The PrintJob object should already have been
 added to the DB beforehand.
 @param printJob
        a valid PrintJob object
 */
- (void)addPrintJob:(PrintJob*)printJob;

/**
 Removes a PrintJob object from the list of print jobs held by
 this group. The print job is also removed from the database.
 @param index
        if this index is invalid, this method simply returns
 @return YES if successful, NO otherwise
 */
- (BOOL)removePrintJobAtIndex:(NSUInteger)index;

/**
 Retrieves the PrintJob object from the list of print jobs
 held by this group, specified by the index.
 @param index
        if this index is invalid, this method simply returns
 @return the PrintJob object if successful, nil otherwise
 */
- (PrintJob*)getPrintJobAtIndex:(NSUInteger)index;

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