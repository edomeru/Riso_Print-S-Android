//
//  PrintJob+Log.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintJob.h"

/**
 * Extension of the PrintJob model that provides debugging methods.
 */
@interface PrintJob (Log)

/**
 * Logs the Print Job details.
 * This is used for debugging only.
 */
- (void)log;

@end
