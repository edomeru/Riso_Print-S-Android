//
//  PrintJobHistoryManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryManager.h"
#import "PrinterManager.h"
#import "DatabaseManager.h"
#import "Printer.h"
#import "PrinterDetails.h"
#import "PrintJob.h"
#import "PrintJob+Log.h"
#import "PrintJobHistoryGroup.h"
#import "PListHelper.h"

@interface PrintJobHistoryManager ()

/**
 Fills the database with PrintJob objects.
 It deletes all existing Printers from the database, then
 adds some pre-defined number of PrintJob objects, assigns
 them each to a printer, then saves the database.
 This is to be used only for debugging.
 */
+ (void)populateWithTestData;

@end

@implementation PrintJobHistoryManager

+ (NSMutableArray*)retrievePrintJobHistoryGroups
{
    NSMutableArray* listPrintJobHistoryGroups = [NSMutableArray array];
    
    // retrieve from DB
    NSArray* listPrintJobs = [DatabaseManager getObjects:E_PRINTJOB];
    if (listPrintJobs != nil) // DB access did not fail
    {
        NSUInteger countPrintJobs = [listPrintJobs count];
        NSLog(@"[INFO][PrintJobManager] listPrintJobs=%lu", (unsigned long)countPrintJobs);
        
        if (countPrintJobs == 0)
        {
            // DB has no data

            // check if will use test data
            BOOL usePrintJobTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
            NSLog(@"[INFO][PrintJobManager] usePrintJobTestData=%@", (usePrintJobTestData ? @"YES" : @"NO"));
            if (usePrintJobTestData)
            {
                // populate the DB
                [self populateWithTestData];
                
                // retrieve from DB again
                listPrintJobs = [DatabaseManager getObjects:E_PRINTJOB];
                countPrintJobs = [listPrintJobs count];
                NSLog(@"[INFO][PrintJobManager] listPrintJobs=%lu", (unsigned long)countPrintJobs);
            }
        }
     
        // sort the print jobs according to printer
        NSMutableDictionary* dictPrintJobHistoryGroups = [NSMutableDictionary dictionary];
        for (PrintJob* job in listPrintJobs)
        {
            NSLog(@"[INFO][PrintJobManager] job=%@", job.name);
            
            // get the printer
            NSString* printerName = job.printer.name;
            
            // attempt to get the group for this printer
            PrintJobHistoryGroup* group = [dictPrintJobHistoryGroups objectForKey:printerName];
            if (group == nil)
            {
                // group does not exist yet
                
                // create a group for this printer
                NSLog(@"[INFO][PrintJobManager] create group for printer=%@", printerName);
                PrintJobHistoryGroup* newGroup = [PrintJobHistoryGroup initWithGroupName:printerName];
                [newGroup collapse:NO];
                
                // add the current job as this group's first job
                [newGroup addPrintJob:job];
                
                // add the group to the dictionary
                [dictPrintJobHistoryGroups setObject:newGroup forKey:printerName];
            }
            else
            {
                // group already exists
                
                // add the current job to the group
                NSLog(@"[INFO][PrintJobManager] update group for printer=%@", printerName);
                [group addPrintJob:job];
            }
        }
        
        // sort each group by timestamp
        // add each group to the result list
        [dictPrintJobHistoryGroups enumerateKeysAndObjectsUsingBlock:^(NSString* printerName,
                                                                       PrintJobHistoryGroup* group,
                                                                       BOOL* stop)
        {
            NSLog(@"[INFO][PrintJobManager] sorting jobs for group=%@", printerName);
            [group sortPrintJobs];
            
            [listPrintJobHistoryGroups addObject:group];
        }];
    }
    
    return listPrintJobHistoryGroups;
}

+ (void)deletePrintJob:(PrintJob*)printJob
{
    [DatabaseManager deleteObject:printJob];
}

+ (void)populateWithTestData
{
    // TEST DATA CONSTANTS
    static const NSUInteger TEST_NUM_PRINTERS = 9;
    static const NSUInteger TEST_NUM_JOBS[9] = {3, 4, 2, 1, 3, 6, 2, 4};
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    // delete first all existing printers
    while (pm.countSavedPrinters != 0)
        [pm deletePrinterAtIndex:0];
    
    // replace with test printers
    for (int i = 1; i < TEST_NUM_PRINTERS; i++)
    {
        PrinterDetails* testPrinterDetails = [[PrinterDetails alloc] init];
        testPrinterDetails.name = [NSString stringWithFormat:@"Test Printer %d", i];
        testPrinterDetails.ip = [NSString stringWithFormat:@"999.99.9.%d", i];
        testPrinterDetails.port = [NSNumber numberWithUnsignedInt:i*100];
        [pm registerPrinter:testPrinterDetails];
    }
    
    // create print jobs and attach to the test printers
    for (int printerIdx = 0; printerIdx < TEST_NUM_PRINTERS; printerIdx++)
    {
        Printer* testPrinter = [pm getPrinterAtIndex:printerIdx];
        for (int jobIdx = 0; jobIdx < TEST_NUM_JOBS[printerIdx]; jobIdx++)
        {
            PrintJob* newPrintJob = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
            newPrintJob.name = [NSString stringWithFormat:@"Test Job %d-%d", printerIdx+1, jobIdx+1];
            newPrintJob.result = [NSNumber numberWithInt:jobIdx%2]; //alternate OK and NG
            newPrintJob.date = [NSDate dateWithTimeIntervalSinceNow:(arc4random()%60)*1000]; //2min intervals
            newPrintJob.printer = testPrinter;
            [newPrintJob log];
        }
    }
    [DatabaseManager saveChanges];
}

@end
