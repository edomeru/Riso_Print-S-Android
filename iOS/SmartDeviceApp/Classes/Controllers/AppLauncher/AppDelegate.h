//
//  AppDelegate.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

/**
 * The device's last orientation before the current instance of RootViewController disappears or gets deallocated.
 * This is used by the RootViewController to get the orientation of the device
 * for cases when accessing UIDevice's orientation property returns UIDeviceOrientationUnknown
 * (e.g. when a new RootViewController is first pushed into the view hierarchy / when a PDF is loaded in the print preview screen)
 */
@property (nonatomic) NSInteger lastOrientation;

- (void)endBgTask;

@end
