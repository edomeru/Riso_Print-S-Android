//
//  PrintJob.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Printer;

@interface PrintJob : NSManagedObject

@property (nonatomic, retain) NSDate* date;
@property (nonatomic, retain) NSString* name;
@property (nonatomic, retain) NSNumber* result;
@property (nonatomic, retain) Printer* printer;

@end
