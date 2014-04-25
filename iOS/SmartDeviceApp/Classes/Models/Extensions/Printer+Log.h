//
//  Printer+Log.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "Printer.h"

@interface Printer (Log)

/**
 Logs the Printer info and capabilities.
 This is used for debugging only.
 */
- (void)log;

@end
