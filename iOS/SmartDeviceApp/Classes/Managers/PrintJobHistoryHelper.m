//
//  PrintJobHistoryHelper.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryHelper.h"
#import "PrinterManager.h"
#import "DatabaseManager.h"
#import "Printer.h"
#import "PrinterDetails.h"
#import "PrintJob.h"
#import "PrintJob+Log.h"
#import "PrintJobHistoryGroup.h"
#import "PListHelper.h"

@interface PrintJobHistoryHelper ()

/**
 Fills the database with PrintJob objects.
 It deletes all existing Printers from the database, then
 adds some pre-defined number of PrintJob objects, assigns
 them each to a printer, then saves the database.
 This is to be used only for debugging.
 */
+ (void)populateWithTestData;

@end

@implementation PrintJobHistoryHelper

+ (NSMutableArray*)preparePrintJobHistoryGroups
{
    NSMutableArray* listPrintJobHistoryGroups = [NSMutableArray array];
    
    // retrieve from DB
    NSArray* listPrintJobs = [DatabaseManager getObjects:E_PRINTJOB];
    if (listPrintJobs != nil) // DB access did not fail
    {
        NSUInteger countPrintJobs = [listPrintJobs count];
        if (countPrintJobs == 0)
        {
            // DB has no data

            // check if will use test data
            BOOL usePrintJobTestData = [PListHelper readBool:kPlistBoolValUsePrintJobTestData];
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
            NSLog(@"[INFO][PrintJobHelper] usePrintJobTestData=%@", (usePrintJobTestData ? @"YES" : @"NO"));
#endif
            if (usePrintJobTestData)
            {
                // populate the DB
                [self populateWithTestData];
                
                // retrieve from DB again
                listPrintJobs = [DatabaseManager getObjects:E_PRINTJOB];
                countPrintJobs = [listPrintJobs count];
            }
        }
        
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[INFO][PrintJobHelper] listPrintJobs=%lu", (unsigned long)countPrintJobs);
#endif
     
        // sort the print jobs according to printer
        NSMutableDictionary* dictPrintJobHistoryGroups = [NSMutableDictionary dictionary];
        for (PrintJob* job in listPrintJobs)
        {
            // get the printer
            NSString* printerName = job.printer.name;
            
            // attempt to get the group for this printer
            PrintJobHistoryGroup* group = [dictPrintJobHistoryGroups objectForKey:printerName];
            if (group == nil)
            {
                // group does not exist yet
                
                // create a group for this printer
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
                [group addPrintJob:job];
            }
        }
        
        // sort each group by timestamp
        // add each group to the result list
        [dictPrintJobHistoryGroups enumerateKeysAndObjectsUsingBlock:^(NSString* printerName,
                                                                       PrintJobHistoryGroup* group,
                                                                       BOOL* stop)
        {
            [group sortPrintJobs];
            [listPrintJobHistoryGroups addObject:group];
        }];
    }
    
    return listPrintJobHistoryGroups;
}

+ (void)populateWithTestData
{
    // TEST DATA CONSTANTS
    const NSUInteger TEST_NUM_PRINTERS = 8;
    const NSUInteger TEST_NUM_JOBS[8] = {5, 8, 10, 1, 4, 10, 3, 7}; 
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    // delete first all existing printers
    while (pm.countSavedPrinters != 0)
    {
        if (![pm deletePrinterAtIndex:0])
            break;
    }
    if (pm.countSavedPrinters != 0)
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to delete all printers from DB");
#endif
        return;
    }
    
    // replace with test printers
    for (int i = 0; i < TEST_NUM_PRINTERS; i++)
    {
        PrinterDetails* testPrinterDetails = [[PrinterDetails alloc] init];
        testPrinterDetails.name = [NSString stringWithFormat:@"Test Printer %d", i+1];
        testPrinterDetails.ip = [NSString stringWithFormat:@"999.99.9.%d", i+1];
        testPrinterDetails.port = [NSNumber numberWithUnsignedInt:(i+1)*100];
        if (![pm registerPrinter:testPrinterDetails])
            break;
    }
    if (pm.countSavedPrinters != TEST_NUM_PRINTERS)
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add test printers to DB");
#endif
        return;
    }
    
    // create print jobs and attach to the test printers
    for (int printerIdx = 0; printerIdx < TEST_NUM_PRINTERS; printerIdx++)
    {
        Printer* testPrinter = [pm getPrinterAtIndex:printerIdx];
        for (int jobIdx = 0; jobIdx < TEST_NUM_JOBS[printerIdx]; jobIdx++)
        {
            PrintJob* newPrintJob = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
            if (newPrintJob == nil)
            {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
                NSLog(@"[ERROR][PrintJobHelper] unable to add print job %d-%d to DB", printerIdx, jobIdx);
#endif
                break;
            }
            newPrintJob.name = [NSString stringWithFormat:@"Test Job %d-%d", printerIdx+1, jobIdx+1];
            newPrintJob.result = [NSNumber numberWithInt:jobIdx%2]; //alternate OK and NG
            newPrintJob.date = [NSDate dateWithTimeIntervalSinceNow:(arc4random()%60)*1000]; //random times
            newPrintJob.printer = testPrinter;
#if DEBUG_LOG_PRINT_JOB_MODEL
            [newPrintJob log];
#endif
        }
    }
    
    if (![DatabaseManager saveChanges])
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add test print jobs to DB");
#endif
    }
}

@end
