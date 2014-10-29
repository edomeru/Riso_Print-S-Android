//
//  NetworkManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "NetworkManager.h"
#import "Reachability.h"

@implementation NetworkManager

+ (BOOL)isConnectedToLocalWifi
{
    Reachability *reachabilityForLocalWifi = [Reachability reachabilityForLocalWiFi];
 
    // check for internet connection
    NetworkStatus localWifiStatus = [reachabilityForLocalWifi currentReachabilityStatus];
    
    if (localWifiStatus != NotReachable)
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

@end
