//
//  PrintSetting.h
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Printer;

@interface PrintSetting : NSManagedObject

@property (nonatomic, retain) NSNumber * colorMode;
@property (nonatomic, retain) NSNumber * orientation;
@property (nonatomic, retain) NSNumber * copies;
@property (nonatomic, retain) NSNumber * duplex;
@property (nonatomic, retain) NSNumber * paperSize;
@property (nonatomic, retain) NSNumber * scaleToFit;
@property (nonatomic, retain) NSNumber * paperType;
@property (nonatomic, retain) NSNumber * inputTray;
@property (nonatomic, retain) NSNumber * imposition;
@property (nonatomic, retain) NSNumber * impositionOrder;
@property (nonatomic, retain) NSNumber * sort;
@property (nonatomic, retain) NSNumber * booklet;
@property (nonatomic, retain) NSNumber * bookletFinish;
@property (nonatomic, retain) NSNumber * bookletLayout;
@property (nonatomic, retain) NSNumber * finishingSide;
@property (nonatomic, retain) NSNumber * staple;
@property (nonatomic, retain) NSNumber * punch;
@property (nonatomic, retain) NSNumber * outputTray;
@property (nonatomic, retain) Printer *printer;

@end
