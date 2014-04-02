//
//  PrintJob+Log.h
//  SmartDeviceApp
//
//  Created by Gino Mempin on 4/2/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJob.h"

@interface PrintJob (Log)

/**
 Logs the Print Job details.
 This is used for debugging only.
 */
- (void)log;

@end
