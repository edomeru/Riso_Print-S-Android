//
//  Printer.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class DefaultPrinter, PrintJob, PrintSetting;

/**
 * Printer is the entity that holds the information about a printer.
 * This also contains the finishing settings of the printer.
 * Printers has the following relationships
 * - DefaultPrinter
 * - PrintJob
 * - PrintSetting
 */
@interface Printer : NSManagedObject

/**
 * Flag if the printer will print the pages in booklet format.
 */
@property (nonatomic, retain) NSNumber* enabled_booklet_finishing;

/**
 * Flag if the printer's punch option for 2 or 3 holes is enabled.
 */
@property (nonatomic, retain) NSNumber* enabled_finisher_2_3_holes;

/**
 * Flag if the printer's punch option for 2 or 4 holes is enabled.
 */
@property (nonatomic, retain) NSNumber* enabled_finisher_2_4_holes;

/**
 * Flag if the printer's port is set to LPR
 */
@property (nonatomic, retain) NSNumber* enabled_lpr;

/**
 * Flag if the printer's port is set to Raw.
 */
@property (nonatomic, retain) NSNumber* enabled_raw;

/**
 * Flag if the print job will be stapled.
 */
@property (nonatomic, retain) NSNumber* enabled_staple;

/**
 * Flag if the printer's settings for output tray is face-down tray.
 */
@property (nonatomic, retain) NSNumber* enabled_tray_face_down;

/**
 * Flag if the printer's settings for output tray is stacking.
 */
@property (nonatomic, retain) NSNumber* enabled_tray_stacking;

/**
 * Flag if the printer's settings for output tray is top.
 */
@property (nonatomic, retain) NSNumber* enabled_tray_top;

/**
 * Flag if the printer's input tray option for external feeder is enabled.
 */
@property (nonatomic, retain) NSNumber* enabled_external_feeder;

/**
 * Flag if the printer's punch option is disabled.
 */
@property (nonatomic, retain) NSNumber* enabled_finisher_0_hole;

/**
 * The IP address of the printer.
 */
@property (nonatomic, retain) NSString* ip_address;

/**
 * The name of the printer.
 */
@property (nonatomic, retain) NSString* name;

/**
 * Port number to be used for connection.
 */
@property (nonatomic, retain) NSNumber* port;

/**
 * Connectivity status of the printer.
 */
@property (nonatomic, retain) NSNumber* onlineStatus;

/**
 * Reference to the default printer.
 */
@property (nonatomic, retain) DefaultPrinter* defaultprinter;

/**
 * Reference to the print job history of the printer.
 */
@property (nonatomic, retain) NSSet* printjob;

/**
 * Reference to the print settings of the printer.
 */
@property (nonatomic, retain) PrintSetting* printsetting;

@end

/**
 * This is an autogenerated category by Core Data.
 * This was created for the one-to-many relationship between the Printer and the PrintJob entities.
 */
@interface Printer (CoreDataGeneratedAccessors)

/**
 * This is an autogenerated method for adding a PrintJob object.
 */
- (void)addPrintjobObject:(PrintJob*)value;

/**
 * This is an autogenerated method for removing a PrintJob object.
 */
- (void)removePrintjobObject:(PrintJob*)value;

/**
 * This is an autogenerated method for adding a list of PrintJob objects.
 */
- (void)addPrintjob:(NSSet*)values;

/**
 * This is an autogenerated method for removing a list of PrintJob objects.
 */
- (void)removePrintjob:(NSSet*)values;

@end
