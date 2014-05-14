//
//  main.m
//  SmartDeviceAppTests
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <objc/runtime.h>

int main(int argc, char * argv[])
{
    @autoreleasepool {
#if GHUNIT_CLI
        CFMessagePortCreateLocal(NULL, (CFStringRef)@"PurpleWorkspacePort", NULL, NULL, NULL);
        class_replaceMethod([UIWindow class], @selector(_createContext), imp_implementationWithBlock(^{}), "v@:");
#endif
        return UIApplicationMain(argc, argv, nil, @"TestAppDelegate");
    }
}