//
//  PrinterManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterManager.h"
#import "Printer.h"
#import "PrintSetting.h"
#import "DefaultPrinter.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"
#import "PListUtils.h"

#define PRINTER_IP              0
#define PRINTER_NAME            1

@implementation PrinterManager

#pragma mark - Printers in DB

+ (Printer*)createPrinter
{
    // create first a PrintSetting object
    PrintSetting* printSetting = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    [self copyDefaultPrintSettings:&printSetting];
    
    // then create a Printer object
    Printer* printer = (Printer*)[DatabaseManager addObject:E_PRINTER];
    if (printer == nil)
    {
        [DatabaseManager discardChanges]; //discard the PrintSetting object
        return nil;
    }
    
    // finally attach the PrintSetting to the Printer
    printer.printsetting = printSetting;
    
    return printer;
}

+ (DefaultPrinter*)createDefaultPrinter:(Printer*)printer
{
    DefaultPrinter* defaultPrinter = (DefaultPrinter*)[DatabaseManager addObject:E_DEFAULTPRINTER];
    defaultPrinter.printer = printer;
    
    return defaultPrinter;
}

+ (NSMutableArray*)getPrinters
{
    return [[DatabaseManager getObjects:E_PRINTER] mutableCopy];
}

+ (DefaultPrinter*)getDefaultPrinter
{
    NSArray* results = [DatabaseManager getObjects:E_DEFAULTPRINTER];
    if(results != nil)
    {
        if([results count] > 0)
        {
            return [results objectAtIndex:0];
        }
    }

    //TODO: handle error (pass to controller to display error?)
    return nil;
}

#pragma mark - Printers in Network (SNMP)

+ (BOOL)searchForPrinter:(Printer**)printer
{
    NSArray* printerInfoCapabilities = [SNMPManager searchForPrinter:(*printer).ip_address];
    
    if (printerInfoCapabilities != nil)
    {
        //save printer info and capabilities to Printer object
        (*printer).name = [printerInfoCapabilities objectAtIndex:PRINTER_NAME];
        //TODO: add others here..
        //TODO: also update print settings object if needed
        
        return YES;
    }
    else
        return NO;
}

#pragma mark - Printer Utilities

+ (void)copyDefaultPrintSettings:(PrintSetting**)printSetting;
{
    NSDictionary* defaultPrintSettings = [PListUtils getDefaultPrintSettings];
    (*printSetting).bind = [defaultPrintSettings objectForKey:PS_BIND];
    (*printSetting).booklet_binding = [defaultPrintSettings objectForKey:PS_BOOKLET_BINDING];
    (*printSetting).booklet_tray = [defaultPrintSettings objectForKey:PS_BOOKLET_TRAY];
    (*printSetting).catch_tray = [defaultPrintSettings objectForKey:PS_CATCH_TRAY];
    (*printSetting).color_mode = [defaultPrintSettings objectForKey:PS_COLOR_MODE];
    (*printSetting).copies = [defaultPrintSettings objectForKey:PS_COPIES];
    (*printSetting).duplex = [defaultPrintSettings objectForKey:PS_DUPLEX];
    (*printSetting).image_quality = [defaultPrintSettings objectForKey:PS_IMAGE_QUALITY];
    (*printSetting).pagination = [defaultPrintSettings objectForKey:PS_PAGINATION];
    (*printSetting).paper_size = [defaultPrintSettings objectForKey:PS_PAPER_SIZE];
    (*printSetting).paper_type = [defaultPrintSettings objectForKey:PS_PAPER_TYPE];
    (*printSetting).punch = [defaultPrintSettings objectForKey:PS_PUNCH];
    (*printSetting).sort = [defaultPrintSettings objectForKey:PS_SORT];
    (*printSetting).staple = [defaultPrintSettings objectForKey:PS_STAPLE];
    (*printSetting).zoom = [defaultPrintSettings objectForKey:PS_ZOOM];
    (*printSetting).zoom_rate = [defaultPrintSettings objectForKey:PS_ZOOM_RATE];
}

+ (BOOL)canAddPrinter:(NSString*)printerIP toList:(NSArray*)listSavedPrinters;
{
    // check if maximum number of printers have been reached
    NSUInteger maxPrinters = [PListUtils getMaxPrinters];
    if ([listSavedPrinters count] == maxPrinters)
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
