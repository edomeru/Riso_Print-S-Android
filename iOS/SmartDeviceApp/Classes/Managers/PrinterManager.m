//
//  PrinterManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrinterManager.h"
#import "Printer.h"
#import "Printer+Log.h"
#import "PrinterDetails.h"
#import "PrintSetting.h"
#import "DefaultPrinter.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"
#import "PListUtils.h"

@implementation PrinterManager

#pragma mark - Printers in DB

- (BOOL)registerPrinter:(PrinterDetails*)printerDetails
{
    // create a PrintSetting object
    PrintSetting* printSetting = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    if (printSetting == nil)
        return NO;
    [self copyDefaultPrintSettings:&printSetting];
    
    // create a Printer object
    Printer* newPrinter = (Printer*)[DatabaseManager addObject:E_PRINTER];
    if (newPrinter == nil)
    {
        [DatabaseManager discardChanges]; //discard the PrintSetting object
        return NO;
    }
    newPrinter.name = printerDetails.name;
    newPrinter.ip_address = printerDetails.ip;
    newPrinter.port = printerDetails.port;
    newPrinter.enabled_bind = [NSNumber numberWithBool:printerDetails.enBind];
    newPrinter.enabled_booklet_binding = [NSNumber numberWithBool:printerDetails.enBookletBind];
    newPrinter.enabled_duplex = [NSNumber numberWithBool:printerDetails.enDuplex];
    newPrinter.enabled_pagination = [NSNumber numberWithBool:printerDetails.enPagination];
    newPrinter.enabled_staple = [NSNumber numberWithBool:printerDetails.enStaple];
    newPrinter.enabled_lpr = [NSNumber numberWithBool:printerDetails.enLPR];
    newPrinter.enabled_raw = [NSNumber numberWithBool:printerDetails.enRAW];
    
    // attach the PrintSetting to the Printer
    newPrinter.printsetting = printSetting;
    
    // set the online status
    newPrinter.onlineStatus = [NSNumber numberWithBool:YES];
    
    // save to DB
    if ([DatabaseManager saveChanges])
    {
        [newPrinter log];
        [self.listSavedPrinters addObject:newPrinter];
        return YES;
    }
    else
    {
        [DatabaseManager discardChanges];
        return NO;
    }
}

- (BOOL)registerDefaultPrinter:(Printer*)printer
{
    if (self.defaultPrinter == nil) //there is no previous default printer
    {
        self.defaultPrinter = (DefaultPrinter*)[DatabaseManager addObject:E_DEFAULTPRINTER];
        if (self.defaultPrinter == nil)
        {
            return NO;
        }
    }
    
    self.defaultPrinter.printer = printer;
    if ([DatabaseManager saveChanges])
    {
        return YES;
    }
    else
    {
        [DatabaseManager discardChanges];
        return NO;
    }
}

- (void)getPrinters
{
    self.listSavedPrinters = [[DatabaseManager getObjects:E_PRINTER] mutableCopy];
}

- (void)getDefaultPrinter
{
    NSArray* results = [DatabaseManager getObjects:E_DEFAULTPRINTER];
    if ((results != nil) && [results count] > 0)
    {
        self.defaultPrinter = [results objectAtIndex:0];
    }
    else
    {
        self.defaultPrinter = nil;
    }
}

- (void)deletePrinter:(Printer*)printer
{
    NSUInteger indexDeleted = [self.listSavedPrinters indexOfObject:printer];
    if ([DatabaseManager deleteObject:printer])
    {
        [self.listSavedPrinters removeObjectAtIndex:indexDeleted];
        self.defaultPrinter.printer = nil;
    }
}

- (void)deleteDefaultPrinter
{
    if ([DatabaseManager deleteObject:self.defaultPrinter])
    {
        self.defaultPrinter = nil;
    }
}

#pragma mark - Printers in Network (SNMP)

- (void)searchForPrinter:(NSString*)printerIP
{
    // register notification observers
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(notifyPrinterFound:)
                                                 name:NOTIF_SNMP_ADD
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(notifySearchEnded:)
                                                 name:NOTIF_SNMP_END
                                               object:nil];
    
    // start the search (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [SNMPManager searchForPrinter:printerIP];
    });
    // after starting the search, control will immediately return to the screen controller
    // results of the search should be handled by the notification observers
}

- (void)searchForAllPrinters
{
    // register notification observers
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(notifyPrinterFound:)
                                                 name:NOTIF_SNMP_ADD
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(notifySearchEnded:)
                                                 name:NOTIF_SNMP_END
                                               object:nil];
    
    // start the search (background thread)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [SNMPManager searchForAvailablePrinters];
    });
    // after starting the search, control will immediately return to the screen controller
    // results of the search should be handled by the notification observers
}

- (void)stopSearching
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    //TODO: cascade the command to the SNMPManager
}

#pragma mark - SNMP Notifications

- (void)notifyPrinterFound:(NSNotification*)notif
{
    NSLog(@"received notification that a printer was found");
    
    // get the printer details
    PrinterDetails* printerInfoCapabilities = (PrinterDetails*)[notif object];
    
    // check if this is a new printer or already saved
    __weak PrinterManager* weakSelf = self;
    if ([self canAddPrinter:printerInfoCapabilities.ip])
    {
        // update the UI (UI thread)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.delegate updateForNewPrinter:printerInfoCapabilities];
        });
    }
    else
    {
        // update the UI (UI thread)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.delegate updateForOldPrinter:printerInfoCapabilities.ip
                                         withExtra:@[printerInfoCapabilities.name]];
        });
    }
}

- (void)notifySearchEnded:(NSNotification*)notif
{
    NSLog(@"received notification that the search has ended");
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    __weak PrinterManager* weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf.delegate searchEnded];
    });
}

#pragma mark - Printer Utilities

- (void)copyDefaultPrintSettings:(PrintSetting**)printSetting;
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

- (BOOL)canAddPrinter:(NSString*)printerIP
{
    // check if maximum number of printers have been reached
    NSInteger maxPrinters = [PListUtils getMaxPrinters];
    if ([self.listSavedPrinters count] == maxPrinters)
        return NO;
    
    // check if there is no existing/duplicate printer on the list
    for (Printer* onePrinter in self.listSavedPrinters)
    {
        if ([printerIP isEqualToString:onePrinter.ip_address])
            return NO;
    }
    
    // no issues, printer can be added
    return YES;
}

@end
