//
//  Printer+Log.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "Printer.h"

@interface Printer (Log)

/**
 Logs the Printer info and capabilities.
 This is used for debugging only.
 
 @param printer
        the Printer object to log
 **/
- (void)log;

@end
