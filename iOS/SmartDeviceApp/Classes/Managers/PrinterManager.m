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

@interface PrinterManager ()

@property (strong, nonatomic) NSMutableArray* listSavedPrinters;
@property (strong, nonatomic) DefaultPrinter* defaultPrinter;
@property (readwrite, assign, nonatomic) NSUInteger countSavedPrinters; //redeclare to modify

@end

@implementation PrinterManager

#pragma mark - Printers in DB

- (BOOL)registerPrinter:(PrinterDetails*)printerDetails
{
    // create a PrintSetting object
    PrintSetting* defaultPrintSettings = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    if (defaultPrintSettings == nil)
        return NO;
    [self copyDefaultPrintSettings:&defaultPrintSettings];
    
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
    newPrinter.printsetting = defaultPrintSettings;
    
    // set the online status
    // since the printer will only be added if online, initial setting is YES
    newPrinter.onlineStatus = [NSNumber numberWithBool:YES];
    
    // save the Printer and PrintSetting objects to DB
    if ([DatabaseManager saveChanges])
    {
        [newPrinter log];
        [self.listSavedPrinters addObject:newPrinter];
        self.countSavedPrinters++;
        return YES;
    }
    else
    {
        [DatabaseManager discardChanges]; //discard the Printer and PrintSetting objects
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
        [DatabaseManager discardChanges]; //discard the DefaultPrinter object
        return NO;
    }
}

- (void)getListOfSavedPrinters
{
    self.listSavedPrinters = [[DatabaseManager getObjects:E_PRINTER] mutableCopy];
    self.countSavedPrinters = [self.listSavedPrinters count];
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

- (Printer*)getPrinterAtIndex:(NSUInteger)index
{
    NSUInteger countSavedPrinters = [self.listSavedPrinters count];
    if (index >= countSavedPrinters)
    {
        NSLog(@"[ERROR][PM] printer index=%d >= countSavedPrinters=%d", index, countSavedPrinters);
        return nil;
    }
    else
        return [self.listSavedPrinters objectAtIndex:index];
}

- (BOOL)deletePrinterAtIndex:(NSUInteger)index
{
    // check if the index is valid
    if (index >= self.countSavedPrinters)
    {
        NSLog(@"[ERROR][PM] printer index=%d >= countSavedPrinters=%d", index, self.countSavedPrinters);
        return NO;
    }
    
    // get the printer to delete
    Printer* printerToDelete = [self.listSavedPrinters objectAtIndex:index];
    
    // check if this printer is the default printer
    if ([self.defaultPrinter.printer isEqual:printerToDelete])
    {
        // delete the default printer first
        NSLog(@"[INFO][PM] this is the default printer, remove default printer object first");
        [self deleteDefaultPrinter];
    }
    
    // delete the printer
    NSLog(@"[INFO][PM] deleting Printer %@ at row %d",printerToDelete.name, index);
    if ([DatabaseManager deleteObject:printerToDelete])
    {
        [self.listSavedPrinters removeObjectAtIndex:index];
        self.countSavedPrinters--;
        return YES;
    }
    else
        return NO;
}

- (BOOL)deleteDefaultPrinter
{
    if(self.defaultPrinter == nil)
    {
        return YES;
    }
    self.defaultPrinter.printer = nil;
    if ([DatabaseManager deleteObject:self.defaultPrinter] == YES)
    {
        self.defaultPrinter = nil;
    }
    else
    {
        return NO;
    }
    return YES;
}

- (BOOL)hasDefaultPrinter
{
    if (self.defaultPrinter == nil)
        return NO;
    else
        return YES;
}

- (BOOL)isDefaultPrinter:(Printer*)printer;
{
    if (self.defaultPrinter.printer != nil
        && [self.defaultPrinter.printer isEqual:printer])
        return YES;
    else
        return NO;
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
    NSLog(@"[INFO][PM] initiating search");
    NSLog(@"[INFO][PM] waiting for notifications from SNMP");
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
    NSLog(@"[INFO][PM] initiating search");
    NSLog(@"[INFO][PM] waiting for notifications from SNMP");
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [SNMPManager searchForAvailablePrinters];
    });
    // after starting the search, control will immediately return to the screen controller
    // results of the search should be handled by the notification observers
}

- (void)stopSearching
{
    NSLog(@"[INFO][PM] stop waiting for notifications");
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    //TODO: cascade the command to the SNMPManager
}

#pragma mark - SNMP Printer Search Notifications

- (void)notifyPrinterFound:(NSNotification*)notif
{
    NSLog(@"[INFO][PM] received notification - printer found");
    
    // get the printer details
    PrinterDetails* printerInfoCapabilities = (PrinterDetails*)[notif object];
    
    // check if this is a new printer
    __weak PrinterManager* weakSelf = self;
    if ([self isIPAlreadyRegistered:printerInfoCapabilities.ip])
    {
        // this is an old printer
        // update the UI (UI thread)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.searchDelegate updateForOldPrinter:printerInfoCapabilities.ip
                                          withName:printerInfoCapabilities.name];
        });
    }
    else
    {
        // this is a new printer
        // update the UI (UI thread)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.searchDelegate updateForNewPrinter:printerInfoCapabilities];
        });
    }
}

- (void)notifySearchEnded:(NSNotification*)notif
{
    NSLog(@"[INFO][PM] received notification - search ended");
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    __weak PrinterManager* weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf.searchDelegate searchEnded];
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

- (BOOL)isAtMaximumPrinters
{
    NSInteger maxPrinters = [PListUtils getMaxPrinters];
    if ([self.listSavedPrinters count] == maxPrinters)
        return YES;
    else
        return NO;
}

- (BOOL)isIPAlreadyRegistered:(NSString*)printerIP
{
    for (Printer* onePrinter in self.listSavedPrinters)
    {
        if ([printerIP isEqualToString:onePrinter.ip_address])
            return YES;
    }
    
    return NO;
}

@end
