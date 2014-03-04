//
//  Printer.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class PrintSetting;

@interface Printer : NSManagedObject

@property (nonatomic, retain) NSNumber * enabled_lpr;
@property (nonatomic, retain) NSString * ip_address;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * port;
@property (nonatomic, retain) NSNumber * enabled_raw;
@property (nonatomic, retain) NSNumber * enabled_pagination;
@property (nonatomic, retain) NSNumber * enabled_duplex;
@property (nonatomic, retain) NSNumber * enabled_booklet_binding;
@property (nonatomic, retain) NSNumber * enabled_staple;
@property (nonatomic, retain) NSNumber * enabled_bind;
@property (nonatomic, retain) PrintSetting *printsetting;

@end
