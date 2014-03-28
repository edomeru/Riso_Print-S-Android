//
//  PrintJobHistoryGroup.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryGroup.h"

@interface PrintJobHistoryGroup ()

@property (readwrite, strong, nonatomic) NSString* groupName;
@property (readwrite, strong, nonatomic) NSMutableArray* listPrintJobs;
@property (readwrite, assign, nonatomic) NSUInteger countPrintJobs;
@property (readwrite, assign, nonatomic) BOOL isCollapsed;

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

- (void)addPrintJob:(NSString*)printJob
{
    [self.listPrintJobs addObject:printJob];
    self.countPrintJobs++;
}

#pragma mark - Delete

- (void)deletePrintJobAtIndex:(NSUInteger)index
{
    [self.listPrintJobs removeObjectAtIndex:index];
    self.countPrintJobs--;
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
