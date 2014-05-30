//
//  PrintJobHistoryHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
#import "PrintDocument.h"

@interface PrintJobHistoryHelper ()

/**
 Fills the database with PrintJob objects.
 It creates default-capability Printers, adds some pre-defined 
 number of PrintJob objects per printer, then saves the database.
 This is to be used only for debugging.
 */
+ (void)populateWithTestData;

@end

@implementation PrintJobHistoryHelper

+ (NSMutableArray*)preparePrintJobHistoryGroups
{
    NSMutableArray* listPrintJobHistoryGroups = [NSMutableArray array];
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    if ((pm.countSavedPrinters == 0)
        && [PListHelper readBool:kPlistBoolValUsePrintJobTestData])
    {
        [self populateWithTestData];
    }
    
    NSInteger groupTag = 0; //unique,immutable group identifier
    for (NSUInteger idx = 0; idx < pm.countSavedPrinters; idx++)
    {
        // get the printer
        Printer* printer = [pm getPrinterAtIndex:idx];
        
        // create the group
        PrintJobHistoryGroup* group = [PrintJobHistoryGroup initWithGroupName:printer.name
                                                                  withGroupIP:printer.ip_address
                                                                 withGroupTag:groupTag++];
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"name=[%@], ip=[%@]", group.groupName, group.groupIP);
#endif
        
        // retrieve the jobs
        NSString* filter = [NSString stringWithFormat:@"printer.ip_address = '%@'", printer.ip_address];
        NSArray* jobs = [DatabaseManager getObjects:E_PRINTJOB usingFilter:filter];
        for (PrintJob* job in jobs)
        {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
            NSLog(@"  job=[%@]", job.name);
#endif
            [group addPrintJob:job];
        }
        
        [group sortPrintJobs];
        [group collapse:NO];
        
        [listPrintJobHistoryGroups addObject:group];
    }
                  
    return listPrintJobHistoryGroups;
}

+ (BOOL)createPrintJobFromDocument:(PrintDocument *)printDocument withResult:(NSInteger)result
{
    PrintJob *newPrintJob = (PrintJob *)[DatabaseManager addObject:E_PRINTJOB];
    if (newPrintJob == nil)
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add print job %d-%d to DB", printerIdx, jobIdx);
#endif
        return NO;
    }
    
    // Add details
    newPrintJob.name = printDocument.name;
    newPrintJob.printer = printDocument.printer;
    newPrintJob.result = [NSNumber numberWithInteger:result];
    newPrintJob.date = [NSDate date];
    
#if DEBUG_LOG_PRINT_JOB_MODEL
    [newPrintJob log];
#endif
    
    if (![DatabaseManager saveChanges])
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add test print jobs to DB");
#endif
        return NO;
    }
    
    return YES;
}

+ (void)populateWithTestData
{
    // TEST DATA CONSTANTS
    NSString* TEST_PRINTER_NAME = @"PrintJob Test Printer";
    NSString* TEST_PRINTER_IP = @"999.99.9";
    NSString* TEST_JOB_NAME = @"Test Job";
    const NSUInteger TEST_NUM_PRINTERS = 8;
    const NSUInteger TEST_NUM_JOBS[TEST_NUM_PRINTERS] = {5, 8, 10, 1, 4, 10, 3, 7};
    
    PrinterManager* pm = [PrinterManager sharedPrinterManager];
    
    // remove existing test printers
    NSUInteger printerIdx = 0;
    while (printerIdx < pm.countSavedPrinters)
    {
        Printer* printer = [pm getPrinterAtIndex:printerIdx];
        if ([printer.name hasPrefix:TEST_PRINTER_NAME])
        {
            if (![pm deletePrinterAtIndex:printerIdx])
            {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
                NSLog(@"[ERROR][PrintJobHelper] unable to delete existing test printer");
#endif
                return;
            }
        }
        else
        {
            printerIdx++;
        }
    }
    
    NSUInteger numNonTestPrinters = pm.countSavedPrinters;
    
    // add the test printers
    for (int printerIdx = 0; printerIdx < TEST_NUM_PRINTERS; printerIdx++)
    {
        PrinterDetails* testPrinterDetails = [[PrinterDetails alloc] init];
        testPrinterDetails.name = [NSString stringWithFormat:@"%@ %d", TEST_PRINTER_NAME, printerIdx+1];
        testPrinterDetails.ip = [NSString stringWithFormat:@"%@.%d", TEST_PRINTER_IP, printerIdx+1];
        testPrinterDetails.port = [NSNumber numberWithUnsignedInt:(printerIdx+1)*100];
        if (![pm registerPrinter:testPrinterDetails])
            break;
    }
    if (pm.countSavedPrinters != (numNonTestPrinters + TEST_NUM_PRINTERS))
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add test printers to DB");
#endif
        return;
    }
    
    // create print jobs and attach to the test printers
    int testPrinterIdx = 0;
    for (int printerIdx = 0; printerIdx < pm.countSavedPrinters; printerIdx++)
    {
        Printer* testPrinter = [pm getPrinterAtIndex:printerIdx];
        if (![testPrinter.name hasPrefix:TEST_PRINTER_NAME])
            continue; // this is not a test printer
        
        for (int jobIdx = 0; jobIdx < TEST_NUM_JOBS[testPrinterIdx]; jobIdx++)
        {
            PrintJob* newPrintJob = (PrintJob*)[DatabaseManager addObject:E_PRINTJOB];
            if (newPrintJob == nil)
            {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
                NSLog(@"[ERROR][PrintJobHelper] unable to add print job %d-%d to DB", printerIdx, jobIdx);
#endif
                break;
            }
            newPrintJob.name = [NSString stringWithFormat:@"%@ %d-%d", TEST_JOB_NAME, testPrinterIdx+1, jobIdx+1];
            newPrintJob.result = [NSNumber numberWithInt:jobIdx%2]; //alternate OK and NG
            newPrintJob.date = [NSDate dateWithTimeIntervalSinceNow:(arc4random()%60)*1000]; //random times
            newPrintJob.printer = testPrinter;
#if DEBUG_LOG_PRINT_JOB_MODEL
            [newPrintJob log];
#endif
        }
        testPrinterIdx++;
    }
    
    if (![DatabaseManager saveChanges])
    {
#if DEBUG_LOG_PRINT_JOB_HISTORY_HELPER
        NSLog(@"[ERROR][PrintJobHelper] unable to add test print jobs to DB");
#endif
    }
}

@end
