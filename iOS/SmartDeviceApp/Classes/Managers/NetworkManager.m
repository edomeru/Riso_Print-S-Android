//
//  NetworkManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "NetworkManager.h"
#import "Reachability.h"

static Reachability* sharedReachabilityForLocalWifi = nil;

@implementation NetworkManager

+ (BOOL)isConnectedToLocalWifi
{
    if (sharedReachabilityForLocalWifi == nil)
        sharedReachabilityForLocalWifi = [Reachability reachabilityForLocalWiFi];
 
    // check for internet connection
    NetworkStatus localWifiStatus = [sharedReachabilityForLocalWifi currentReachabilityStatus];
    
    if (localWifiStatus != NotReachable)
        return YES;
    else
        return NO;
}

@end
