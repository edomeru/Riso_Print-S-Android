//
//  PrintSetting.h
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/5/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Printer;

@interface PrintSetting : NSManagedObject

@property (nonatomic, retain) NSNumber * bind;
@property (nonatomic, retain) NSNumber * booklet_binding;
@property (nonatomic, retain) NSNumber * booklet_tray;
@property (nonatomic, retain) NSNumber * catch_tray;
@property (nonatomic, retain) NSNumber * color_mode;
@property (nonatomic, retain) NSNumber * copies;
@property (nonatomic, retain) NSNumber * duplex;
@property (nonatomic, retain) NSNumber * image_quality;
@property (nonatomic, retain) NSNumber * pagination;
@property (nonatomic, retain) NSNumber * paper_size;
@property (nonatomic, retain) NSNumber * paper_type;
@property (nonatomic, retain) NSNumber * punch;
@property (nonatomic, retain) NSNumber * sort;
@property (nonatomic, retain) NSNumber * staple;
@property (nonatomic, retain) NSNumber * zoom;
@property (nonatomic, retain) NSNumber * zoom_rate;
@property (nonatomic, retain) Printer *printer;

@end
