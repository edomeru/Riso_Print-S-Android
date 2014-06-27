//
//  PrintSetting+Log.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "PrintSetting.h"

/**
 * Extension of the PrintSetting model that provides debugging methods.
 */
@interface PrintSetting (Log)

/**
 * Logs the PrintSetting object.
 * This is used for debugging only.
 */
- (void)log;

@end
