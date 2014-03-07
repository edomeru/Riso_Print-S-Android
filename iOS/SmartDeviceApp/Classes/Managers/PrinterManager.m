//
//  PrinterManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterManager.h"
#import "Printer.h"
#import "DefaultPrinter.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"

#define PRINTER_IP      0
#define PRINTER_NAME    1

#define PRINTER_MAX_COUNT   20

@implementation PrinterManager


+ (Printer*)createPrinter
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    
    return [NSEntityDescription insertNewObjectForEntityForName:@"Printer"
                                         inManagedObjectContext:context];
}

+ (PrintSetting *)createPrintSetting
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    
    return [NSEntityDescription insertNewObjectForEntityForName:@"PrintSetting"
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
+(NSMutableArray *) getPrinters
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc]  initWithEntityName:@"Printer" ];
    NSError *error;
    return [[context executeFetchRequest:fetchRequest error:&error] mutableCopy];
}

+(DefaultPrinter *) getDefaultPrinter
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc]  initWithEntityName:@"DefaultPrinter" ];
    NSError *error;
    NSArray *fetchResult = [context executeFetchRequest:fetchRequest error:&error];
    if(fetchResult != nil)
    {
        if([fetchResult count] > 0)
        {
            return [fetchResult objectAtIndex:0];
        }
    }
    else
    {
        //TODO show error
    }
    return nil;
}

+ (DefaultPrinter*)createDefaultPrinter :(Printer *) printer
{
    NSManagedObjectContext* context = [DatabaseManager getManagedObjectContext];
    
    DefaultPrinter *defaultPrinter = [NSEntityDescription insertNewObjectForEntityForName:@"DefaultPrinter"
                                         inManagedObjectContext:context];
    defaultPrinter.printer = printer;
    
    return defaultPrinter;
}
@end
