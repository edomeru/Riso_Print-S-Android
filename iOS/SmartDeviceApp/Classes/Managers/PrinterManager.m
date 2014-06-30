//
//  PrinterManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrinterManager.h"
#import "Printer.h"
#import "Printer+Log.h"
#import "PrinterDetails.h"
#import "PrintSetting.h"
#import "DefaultPrinter.h"
#import "DatabaseManager.h"
#import "SNMPManager.h"
#import "PListHelper.h"
#import "NotificationNames.h"
#import "PrintSettingsHelper.h"

static PrinterManager* sharedPrinterManager = nil;

@interface PrinterManager ()

/** 
 * Reference to all the Printer objects currently saved in the database.
 * This list is auto-initialized at the start during the call to {@link sharedPrinterManager}.\n
 * It is then auto-updated when add or delete operations are performed.
 */
@property (strong, nonatomic) NSMutableArray* listSavedPrinters;

/**
 * Reference to the DefaultPrinter object.
 * This property is not nil if there is a printer set as the default printer.
 */
@property (strong, nonatomic) DefaultPrinter* defaultPrinter;

/** 
 * Number of Printer objects in {@link listSavedPrinters}.
 * This counter is auto-updated when add or delete operations are performed.
 */
@property (readwrite, assign, nonatomic) NSUInteger countSavedPrinters;

/**
 * Maximum number of printers that are allowed to be added to the database.
 */
@property (assign, nonatomic) NSUInteger maxPrinterCount;

/**
 * Gets the list of Printer objects saved in the database.
 * This list is stored in the {@link listSavedPrinters} property.
 */
- (void)getListOfSavedPrinters;

/**
 * Gets the DefaultPrinter object saved in the database.
 * The DefaultPrinter object is stored in the {@link defaultPrinter} property.
 */
- (void)retrieveDefaultPrinter;

@end

@implementation PrinterManager

#pragma mark - Initialization

/** Designated Initializer */
- (id)init
{
    self = [super init];
    if (self)
    {
        self.searchDelegate = nil;
        self.maxPrinterCount = [PListHelper readUint:kPlistUintValMaxPrinters];
        
#if DEBUG_LOG_PRINTER_MANAGER
        NSLog(@"[INFO][PM] getting printers from DB");
#endif
        [self getListOfSavedPrinters];
        
#if DEBUG_LOG_PRINTER_MANAGER
        NSLog(@"[INFO][PM] getting default printer from DB");
#endif
        [self retrieveDefaultPrinter];
    }
    return self;
}

+ (PrinterManager*)sharedPrinterManager
{
    @synchronized(self)
    {
        if (sharedPrinterManager == nil)
            sharedPrinterManager = [[self alloc] init];
    }
    return sharedPrinterManager;
}

#pragma mark - Printers in DB

- (BOOL)registerPrinter:(PrinterDetails*)printerDetails
{
    // create a PrintSetting object
    PrintSetting* defaultPrintSettings = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    if (defaultPrintSettings == nil)
    {
        return NO;
    }
    [PrintSettingsHelper copyDefaultPrintSettings:&defaultPrintSettings];
    
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
    newPrinter.enabled_booklet_finishing = [NSNumber numberWithBool:printerDetails.enBookletFinishing];
    newPrinter.enabled_finisher_2_3_holes = [NSNumber numberWithBool:printerDetails.enFinisher23Holes];
    newPrinter.enabled_finisher_2_4_holes = [NSNumber numberWithBool:printerDetails.enFinisher24Holes];
    newPrinter.enabled_lpr = [NSNumber numberWithBool:printerDetails.enLpr];
    newPrinter.enabled_raw = [NSNumber numberWithBool:printerDetails.enRaw];
    newPrinter.enabled_staple = [NSNumber numberWithBool:printerDetails.enStaple];
    newPrinter.enabled_tray_face_down = [NSNumber numberWithBool:printerDetails.enTrayFaceDown];
    newPrinter.enabled_tray_stacking = [NSNumber numberWithBool:printerDetails.enTrayStacking];
    newPrinter.enabled_tray_top = [NSNumber numberWithBool:printerDetails.enTrayTop];
    
    // attach the PrintSetting to the Printer
    newPrinter.printsetting = defaultPrintSettings;
    
    // set the online status
    newPrinter.onlineStatus = [NSNumber numberWithBool:printerDetails.isPrinterFound];
    
    // save the Printer and PrintSetting objects to DB
    if ([DatabaseManager saveChanges])
    {
#if DEBUG_LOG_PRINTER_MODEL
        [newPrinter log];
#endif
        [self.listSavedPrinters addObject:newPrinter];
        self.countSavedPrinters++;
        if (self.countSavedPrinters == 1)
        {
            // if this is the only printer, also set it as the default printer
            [self registerDefaultPrinter:newPrinter];
        }
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

- (void)retrieveDefaultPrinter
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
    //check if the index is valid
    if (index >= self.countSavedPrinters)
    {
#if DEBUG_LOG_PRINTER_MANAGER
        NSLog(@"[ERROR][PM] printer index=%lu >= countSavedPrinters=%lu",
              (unsigned long)index, (unsigned long)self.countSavedPrinters);
#endif
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
#if DEBUG_LOG_PRINTER_MANAGER
        NSLog(@"[ERROR][PM] printer index=%lu >= countSavedPrinters=%lu",
              (unsigned long)index, (unsigned long)self.countSavedPrinters);
#endif
        return NO;
    }
    
    // get the printer to delete
    Printer* printerToDelete = [self.listSavedPrinters objectAtIndex:index];
    
    // check if this printer is the default printer
    if ([self isDefaultPrinter:printerToDelete])
    {
        // check if this is NOT the last printer
        if (self.countSavedPrinters > 1)
        {
            NSUInteger indexOfNext;
            if (index == 0)
            {
                // mark the next printer as the default
                indexOfNext = 1;
            }
            else
            {
                // mark the first printer as the default
                indexOfNext = 0;
            }
            
            if (![self registerDefaultPrinter:[self.listSavedPrinters objectAtIndex:indexOfNext]])
                return NO;
        }
        else
        {
            // last printer
            // just delete the default printer object
            self.defaultPrinter.printer = nil;
            if ([DatabaseManager deleteObject:self.defaultPrinter])
            {
                self.defaultPrinter = nil;
            }
            else
            {
                return NO;
            }
        }
    }
    
    // delete the printer
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] deleting Printer %@ at row %lu",printerToDelete.name, (unsigned long)index);
#endif
    if ([DatabaseManager deleteObject:printerToDelete])
    {
        [self.listSavedPrinters removeObjectAtIndex:index];
        self.countSavedPrinters--;
        return YES;
    }
    else
    {
        return NO;
    }
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

- (Printer*)getDefaultPrinter
{
    return self.defaultPrinter.printer;
}

- (BOOL)savePrinterChanges
{
    return [DatabaseManager saveChanges];
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
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] initiating search");
    NSLog(@"[INFO][PM] waiting for notifications from SNMP");
#endif
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        SNMPManager* snmpManager = [SNMPManager sharedSNMPManager];
        [snmpManager searchForPrinter:printerIP];
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
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] initiating search");
    NSLog(@"[INFO][PM] waiting for notifications from SNMP");
#endif
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        SNMPManager* snmpManager = [SNMPManager sharedSNMPManager];
        [snmpManager searchForAvailablePrinters];
    });
    // after starting the search, control will immediately return to the screen controller
    // results of the search should be handled by the notification observers
}

- (void)stopSearching
{
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] stop waiting for notifications");
#endif
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    SNMPManager* snmpManager = [SNMPManager sharedSNMPManager];
    [snmpManager cancelSearch];
}

#pragma mark - SNMP Printer Search Notifications

- (void)notifyPrinterFound:(NSNotification*)notif
{
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] received notification - printer found");
#endif
    
    // get the printer details
    NSDictionary *userInfo = [notif userInfo];
    PrinterDetails* printerInfoCapabilities = (PrinterDetails*)[userInfo objectForKey:@"printerDetails"];
    
    // check if this is a new printer
    __weak PrinterManager* weakSelf = self;
    if ([self isIPAlreadyRegistered:printerInfoCapabilities.ip])
    {
        if ([self.searchDelegate respondsToSelector:@selector(printerSearchDidFoundOldPrinter:withName:)])
        {
            // this is an old printer
            // update the UI (UI thread)
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf.searchDelegate printerSearchDidFoundOldPrinter:printerInfoCapabilities.ip
                                                                withName:printerInfoCapabilities.name];
            });
        }
    }
    else
    {
        // this is a new printer
        // update the UI (UI thread)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.searchDelegate printerSearchDidFoundNewPrinter:printerInfoCapabilities];
        });
    }
}

- (void)notifySearchEnded:(NSNotification*)notif
{
#if DEBUG_LOG_PRINTER_MANAGER
    NSLog(@"[INFO][PM] received notification - search ended");
#endif
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    //get the search result
    NSDictionary *userInfo = [notif userInfo];
    BOOL result = [(NSNumber*)[userInfo objectForKey:@"result"] boolValue];
    
    __weak PrinterManager* weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf.searchDelegate printerSearchEndedwithResult:result];
    });
}

#pragma mark - Printer Utilities

- (BOOL)isAtMaximumPrinters
{
    if ([self.listSavedPrinters count] == self.maxPrinterCount)
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
