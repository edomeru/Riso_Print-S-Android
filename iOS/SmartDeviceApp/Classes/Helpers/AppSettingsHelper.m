//
//  AppSettingsHelper.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "AppSettingsHelper.h"

#define SEARCHSETTINGS_COMMUNITY_NAME_DEFAULT   @"public"
#define KEY_SEARCHSETTINGS_COMMUNITY_NAME       @"SNMPCommunityName"

@implementation AppSettingsHelper

+(NSString *)getSNMPCommunityName
{
    NSUserDefaults *appSettings = [NSUserDefaults standardUserDefaults];
    NSString *communityName = [appSettings objectForKey:KEY_SEARCHSETTINGS_COMMUNITY_NAME];
    
    if(communityName == nil)
    {
        communityName = SEARCHSETTINGS_COMMUNITY_NAME_DEFAULT;
        [appSettings setObject:communityName forKey:KEY_SEARCHSETTINGS_COMMUNITY_NAME];
    }
    
    return communityName;
}

+ (void)saveSNMPCommunityName:(NSString *)communityName
{
    if(communityName == nil || communityName.length == 0)
    {
        communityName = SEARCHSETTINGS_COMMUNITY_NAME_DEFAULT;
    }
    
    NSUserDefaults *appSettings = [NSUserDefaults standardUserDefaults];
    [appSettings setObject:communityName forKey:KEY_SEARCHSETTINGS_COMMUNITY_NAME];
}

@end
