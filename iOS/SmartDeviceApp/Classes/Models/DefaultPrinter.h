//
//  DefaultPrinter.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Printer;

@interface DefaultPrinter : NSManagedObject

@property (nonatomic, retain) Printer *printer;

@end
