//
//  PrinterDetails.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/** 
 * This is a non-NSManagedObject version of the Printer class.
 * This is used as a temporary container for the printer info
 * and capabilities when passing it around (i.e. between the
 * SNMPManager and the PrinterManager) before finally adding
 * it to the database.
 */
@interface PrinterDetails : NSObject

/**
 * Printer name.
 */
@property (strong, nonatomic) NSString* name;

/**
 * Printer IP address.
 */
@property (strong, nonatomic) NSString* ip;

/**
 * Printer port (LPR or Raw).
 */
@property (strong, nonatomic) NSNumber* port;

/**
 * Printer capability (Booklet-Finishing).
 */
@property (assign, nonatomic) BOOL enBookletFinishing;

/**
 * Printer capability (3-hole Punch).
 */
@property (assign, nonatomic) BOOL enFinisher23Holes;

/**
 * Printer capability (4-hole Punch).
 */
@property (assign, nonatomic) BOOL enFinisher24Holes;

/**
 * Printer capability (Support for LPR).
 */
@property (assign, nonatomic) BOOL enLpr;

/**
 * Printer capability (Support for Raw).
 */
@property (assign, nonatomic) BOOL enRaw;

/**
 * Printer capability (Staple).
 */
@property (assign, nonatomic) BOOL enStaple;

/**
 * Printer capability (Face-down Output Tray).
 */
@property (assign, nonatomic) BOOL enTrayFaceDown;

/**
 * Printer capability (Stacking Output Tray).
 */
@property (assign, nonatomic) BOOL enTrayStacking;

/**
 * Printer capability (Top Output Tray).
 */
@property (assign, nonatomic) BOOL enTrayTop;

/**
 * Flag whether the printer was found during search (SNMP).
 */
@property (assign, nonatomic) BOOL isPrinterFound;

@end
