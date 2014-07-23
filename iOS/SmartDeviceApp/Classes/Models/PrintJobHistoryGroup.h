//
//  PrintJobHistoryGroup.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PrintJob;

/**
 * Model for a group of print jobs belonging to one printer.
 * This is used for the "Print Job History" screen where the print job
 * items are grouped into printers.\n
 * Each print job group is identified by:
 *  - a unique {@link tag}
 *  - the printer name
 *  - the printer's IP address
 *
 * It acts as the interface to the PrintJob objects when they are displayed and
 * deleted in the "Print Job History" screen.
 * In addition, the displayed state of a print job group is also managed by this model.
 */
@interface PrintJobHistoryGroup : NSObject

#pragma mark - Properties

/**
 * Unique identifier for the group which can be used to associate
 * a group to its view.
 */
@property (readonly, assign, nonatomic) NSInteger tag;

/**
 * Name of the Printer object under which all the PrintJob objects this group
 * holds belong to. This will be displayed in the group's header.
 */
@property (readonly, strong, nonatomic) NSString* groupName;

/**
 * IP address of the Printer object under which all the PrintJob objects this
 * group holds belong to. This will be displayed in the group's header.
 */
@property (readonly, strong, nonatomic) NSString* groupIP;

/**
 * Stores the number of PrintJob objects held by this group.
 */
@property (readonly, assign, nonatomic) NSUInteger countPrintJobs;

/**
 * If YES, this group will be displayed in collapsed form;
 * If NO, this group will be displayed as expanded to show the list of print jobs.
 */
@property (readonly, assign, nonatomic) BOOL isCollapsed;

#pragma mark - Methods

/**
 * Returns an initialized instance of a PrintJobHistoryGroup.
 * The group has initially no PrintJob objects and is marked as expanded.
 *
 * @param name printer name that will be displayed in the group's header
 * @param ip printer IP address that will be displayed in the group's header
 * @param tag unique and immutable tag to identify the group
 */
+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name withGroupIP:(NSString*)ip withGroupTag:(NSInteger)tag;

/**
 * Adds a PrintJob object to this group.
 * The PrintJob object should already have been added to the database beforehand.
 *
 * @param printJob a valid PrintJob object
 */
- (void)addPrintJob:(PrintJob*)printJob;

/**
 * Removes a PrintJob object this group. 
 * The PrintJob object is also removed from the database.
 *
 * @param index list index of the print job to remove
 * @return YES if successful, NO otherwise
 */
- (BOOL)removePrintJobAtIndex:(NSUInteger)index;

/**
 * Retrieves the PrintJob object belonging to this group.
 *
 * @param index list index of the print job to retrieve
 * @return the PrintJob object if successful, nil otherwise
 */
- (PrintJob*)getPrintJobAtIndex:(NSUInteger)index;

/**
 * Sets whether the group is to be displayed as collapsed or expanded.
 *
 * @param isCollapsed YES if collapsed, NO for expanded
 */
- (void)collapse:(BOOL)isCollapsed;

/**
 * Sorts the PrintJob objects according to their timestamps,
 * with the most recent items placed at the start of the list.
 */
- (void)sortPrintJobs;

@end
