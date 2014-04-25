//
//  PrinterDetails.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PrinterDetails : NSObject

// This is a non-NSManagedObject version of the Printer class.
// This is used as a temporary container for the printer info
// and capabilities when passing it around (i.e. between the
// SNMPManager and the PrinterManager).
// ..
// WHAT IS THIS FOR?
// Creating a Printer object also inserts an NSManagedObject
// into the database. For temporary use, this object has to
// be discarded before adding a new one, which presents a
// problem for screens such as the Printer Search.
// ..
//TODO: find an alternative for this
//TODO: NSDictionary? setting NSManagedObjectContext to nil?

@property (strong, nonatomic) NSString* name;
@property (strong, nonatomic) NSString* ip;
@property (strong, nonatomic) NSNumber* port;
@property (assign, nonatomic) BOOL enBind;
@property (assign, nonatomic) BOOL enBookletBind;
@property (assign, nonatomic) BOOL enDuplex;
@property (assign, nonatomic) BOOL enPagination;
@property (assign, nonatomic) BOOL enStaple;
@property (assign, nonatomic) BOOL enLPR;
@property (assign, nonatomic) BOOL enRAW;
@property (assign, nonatomic) BOOL enPunch3Holes;

@property (assign, nonatomic) BOOL capBooklet;
@property (assign, nonatomic) BOOL capStapler;
@property (assign, nonatomic) BOOL capFin24Holes;
@property (assign, nonatomic) BOOL capFin23Holes;
@property (assign, nonatomic) BOOL capTrayFaceDown;
@property (assign, nonatomic) BOOL capTrayAutoStack;
@property (assign, nonatomic) BOOL capTrayTop;
@property (assign, nonatomic) BOOL capTrayStack;

@end
