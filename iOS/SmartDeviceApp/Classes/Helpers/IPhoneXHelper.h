//
//  IPhoneXHelper.h
//  RISOSmartPrint
//
//  Created by SDA on 19/10/2017.
//  Copyright © 2017 aLink. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * Helper class for handling iPhone X
 *
 */
@interface IPhoneXHelper : NSObject

/**
 * Checks whether the current device or simulator is an iPhone X
 */
+ (BOOL)isDeviceIPhoneX;

/**
 * Returns the height of the sensor housing on an iPhone X
 */
+ (CGFloat)sensorHousingHeight;

@end
