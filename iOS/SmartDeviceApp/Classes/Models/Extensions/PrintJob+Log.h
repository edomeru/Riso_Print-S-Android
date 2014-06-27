//
//  PrintJob+Log.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJob.h"

@interface PrintJob (Log)

/**
 * Logs the Print Job details.
 * This is used for debugging only.
 */
- (void)log;

@end
