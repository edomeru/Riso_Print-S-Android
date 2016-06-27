//
//  AppSettingsHelper.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AppSettingsHelper : NSObject

/**
 * Gets the current value of the SNMP Community Name from the NSUserDefaults
 * @return String object containing the current SNMP Community Name
 */
+ (NSString *)getSNMPCommunityName;

/**
 * Saves a new value of the SNMP Community Name to the NSUserDefaults
 * @param communityName
 *        New value for SNMP community name
 */
+ (void)saveSNMPCommunityName:(NSString *)communityName;

@end
