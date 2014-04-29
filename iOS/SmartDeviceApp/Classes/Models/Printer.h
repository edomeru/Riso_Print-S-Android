//
//  Printer.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class DefaultPrinter, PrintJob, PrintSetting;

@interface Printer : NSManagedObject

@property (nonatomic, retain) NSNumber* enabled_booklet;
@property (nonatomic, retain) NSNumber* enabled_finisher_2_3_holes;
@property (nonatomic, retain) NSNumber* enabled_finisher_2_4_holes;
@property (nonatomic, retain) NSNumber* enabled_lpr;
@property (nonatomic, retain) NSNumber* enabled_raw;
@property (nonatomic, retain) NSNumber* enabled_staple;
@property (nonatomic, retain) NSNumber* enabled_tray_auto_stacking;
@property (nonatomic, retain) NSNumber* enabled_tray_face_down;
@property (nonatomic, retain) NSNumber* enabled_tray_stacking;
@property (nonatomic, retain) NSNumber* enabled_tray_top;
@property (nonatomic, retain) NSString* ip_address;
@property (nonatomic, retain) NSString* name;
@property (nonatomic, retain) NSNumber* port;
@property (nonatomic, retain) NSNumber* onlineStatus;
@property (nonatomic, retain) DefaultPrinter* defaultprinter;
@property (nonatomic, retain) NSSet* printjob;
@property (nonatomic, retain) PrintSetting* printsetting;
@end

@interface Printer (CoreDataGeneratedAccessors)

- (void)addPrintjobObject:(PrintJob*)value;
- (void)removePrintjobObject:(PrintJob*)value;
- (void)addPrintjob:(NSSet*)values;
- (void)removePrintjob:(NSSet*)values;

@end
