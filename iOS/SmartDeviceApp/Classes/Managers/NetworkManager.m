//
//  NetworkManager.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "NetworkManager.h"
#import "Reachability.h"

static Reachability* sharedReachabilityForInternet = nil;

@implementation NetworkManager

+ (BOOL)isConnectedToNetwork
{
    if (sharedReachabilityForInternet == nil)
        sharedReachabilityForInternet = [Reachability reachabilityForInternetConnection];
 
    // check for internet connection
    NetworkStatus internetStatus = [sharedReachabilityForInternet currentReachabilityStatus];
    
    if (internetStatus != NotReachable)
        return YES;
    else
        return NO;
}

@end
