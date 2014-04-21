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

@property (readwrite, assign, nonatomic) NSInteger tag;
@property (readwrite, strong, nonatomic) NSString* groupName;
@property (readwrite, assign, nonatomic) NSUInteger countPrintJobs;
@property (readwrite, assign, nonatomic) BOOL isCollapsed;

/** 
 Container for the list of PrintJob objects. 
 This container should be abstracted from the view and the controller,
 and all operations/handling on it should be done inside this class.
 */
@property (strong, nonatomic) NSMutableArray* listPrintJobs;

/** Checks if the specified index will not cause an out-of-bounds access. */
- (BOOL)isIndexValid:(NSUInteger)index;

@end

@implementation PrintJobHistoryGroup

#pragma mark - Initializer

- (id)initWithName:(NSString*)name withTag:(NSInteger)tag
{
    self = [super init];
    if (self)
    {
        self.tag = tag;
        self.groupName = name;
        self.listPrintJobs = [NSMutableArray array];
        self.countPrintJobs = 0;
        self.isCollapsed = NO;
    }
    return self;
}

+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name withGroupTag:(NSInteger)tag
{
    return [[self alloc] initWithName:name withTag:tag];
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
