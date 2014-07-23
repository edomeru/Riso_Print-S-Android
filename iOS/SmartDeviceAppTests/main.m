//
//  main.m
//  SmartDeviceAppTests
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <objc/runtime.h>
#import "MagicalRecord.h"

int main(int argc, char * argv[])
{
    @autoreleasepool {
        [MagicalRecord setLoggingMask:MagicalRecordLogMaskOff];
        return UIApplicationMain(argc, argv, nil, @"TestAppDelegate");
    }
}