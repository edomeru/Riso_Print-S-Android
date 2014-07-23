//
//  PrintJobHistoryGroup.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJobHistoryGroup.h"
#import "PrintJob.h"
#import "DatabaseManager.h"

@interface PrintJobHistoryGroup ()

/**
 * Unique identifier for the group which can be used to associate
 * a group to its view.
 */
@property (readwrite, assign, nonatomic) NSInteger tag;

/**
 * Name of the Printer object under which all the PrintJob objects this group
 * holds belong to. This will be displayed in the group's header.
 */
@property (readwrite, strong, nonatomic) NSString* groupName;

/**
 * IP address of the Printer object under which all the PrintJob objects this
 * group holds belong to. This will be displayed in the group's header.
 */
@property (readwrite, strong, nonatomic) NSString* groupIP;

/**
 * Stores the number of PrintJob objects held by this group.
 */
@property (readwrite, assign, nonatomic) NSUInteger countPrintJobs;

/**
 * If YES, this group will be displayed in collapsed form;
 * If NO, this group will be displayed as expanded to show the list of print jobs.
 */
@property (readwrite, assign, nonatomic) BOOL isCollapsed;

/** 
 * List of PrintJob objects.
 */
@property (strong, nonatomic) NSMutableArray* listPrintJobs;

/** 
 * Checks if the specified index is valid in {@listPrintJobs}.
 *
 * @param index the list index
 * @return YES if the index is valid, NO otherwise
 */
- (BOOL)isIndexValid:(NSUInteger)index;

@end

@implementation PrintJobHistoryGroup

#pragma mark - Initializer

- (id)initWithName:(NSString*)name withGroupIP:ip withTag:(NSInteger)tag
{
    self = [super init];
    if (self)
    {
        self.tag = tag;
        self.groupName = name;
        self.groupIP = ip;
        self.listPrintJobs = [NSMutableArray array];
        self.countPrintJobs = 0;
        self.isCollapsed = NO;
    }
    return self;
}

+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name withGroupIP:(NSString*)ip withGroupTag:(NSInteger)tag
{
    return [[self alloc] initWithName:name withGroupIP:ip withTag:tag];
}

#pragma mark - Add

- (void)addPrintJob:(PrintJob*)printJob
{
    [self.listPrintJobs addObject:printJob];
    self.countPrintJobs++;
}

#pragma mark - Delete

- (BOOL)removePrintJobAtIndex:(NSUInteger)index
{
    if (![self isIndexValid:index])
        return NO;
    
    // if the PrintJob object is not anymore held by this group,
    // then the user chose to not remove it from display, so it
    // should also be removed from DB
    if (![DatabaseManager deleteObject:[self.listPrintJobs objectAtIndex:index]])
    {
#if DEBUG_LOG_PRINT_JOB_GROUP_MODEL
        NSLog(@"[ERROR][PrintJobGroup] could not delete PrintJob at index=%lu", (unsigned long)index);
#endif
        return NO;
    }
    
    [self.listPrintJobs removeObjectAtIndex:index];
    self.countPrintJobs--;
    
    return YES;
}

#pragma mark - Get

- (PrintJob*)getPrintJobAtIndex:(NSUInteger)index
{
    if (![self isIndexValid:index])
        return nil;
    
    return [self.listPrintJobs objectAtIndex:index];
}

#pragma mark - Collapse/Expand

- (void)collapse:(BOOL)isCollapsed
{
    self.isCollapsed = isCollapsed;
}

#pragma mark - Sort

- (void)sortPrintJobs
{
    [self.listPrintJobs sortUsingComparator:^NSComparisonResult(PrintJob* job1, PrintJob* job2)
    {
        NSComparisonResult result = [job1.date compare:job2.date];
        
        //// sort by oldest first (default behavior)
        //return result;
        
        // sort by most recent first (reverse behavior)
        if (result == NSOrderedAscending)
            return NSOrderedDescending;
        else if (result == NSOrderedDescending)
            return NSOrderedAscending;
        else
            return NSOrderedSame;
    }];
}

#pragma mark - Utilities

- (BOOL)isIndexValid:(NSUInteger)index
{
    if (index >= self.countPrintJobs)
    {
#if DEBUG_LOG_PRINT_JOB_GROUP_MODEL
        NSLog(@"[ERROR][PrintJobGroup] index=%lu >= count=%lu",
              (unsigned long)index, (unsigned long)self.countPrintJobs);
#endif
        return NO;
    }
    
    return YES;
}

@end
