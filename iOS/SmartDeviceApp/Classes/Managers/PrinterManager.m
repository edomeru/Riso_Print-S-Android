//
//  PrinterManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterManager.h"
#import "Printer.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"

#define PRINTER_IP      0
#define PRINTER_NAME    1

#define PRINTER_MAX_COUNT   20

@implementation PrinterManager

+ (Printer*)createPrinter
{
    id delegate = [[UIApplication sharedApplication] delegate];
    NSManagedObjectContext* context = [delegate managedObjectContext];
    
    return [NSEntityDescription insertNewObjectForEntityForName:@"Printer"
                                         inManagedObjectContext:context];
}

+ (BOOL)searchForPrinter:(Printer**)printer
{
    NSArray* printerInfoCapabilities = [SNMPManager searchForPrinter:(*printer).ip_address];
    
    if (printerInfoCapabilities != nil)
    {
        //save printer info and capabilities to Printer object
        (*printer).name = [printerInfoCapabilities objectAtIndex:PRINTER_NAME];
        //TODO: add others here..
        
        return YES;
    }
    else
        return NO;
}

+ (BOOL)addPrinterToDB:(Printer*)printer
{
    return [DatabaseManager addToDB:printer forEntityName:@"Printer"];
}

+ (BOOL)canAddPrinter:(NSString*)printerIP toList:(NSArray*)listSavedPrinters;
{
    // check if maximum number of printers have been reached
    if ([listSavedPrinters count] == 20)
        return NO;
    
    // check if there is no existing/duplicate printer on the list
    for (Printer* onePrinter in listSavedPrinters)
    {
        if ([printerIP isEqualToString:onePrinter.ip_address])
            return NO;
    }
    
    // no issues, printer can be added
    return YES;
}

@end
