//
//  PrintJobHistoryGroup.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryGroup.h"
#import "PrintJob.h"
#import "DatabaseManager.h"

@interface PrintJobHistoryGroup ()

@property (readwrite, strong, nonatomic) NSString* groupName;
@property (readwrite, assign, nonatomic) NSUInteger countPrintJobs;
@property (readwrite, assign, nonatomic) BOOL isCollapsed;

/** 
 Container for the list of PrintJob objects. 
 This container should be abstracted from the view and the controller,
 and all operations/handling on it should be done inside this class.
 */
@property (strong, nonatomic) NSMutableArray* listPrintJobs;

@end

@implementation PrintJobHistoryGroup

#pragma mark - Initializer

- (id)initWithName:(NSString*)name
{
    self = [super init];
    if (self)
    {
        self.groupName = name;
        self.listPrintJobs = [NSMutableArray array];
        self.countPrintJobs = 0;
        self.isCollapsed = NO;
    }
    return self;
}

+ (PrintJobHistoryGroup*)initWithGroupName:(NSString*)name
{
    return [[self alloc] initWithName:name];
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
    if (index >= self.countPrintJobs)
    {
        NSLog(@"[ERROR][PrintJobGroup] index=%lu >= count=%lu",
              (unsigned long)index, (unsigned long)self.countPrintJobs);
        return NO;
    }
    
    // if the PrintJob object is not anymore held by this group,
    // then the user chose to not remove it from display, so it
    // should also be removed from DB
    if (![DatabaseManager deleteObject:[self.listPrintJobs objectAtIndex:index]])
    {
        NSLog(@"[ERROR][PrintJobGroup] could not delete PrintJob at index=%lu", (unsigned long)index);
        return NO;
    }
    
    [self.listPrintJobs removeObjectAtIndex:index];
    self.countPrintJobs--;
    
    return YES;
}

#pragma mark - Get

- (PrintJob*)getPrintJobAtIndex:(NSUInteger)index
{
    if (index >= self.countPrintJobs)
    {
        NSLog(@"[ERROR][PrintJobGroup] index=%lu >= count=%lu",
              (unsigned long)index, (unsigned long)self.countPrintJobs);
        return nil;
    }
    
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
    //TODO: sort by date, most recent first
}

@end
