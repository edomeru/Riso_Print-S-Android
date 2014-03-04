//
//  PrintJob.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Printer;

@interface PrintJob : NSManagedObject

@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * date;
@property (nonatomic, retain) NSNumber * result;
@property (nonatomic, retain) Printer *printer;

@end
